import AVKit
import SwiftUI
import SharedIosApp

/**
 * @author 浩楠
 * @date 2026-03-12
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: iOS 宿主入口，承载 Home、Shorts、Detail 三条页面链路
 */
struct ContentView: View {
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let designSnapshot = IosDesignBridge().snapshot(darkTheme: colorScheme == .dark)

        TabView {
            HomeTabView(designSnapshot: designSnapshot)
                .tabItem {
                    Label("Home", systemImage: "house")
                }

            ShortsTabView(designSnapshot: designSnapshot)
                .tabItem {
                    Label("Shorts", systemImage: "play.rectangle")
                }
        }
        .tint(Color(argb: designSnapshot.primaryArgb))
        .background(Color(argb: designSnapshot.backgroundArgb))
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
 *  描述: iOS 首页 Tab
 */
private struct HomeTabView: View {
    let designSnapshot: IosThemeSnapshot

    @StateObject private var viewModel = HomeFeedViewModel()

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
                        onAction: viewModel.retry
                    )
                } else {
                    List(viewModel.videos, id: \.id) { video in
                        NavigationLink {
                            VideoDetailView(
                                video: video,
                                designSnapshot: designSnapshot
                            )
                        } label: {
                            HomeVideoRow(
                                video: video,
                                designSnapshot: designSnapshot
                            )
                        }
                        .listRowBackground(Color(argb: designSnapshot.surfaceArgb))
                    }
                    .listStyle(.plain)
                }
            }
            .background(Color(argb: designSnapshot.backgroundArgb))
            .navigationTitle(viewModel.title)
            .safeAreaInset(edge: .top) {
                if !viewModel.sources.isEmpty {
                    Picker("频道", selection: Binding(
                        get: { viewModel.selectedSourceKey },
                        set: { viewModel.updateSelectedSource($0) }
                    )) {
                        ForEach(viewModel.sources, id: \.key) { source in
                            Text(source.title).tag(source.key)
                        }
                    }
                    .pickerStyle(.segmented)
                    .padding(.horizontal, 16)
                    .padding(.top, 8)
                    .padding(.bottom, 4)
                    .background(Color(argb: designSnapshot.surfaceArgb).opacity(0.96))
                }
            }
        }
        .task {
            viewModel.loadIfNeeded()
        }
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
 *  描述: iOS Shorts Tab
 */
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
                    TabView(
                        selection: Binding(
                            get: { viewModel.currentPage },
                            set: { viewModel.updateCurrentPage($0) }
                        )
                    ) {
                        ForEach(Array(viewModel.videos.enumerated()), id: \.element.id) { index, video in
                            ShortsVideoPage(
                                video: video,
                                designSnapshot: designSnapshot
                            )
                            .tag(index)
                        }
                    }
                    .tabViewStyle(.page(indexDisplayMode: .automatic))
                    .background(Color.black)
                }
            }
            .background(Color(argb: designSnapshot.backgroundArgb))
            .navigationTitle("Shorts")
        }
        .task {
            viewModel.loadIfNeeded()
        }
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
 *  描述: iOS 首页列表项
 */
private struct HomeVideoRow: View {
    let video: IosVideoCard
    let designSnapshot: IosThemeSnapshot

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(video.title)
                .font(.system(size: CGFloat(designSnapshot.titleFontSizeSp), weight: .bold))
                .lineLimit(2)
            Text(video.subtitle)
                .font(.system(size: CGFloat(designSnapshot.bodyFontSizeSp)))
                .foregroundStyle(Color(argb: designSnapshot.secondaryTextArgb))
            Text(video.categoryDurationLabel)
                .font(.system(size: CGFloat(designSnapshot.captionFontSizeSp)))
                .foregroundStyle(Color(argb: designSnapshot.secondaryTextArgb))
            if !video.descriptionText.isEmpty {
                Text(video.descriptionText)
                    .font(.system(size: CGFloat(designSnapshot.captionFontSizeSp)))
                    .foregroundStyle(Color(argb: designSnapshot.secondaryTextArgb))
                    .lineLimit(3)
            }
        }
        .padding(.vertical, 6)
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
 *  描述: iOS Shorts 单页
 */
private struct ShortsVideoPage: View {
    let video: IosVideoCard
    let designSnapshot: IosThemeSnapshot

