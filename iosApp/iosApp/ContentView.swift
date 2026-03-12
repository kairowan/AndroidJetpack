import AVKit
import SharedIosApp
import SwiftUI

/// @author 浩楠
/// @date 2026-03-12
///      _              _           _     _   ____  _             _ _
///     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
///    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
///   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
///  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
///  描述: iOS 宿主入口，承载 Home、Shorts、Detail 三条页面链路
struct ContentView: View {
    @Environment(\.colorScheme) private var colorScheme
    @State private var selectedTab: RootTab = .home
    @State private var homeBottomBarHidden = false
    @State private var shortsBottomBarHidden = false

    var body: some View {
        let designSnapshot = IosDesignBridge().snapshot(darkTheme: colorScheme == .dark)

        TabView(selection: $selectedTab) {
            HomeTabView(designSnapshot: designSnapshot)
                .tag(RootTab.home)
                .onPreferenceChange(BottomBarHiddenPreferenceKey.self) { homeBottomBarHidden = $0 }

            ShortsTabView(designSnapshot: designSnapshot)
                .tag(RootTab.shorts)
                .onPreferenceChange(BottomBarHiddenPreferenceKey.self) { shortsBottomBarHidden = $0 }
        }
        .toolbar(.hidden, for: .tabBar)
        .background(Color(argb: designSnapshot.backgroundArgb).ignoresSafeArea())
        .safeAreaInset(edge: .bottom, spacing: 0) {
            if !isBottomBarHidden {
                AppBottomBar(
                    selectedTab: $selectedTab,
                    designSnapshot: designSnapshot
                )
            }
        }
    }

    private var isBottomBarHidden: Bool {
        switch selectedTab {
        case .home:
            homeBottomBarHidden
        case .shorts:
            shortsBottomBarHidden
        }
    }
}

private enum RootTab: CaseIterable, Hashable {
    case home
    case shorts

    var title: String {
        switch self {
        case .home:
            "首页"
        case .shorts:
            "短视频"
        }
    }

    var iconName: String {
        switch self {
        case .home:
            "house.fill"
        case .shorts:
            "play.fill"
        }
    }
}

private struct BottomBarHiddenPreferenceKey: PreferenceKey {
    static let defaultValue = false

    static func reduce(value: inout Bool, nextValue: () -> Bool) {
        value = value || nextValue()
    }
}

private struct AppBottomBar: View {
    @Binding var selectedTab: RootTab
    let designSnapshot: IosThemeSnapshot

    var body: some View {
        HStack(spacing: 0) {
            ForEach(RootTab.allCases, id: \.self) { tab in
                Button {
                    selectedTab = tab
                } label: {
                    VStack(spacing: 10) {
                        ZStack {
                            RoundedRectangle(cornerRadius: 22, style: .continuous)
                                .fill(selectedTab == tab ? Color.black : Color.clear)
                                .frame(width: 94, height: 42)

                            Image(systemName: tab.iconName)
                                .font(.system(size: 24, weight: .bold))
                                .foregroundStyle(selectedTab == tab ? Color.white : Color.black)
                        }

                        Text(tab.title)
                            .font(.system(size: CGFloat(designSnapshot.bodyFontSizeSp), weight: .medium))
                            .foregroundStyle(Color.black)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.top, 2)
                    .padding(.bottom, 4)
                }
                .buttonStyle(.plain)
            }
        }
        .padding(.horizontal, 12)
        .padding(.top, 8)
        .padding(.bottom, 10)
        .background(
            Color.white
                .shadow(color: Color.black.opacity(0.08), radius: 12, y: -2)
        )
    }
}

/// 描述: iOS 首页 Tab
private struct HomeTabView: View {
    let designSnapshot: IosThemeSnapshot

    @StateObject private var viewModel = HomeFeedViewModel()

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                if !viewModel.sources.isEmpty {
                    HomeFeedSourceSelector(
                        selectedSourceKey: viewModel.selectedSourceKey,
                        sources: viewModel.sources,
                        designSnapshot: designSnapshot,
                        onSelected: viewModel.updateSelectedSource
                    )
                }

