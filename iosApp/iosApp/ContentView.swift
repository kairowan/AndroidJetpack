import SharedIosApp
import SwiftUI
import UIKit

/// iOS 只保留平台入口，实际页面由共享 CMP 控制器渲染。
struct ContentView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        IosAppController().makeViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}
