import Foundation
import SharedIosApp

/**
 * @author 浩楠
 * @date 2026-03-11
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: iOS 首页状态容器
 */
@MainActor
final class HomeFeedViewModel: ObservableObject {
    @Published var sources: [IosFeedOption]
    @Published var selectedSourceKey: String
    @Published var title: String
    @Published var isLoading: Bool
    @Published var isLoadingMore: Bool
    @Published var errorMessage: String?
    @Published var entries: [IosHomeFeedEntry]
    @Published var videos: [IosVideoCard]

    private let bridge: IosHomeFeedBridge
    private var hasLoaded = false
    private var nextPageUrl: String?
    private var canLoadMore = false

    init(bridge: IosHomeFeedBridge = IosHomeFeedBridge()) {
        let defaultSourceKey = bridge.defaultSourceKey()
        let state = bridge.initialState(sourceKey: defaultSourceKey)
        self.bridge = bridge
        self.sources = bridge.availableSources()
        self.selectedSourceKey = defaultSourceKey
        self.title = state.title
        self.isLoading = state.isLoading
        self.isLoadingMore = false
        self.errorMessage = state.errorMessage
        self.entries = state.entries
        self.videos = state.videos
        self.nextPageUrl = state.nextPageUrl
        self.canLoadMore = state.nextPageUrl != nil
    }

    deinit {
        bridge.cancel()
    }

    func loadIfNeeded() {
        guard !hasLoaded else { return }
        hasLoaded = true
        loadSelectedSource()
    }

    func updateSelectedSource(_ sourceKey: String) {
        guard sourceKey != selectedSourceKey else { return }
        selectedSourceKey = sourceKey
        loadSelectedSource()
    }

    func retry() {
        loadSelectedSource()
    }

    func loadMoreIfNeeded(currentItemId: Int) {
        guard canLoadMore, !isLoading, !isLoadingMore else { return }
        guard let nextPageUrl, let triggerIndex = videos.firstIndex(where: { $0.id == currentItemId }) else { return }
        let thresholdIndex = max(videos.count - 3, 0)
        guard triggerIndex >= thresholdIndex else { return }

        isLoadingMore = true
        bridge.loadFeed(sourceKey: selectedSourceKey, nextPageUrl: nextPageUrl) { [weak self] state in
            DispatchQueue.main.async {
                self?.append(state)
            }
        }
    }

    private func loadSelectedSource() {
        let loadingState = bridge.initialState(sourceKey: selectedSourceKey)
        apply(loadingState, preserveVideos: false)
        bridge.loadFeed(sourceKey: selectedSourceKey, nextPageUrl: nil) { [weak self] state in
            DispatchQueue.main.async {
                self?.apply(state, preserveVideos: false)
            }
        }
    }

    private func append(_ state: IosFeedScreenState) {
        isLoadingMore = false
        errorMessage = state.errorMessage
        nextPageUrl = state.nextPageUrl
        canLoadMore = state.nextPageUrl != nil
        let existingKeys = Set(entries.map(\.stableKey))
        let newEntries = state.entries.filter { !existingKeys.contains($0.stableKey) }
        entries.append(contentsOf: newEntries)
        videos = entries.compactMap(\.video)
    }

    private func apply(_ state: IosFeedScreenState, preserveVideos: Bool) {
        title = state.title
        isLoading = state.isLoading
        isLoadingMore = false
        errorMessage = state.errorMessage
        nextPageUrl = state.nextPageUrl
        canLoadMore = state.nextPageUrl != nil
        if !preserveVideos {
            entries = state.entries
            videos = state.videos
        }
    }
}

/**
 *  描述: iOS Shorts 状态容器
 */
@MainActor
final class ShortsFeedViewModel: ObservableObject {
    @Published var isLoading: Bool
    @Published var isLoadingMore: Bool
    @Published var errorMessage: String?
    @Published var emptyMessage: String
    @Published var currentPage: Int
    @Published var videos: [IosVideoCard]
    @Published var controlsCopy: IosShortsControlsCopy

    private let bridge: IosShortsBridge
    private var hasLoaded = false
    private var nextPageUrl: String?
    private var canLoadMore = false

    init(bridge: IosShortsBridge = IosShortsBridge()) {
        let state = bridge.initialState()
        self.bridge = bridge
        self.isLoading = state.isLoading
        self.isLoadingMore = state.isLoadingMore
        self.errorMessage = state.errorMessage
        self.emptyMessage = state.emptyMessage
        self.currentPage = Int(state.currentPage)
        self.videos = state.videos
        self.controlsCopy = state.controlsCopy
        self.nextPageUrl = state.nextPageUrl
        self.canLoadMore = state.canLoadMore
    }

    deinit {
        bridge.cancel()
    }

    func loadIfNeeded() {
        guard !hasLoaded else { return }
        hasLoaded = true
        reload()
    }

    func reload() {
        apply(bridge.initialState())
        bridge.loadFeed { [weak self] state in
            DispatchQueue.main.async {
                self?.apply(state)
            }
        }
    }

    func updateCurrentPage(_ page: Int) {
        currentPage = max(0, min(page, max(videos.count - 1, 0)))
        loadMoreIfNeeded()
    }

    func loadMoreIfNeeded() {
        guard canLoadMore, !isLoading, !isLoadingMore else { return }
        guard let nextPageUrl else { return }
        guard currentPage >= max(videos.count - 3, 0) else { return }

        isLoadingMore = true
        bridge.loadFeed(nextPageUrl: nextPageUrl) { [weak self] state in
            DispatchQueue.main.async {
                self?.append(state)
            }
        }
    }

    private func apply(_ state: IosShortsScreenState) {
        isLoading = state.isLoading
        isLoadingMore = state.isLoadingMore
        errorMessage = state.errorMessage
        emptyMessage = state.emptyMessage
        currentPage = Int(state.currentPage)
        videos = state.videos
        controlsCopy = state.controlsCopy
        nextPageUrl = state.nextPageUrl
        canLoadMore = state.canLoadMore
    }

    private func append(_ state: IosShortsScreenState) {
        isLoadingMore = false
        errorMessage = state.errorMessage
        emptyMessage = state.emptyMessage
        controlsCopy = state.controlsCopy
        nextPageUrl = state.nextPageUrl
        canLoadMore = state.canLoadMore
        let existingIds = Set(videos.map(\.id))
        let newVideos = state.videos.filter { !existingIds.contains($0.id) }
        videos.append(contentsOf: newVideos)
    }
}