                if viewModel.isLoading {
                    ProgressView()
                        .tint(Color(argb: designSnapshot.primaryArgb))
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else if let errorMessage = viewModel.errorMessage {
                    FeedbackView(
                        designSnapshot: designSnapshot,
                        title: designSnapshot.errorTitle,
                        message: errorMessage,
                        actionTitle: designSnapshot.retryLabel,
                        onAction: viewModel.retry
                    )
                } else {
                    ScrollView {
                        LazyVStack(spacing: 18) {
                            ForEach(viewModel.entries, id: \.stableKey) { entry in
                                homeEntryView(entry)
                            }

                            if viewModel.isLoadingMore {
                                ProgressView()
                                    .tint(Color(argb: designSnapshot.primaryArgb))
                                    .frame(maxWidth: .infinity)
                                    .padding(.vertical, 12)
                            }
                        }
                        .padding(.horizontal, 16)
                        .padding(.top, 12)
                        .padding(.bottom, 20)
                    }
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
            .background(Color(argb: designSnapshot.backgroundArgb))
        }
        .task {
            viewModel.loadIfNeeded()
        }
    }

    @ViewBuilder
    private func homeEntryView(_ entry: IosHomeFeedEntry) -> some View {
        switch entry.kind {
        case "VIDEO":
            if let video = entry.video {
                NavigationLink {
                    VideoDetailView(
                        video: video,
                        designSnapshot: designSnapshot
                    )
                } label: {
                    HomeVideoCard(
                        video: video
                    )
                }
                .buttonStyle(.plain)
                .onAppear {
                    viewModel.loadMoreIfNeeded(currentItemId: Int(video.id))
                }
            }
        case "HEADER":
            if let text = entry.text, !text.isEmpty {
                HomeFeedHeaderView(
                    text: text,
                    designSnapshot: designSnapshot
                )
            }
        case "FOOTER":
            if let text = entry.text, !text.isEmpty {
                HomeFeedFooterView(text: text)
            }
        default:
            EmptyView()
        }
    }
}

/// 描述: iOS 首页频道选择器
private struct HomeFeedSourceSelector: View {
    let selectedSourceKey: String
    let sources: [IosFeedOption]
    let designSnapshot: IosThemeSnapshot
    let onSelected: (String) -> Void

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 16) {
                ForEach(sources, id: \.key) { source in
                    Button {
                        onSelected(source.key)
                    } label: {
                        Text(source.title)
                            .font(.system(size: CGFloat(designSnapshot.bodyFontSizeSp + 1), weight: .semibold))
                            .foregroundStyle(
                                selectedSourceKey == source.key
                                    ? Color.black.opacity(0.86)
                                    : Color.black.opacity(0.58)
                            )
                            .padding(.horizontal, 22)
                            .padding(.vertical, 14)
                            .background(
                                RoundedRectangle(cornerRadius: 16, style: .continuous)
                                    .fill(
                                        selectedSourceKey == source.key
                                            ? AppChromePalette.chipSelected
                                            : Color.white
                                    )
                            )
                            .overlay(
                                RoundedRectangle(cornerRadius: 16, style: .continuous)
                                    .stroke(
                                        selectedSourceKey == source.key
                                            ? Color.clear
                                            : AppChromePalette.chipBorder,
                                        lineWidth: 1.5
                                    )
                            )
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(.horizontal, 12)
            .padding(.top, 14)
            .padding(.bottom, 18)
        }
        .background(Color(argb: designSnapshot.backgroundArgb))
    }
}

/// 描述: iOS 首页卡片
private struct HomeVideoCard: View {
    let video: IosVideoCard

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            RemoteCoverImage(urlString: video.coverUrl)
                .frame(height: 212)
                .clipped()

