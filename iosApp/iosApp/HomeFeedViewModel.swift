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
    @Published var errorMessage: String?
    @Published var videos: [IosVideoCard]

    private let bridge: IosHomeFeedBridge
    private var hasLoaded = false

    init(bridge: IosHomeFeedBridge = IosHomeFeedBridge()) {
        let defaultSourceKey = bridge.defaultSourceKey()
        let state = bridge.initialState(sourceKey: defaultSourceKey)
        self.bridge = bridge
        self.sources = bridge.availableSources()
        self.selectedSourceKey = defaultSourceKey
        self.title = state.title
        self.isLoading = state.isLoading
        self.errorMessage = state.errorMessage
        self.videos = state.videos
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

    private func loadSelectedSource() {
        let loadingState = bridge.initialState(sourceKey: selectedSourceKey)
        apply(loadingState)
        bridge.loadFeed(sourceKey: selectedSourceKey) { [weak self] state in
            DispatchQueue.main.async {
                self?.apply(state)
            }
        }
    }

    private func apply(_ state: IosFeedScreenState) {
        title = state.title
        isLoading = state.isLoading
        errorMessage = state.errorMessage
        videos = state.videos
    }
}

/**
 * @author 浩楠
 * @date 2026-03-12
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: iOS Shorts 状态容器
 */
@MainActor
final class ShortsFeedViewModel: ObservableObject {
    @Published var isLoading: Bool
    @Published var errorMessage: String?
    @Published var emptyMessage: String
    @Published var currentPage: Int
    @Published var videos: [IosVideoCard]

    private let bridge: IosShortsBridge
    private var hasLoaded = false

    init(bridge: IosShortsBridge = IosShortsBridge()) {
        let state = bridge.initialState()
        self.bridge = bridge
        self.isLoading = state.isLoading
        self.errorMessage = state.errorMessage
        self.emptyMessage = state.emptyMessage
        self.currentPage = Int(state.currentPage)
        self.videos = state.videos
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
    }

    private func apply(_ state: IosShortsScreenState) {
        isLoading = state.isLoading
        errorMessage = state.errorMessage
        emptyMessage = state.emptyMessage
        currentPage = Int(state.currentPage)
        videos = state.videos
    }
}