    var body: some View {
        ZStack(alignment: .bottomLeading) {
            SharedVideoPlayer(
                urlString: video.playUrl,
                cornerRadius: 0
            )
            .ignoresSafeArea()

            LinearGradient(
                colors: [
                    .clear,
                    Color.black.opacity(0.82)
                ],
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()

            VStack(alignment: .leading, spacing: 10) {
                Text(video.authorHandle)
                    .font(.system(size: CGFloat(designSnapshot.bodyFontSizeSp), weight: .bold))
                    .foregroundStyle(.white)
                Text(video.title)
                    .font(.system(size: CGFloat(designSnapshot.titleFontSizeSp), weight: .bold))
                    .foregroundStyle(.white)
                    .lineLimit(2)
                Text(video.subtitle)
                    .font(.system(size: CGFloat(designSnapshot.bodyFontSizeSp)))
                    .foregroundStyle(Color.white.opacity(0.78))
                    .lineLimit(1)
                NavigationLink {
                    VideoDetailView(
                        video: video,
                        designSnapshot: designSnapshot
                    )
                } label: {
                    Text("查看详情")
                        .font(.system(size: CGFloat(designSnapshot.bodyFontSizeSp), weight: .semibold))
                        .padding(.horizontal, 14)
                        .padding(.vertical, 8)
                        .background(Color(argb: designSnapshot.primaryArgb))
                        .foregroundStyle(Color.black.opacity(0.85))
                        .clipShape(Capsule())
                }
            }
            .padding(.horizontal, 20)
            .padding(.bottom, 40)
        }
        .background(Color.black)
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
 *  描述: iOS 详情页
 */
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

                Text(detailState.title)
                    .font(.system(size: CGFloat(designSnapshot.titleFontSizeSp), weight: .bold))
                    .foregroundStyle(Color(argb: designSnapshot.primaryArgb))

                Text(detailState.metadataLabel)
                    .font(.system(size: CGFloat(designSnapshot.bodyFontSizeSp)))
                    .foregroundStyle(Color(argb: designSnapshot.secondaryTextArgb))

                HStack(spacing: 12) {
                    Circle()
                        .fill(Color(argb: designSnapshot.secondaryArgb))
                        .frame(width: 44, height: 44)
                        .overlay(
                            Text(String(detailState.authorName.prefix(1)))
                                .font(.system(size: CGFloat(designSnapshot.bodyFontSizeSp), weight: .bold))
                                .foregroundStyle(Color.white)
                        )

                    VStack(alignment: .leading, spacing: 4) {
                        Text(detailState.authorName)
                            .font(.system(size: CGFloat(designSnapshot.bodyFontSizeSp), weight: .semibold))
                        Text("Shared detail presenter")
                            .font(.system(size: CGFloat(designSnapshot.captionFontSizeSp)))
                            .foregroundStyle(Color(argb: designSnapshot.secondaryTextArgb))
                    }
                }

                if !detailState.descriptionText.isEmpty {
                    Text(detailState.descriptionText)
                        .font(.system(size: CGFloat(designSnapshot.bodyFontSizeSp)))
                        .foregroundStyle(Color(argb: designSnapshot.secondaryTextArgb))
                }
            }
            .padding(20)
        }
        .background(Color(argb: designSnapshot.backgroundArgb))
        .navigationTitle("Detail")
        .navigationBarTitleDisplayMode(.inline)
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
 *  描述: iOS 播放组件
 */
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
        guard let url = URL(string: urlString) else { return }
        let currentUrl = (player.currentItem?.asset as? AVURLAsset)?.url
        if currentUrl != url {
            player.replaceCurrentItem(with: AVPlayerItem(url: url))
        }
        player.play()
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
 *  描述: iOS 通用反馈视图
 */
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

/**
 * @author 浩楠
 * @date 2026-03-12
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: iOS 空态视图
 */
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

private extension Color {
    init(argb: Int64) {
        let alpha = Double((argb >> 24) & 0xFF) / 255.0
        let red = Double((argb >> 16) & 0xFF) / 255.0
        let green = Double((argb >> 8) & 0xFF) / 255.0
        let blue = Double(argb & 0xFF) / 255.0
        self.init(.sRGB, red: red, green: green, blue: blue, opacity: alpha)
    }
}