            HStack(alignment: .center, spacing: 16) {
                RemoteAvatarImage(
                    urlString: video.authorIcon,
                    size: 48,
                    cornerRadius: 4
                )

                VStack(alignment: .leading, spacing: 6) {
                    Text(video.title)
                        .font(.system(size: 18, weight: .bold))
                        .foregroundStyle(Color.black.opacity(0.88))
                        .lineLimit(2)

                    Text(video.subtitle)
                        .font(.system(size: 15, weight: .regular))
                        .foregroundStyle(Color.black.opacity(0.48))
                        .lineLimit(1)
                }
                Spacer(minLength: 0)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 18)
            .background(AppChromePalette.cardTint)
        }
        .background(AppChromePalette.cardTint)
        .clipShape(RoundedRectangle(cornerRadius: 24, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 24, style: .continuous)
                .stroke(Color.black.opacity(0.04), lineWidth: 1)
        )
        .shadow(color: Color.black.opacity(0.10), radius: 10, y: 4)
    }
}

private struct HomeFeedHeaderView: View {
    let text: String
    let designSnapshot: IosThemeSnapshot

    var body: some View {
        Text(text)
            .font(.system(size: CGFloat(designSnapshot.bodyFontSizeSp), weight: .bold))
            .foregroundStyle(Color.black.opacity(0.86))
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.horizontal, 4)
            .padding(.top, 10)
            .padding(.bottom, 6)
    }
}

private struct HomeFeedFooterView: View {
    let text: String

    var body: some View {
        Text(text)
            .font(.system(size: 16, weight: .medium))
            .foregroundStyle(AppChromePalette.footerBlue)
            .frame(maxWidth: .infinity)
            .padding(.vertical, 20)
    }
}

/// 描述: iOS Shorts Tab
private struct ShortsTabView: View {
    let designSnapshot: IosThemeSnapshot

    @StateObject private var viewModel = ShortsFeedViewModel()

    var body: some View {
        NavigationStack {
            Group {
                if viewModel.isLoading {
                    ProgressView()
                        .tint(Color(argb: designSnapshot.primaryArgb))
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else if let errorMessage = viewModel.errorMessage {
                    FeedbackView(
                        designSnapshot: designSnapshot,
                        title: designSnapshot.errorTitle,
                        message: errorMessage,
                        actionTitle: designSnapshot.retryLabel,
                        onAction: viewModel.reload
                    )
                } else if viewModel.videos.isEmpty {
                    EmptyStateView(
                        designSnapshot: designSnapshot,
                        message: viewModel.emptyMessage
                    )
                } else {
                    ZStack(alignment: .bottomTrailing) {
                        TabView(
                            selection: Binding(
                                get: { viewModel.currentPage },
                                set: { viewModel.updateCurrentPage($0) }
                            )
                        ) {
                            ForEach(Array(viewModel.videos.enumerated()), id: \.element.id) { index, video in
                                ShortsVideoPage(
                                    video: video
                                )
                                .tag(index)
                            }
                        }
                        .tabViewStyle(.page(indexDisplayMode: .never))
                        .background(Color.black)

                        if viewModel.isLoadingMore {
                            ProgressView()
                                .tint(Color.white)
                                .padding(.trailing, 20)
                                .padding(.bottom, 28)
                        }
                    }
                }
            }
            .background(Color(argb: designSnapshot.backgroundArgb))
        }
        .task {
            viewModel.loadIfNeeded()
        }
    }
}

/// 描述: iOS Shorts 单页
private struct ShortsVideoPage: View {
    let video: IosVideoCard

    var body: some View {
        ZStack {
            SharedVideoPlayer(
                urlString: video.playUrl,
                cornerRadius: 0
            )
            .ignoresSafeArea()

            LinearGradient(
                colors: [
                    .clear,
                    Color.black.opacity(0.82),
                ],
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()

            VStack {
                Spacer()

                VStack(alignment: .leading, spacing: 14) {
                    HStack(spacing: 12) {
                        RemoteAvatarImage(
                            urlString: video.authorIcon,
                            size: 50,
                            cornerRadius: 4
                        )
                        Text(video.authorHandle)
                            .font(.system(size: 24, weight: .bold))
                            .foregroundStyle(.white)
                    }

                    Text(video.title)
                        .font(.system(size: 22, weight: .bold))
                        .foregroundStyle(.white)
                        .lineLimit(2)

                    Text(video.subtitle)
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundStyle(Color.white.opacity(0.78))
                        .lineLimit(1)

                    HStack(spacing: 22) {
                        Image(systemName: "arrow.up.left.and.arrow.down.right")
                            .font(.system(size: 28, weight: .medium))
                            .foregroundStyle(Color.white)
                        Image(systemName: "rotate.right")
                            .font(.system(size: 28, weight: .medium))
                            .foregroundStyle(Color.white)
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.horizontal, 30)
                .padding(.bottom, 34)
            }
        }
        .background(Color.black)
    }
}

/// 描述: iOS 详情页
private struct VideoDetailView: View {
    let video: IosVideoCard
    let designSnapshot: IosThemeSnapshot

    private var detailState: IosVideoDetailScreenState {
        IosVideoDetailBridge().screenState(video: video)
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 18) {
                SharedVideoPlayer(
                    urlString: detailState.playUrl,
                    cornerRadius: 20
                )
                .shadow(color: Color.black.opacity(0.08), radius: 14, y: 4)

                Text(detailState.title)
                    .font(.system(size: CGFloat(designSnapshot.titleFontSizeSp), weight: .bold))
                    .foregroundStyle(Color.black.opacity(0.92))

                Text(detailState.metadataLabel)
                    .font(.system(size: CGFloat(designSnapshot.bodyFontSizeSp)))
                    .foregroundStyle(Color(argb: designSnapshot.secondaryTextArgb))
                    .padding(.horizontal, 12)
                    .padding(.vertical, 8)
                    .background(Color(argb: designSnapshot.surfaceArgb))
                    .clipShape(Capsule())

                HStack(spacing: 12) {
                    RemoteAvatarImage(urlString: detailState.authorIcon)
                    VStack(alignment: .leading, spacing: 4) {
                        Text(detailState.authorName)
                            .font(.system(size: CGFloat(designSnapshot.bodyFontSizeSp), weight: .semibold))
                        Text("Shared detail presenter")
                            .font(.system(size: CGFloat(designSnapshot.captionFontSizeSp)))
                            .foregroundStyle(Color(argb: designSnapshot.secondaryTextArgb))
                    }
                }
                .padding(14)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(Color(argb: designSnapshot.surfaceArgb))
                .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
                .overlay(
                    RoundedRectangle(cornerRadius: 18, style: .continuous)
                        .stroke(Color.black.opacity(0.05), lineWidth: 1)
                )

                if !detailState.descriptionText.isEmpty {
                    Text(detailState.descriptionText)
                        .font(.system(size: CGFloat(designSnapshot.bodyFontSizeSp)))
                        .foregroundStyle(Color(argb: designSnapshot.secondaryTextArgb))
                        .padding(18)
                        .background(Color(argb: designSnapshot.surfaceArgb))
                        .clipShape(RoundedRectangle(cornerRadius: 20, style: .continuous))
                }
            }
            .padding(20)
        }
        .background(Color(argb: designSnapshot.backgroundArgb))
        .navigationTitle("Detail")
        .navigationBarTitleDisplayMode(.inline)
        .preference(key: BottomBarHiddenPreferenceKey.self, value: true)
    }
}

/// 描述: iOS 播放组件
private struct SharedVideoPlayer: View {
    let urlString: String
    let cornerRadius: CGFloat

    @State private var player = AVPlayer()

    var body: some View {
        VideoPlayer(player: player)
            .frame(maxWidth: .infinity)
            .aspectRatio(16.0 / 9.0, contentMode: .fit)
            .clipShape(RoundedRectangle(cornerRadius: cornerRadius, style: .continuous))
            .onAppear {
                configurePlayer()
            }
            .onChange(of: urlString) { _ in
                configurePlayer()
            }
            .onDisappear {
                player.pause()
            }
    }

    private func configurePlayer() {
        guard let url = URL(string: urlString.atsSafeRemoteUrlString()) else { return }
        let currentUrl = (player.currentItem?.asset as? AVURLAsset)?.url
        if currentUrl != url {
            player.replaceCurrentItem(with: AVPlayerItem(url: url))
        }
        player.play()
    }
}

/// 描述: iOS 通用反馈视图
private struct FeedbackView: View {
    let designSnapshot: IosThemeSnapshot
    let title: String
    let message: String
    let actionTitle: String
    let onAction: () -> Void

    var body: some View {
        VStack(spacing: 12) {
            Text(title)
                .font(.system(size: CGFloat(designSnapshot.titleFontSizeSp), weight: .bold))
                .foregroundStyle(Color(argb: designSnapshot.errorArgb))
            Text(message)
                .font(.system(size: CGFloat(designSnapshot.bodyFontSizeSp)))
                .foregroundStyle(Color(argb: designSnapshot.secondaryTextArgb))
                .multilineTextAlignment(.center)
            Button(actionTitle, action: onAction)
                .buttonStyle(.borderedProminent)
                .tint(Color(argb: designSnapshot.primaryArgb))
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .padding(24)
    }
}

/// 描述: iOS 空态视图
private struct EmptyStateView: View {
    let designSnapshot: IosThemeSnapshot
    let message: String

    var body: some View {
        VStack(spacing: 10) {
            Text(message)
                .font(.system(size: CGFloat(designSnapshot.bodyFontSizeSp), weight: .semibold))
                .foregroundStyle(Color(argb: designSnapshot.secondaryTextArgb))
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .padding(24)
    }
}

private struct RemoteCoverImage: View {
    let urlString: String

    var body: some View {
        AsyncImage(url: URL(string: urlString.atsSafeRemoteUrlString())) { phase in
            switch phase {
            case .success(let image):
                image
                    .resizable()
                    .scaledToFill()
            case .failure:
                Rectangle()
                    .fill(Color.black.opacity(0.1))
            case .empty:
                ZStack {
                    Rectangle()
                        .fill(Color.black.opacity(0.06))
                    ProgressView()
                        .tint(Color.black.opacity(0.45))
                }
            @unknown default:
                Rectangle()
                    .fill(Color.black.opacity(0.1))
            }
        }
    }
}

private struct RemoteAvatarImage: View {
    let urlString: String
    var size: CGFloat = 44
    var cornerRadius: CGFloat = 6

    var body: some View {
        AsyncImage(url: URL(string: urlString.atsSafeRemoteUrlString())) { phase in
            switch phase {
            case .success(let image):
                image
                    .resizable()
                    .scaledToFill()
            default:
                Circle()
                    .fill(Color.black.opacity(0.12))
            }
        }
        .frame(width: size, height: size)
        .clipShape(RoundedRectangle(cornerRadius: cornerRadius, style: .continuous))
    }
}

private enum AppChromePalette {
    static let chipSelected = Color(.sRGB, red: 0.87, green: 0.84, blue: 0.92, opacity: 1)
    static let chipBorder = Color.black.opacity(0.36)
    static let cardTint = Color(.sRGB, red: 0.91, green: 0.89, blue: 0.93, opacity: 1)
    static let footerBlue = Color(.sRGB, red: 0.10, green: 0.46, blue: 0.82, opacity: 1)
}

extension Color {
    fileprivate init(argb: Int64) {
        let alpha = Double((argb >> 24) & 0xFF) / 255.0
        let red = Double((argb >> 16) & 0xFF) / 255.0
        let green = Double((argb >> 8) & 0xFF) / 255.0
        let blue = Double(argb & 0xFF) / 255.0
        self.init(.sRGB, red: red, green: green, blue: blue, opacity: alpha)
    }
}

private extension String {
    func atsSafeRemoteUrlString() -> String {
        guard hasPrefix("http://") else { return self }
        let host = removingPrefix("http://").split(separator: "/").first.map(String.init) ?? ""
        if host == "kaiyanapp.com" || host.hasSuffix(".kaiyanapp.com") {
            return replacingOccurrences(of: "http://", with: "https://", options: .anchored)
        }
        return self
    }

    private func removingPrefix(_ prefix: String) -> String {
        guard hasPrefix(prefix) else { return self }
        return String(dropFirst(prefix.count))
    }
}
