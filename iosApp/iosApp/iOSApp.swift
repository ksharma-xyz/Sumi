import SwiftUI
import FirebaseCore
import ComposeApp
import AppTrackingTransparency

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        FirebaseApp.configure()
        MainViewControllerKt.doInitKoinIos()
        return true
    }

    func applicationDidBecomeActive(_ application: UIApplication) {
        DispatchQueue.main.asyncAfter(deadline: .now() + 5.0) {
            let status = ATTrackingManager.trackingAuthorizationStatus
            self.showAttDebugAlert(status: status)
            guard status == ATTrackingManager.AuthorizationStatus.notDetermined else { return }
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                ATTrackingManager.requestTrackingAuthorization { _ in }
            }
        }
    }

    // DEBUG — remove before final submission
    private func showAttDebugAlert(status: ATTrackingManager.AuthorizationStatus) {
        let label: String
        switch status {
        case .notDetermined: label = "notDetermined — ATT dialog will appear in 0.5s"
        case .restricted:    label = "restricted — dialog suppressed by policy"
        case .denied:        label = "denied — user already refused"
        case .authorized:    label = "authorized — already granted"
        @unknown default:    label = "unknown (\(status.rawValue))"
        }
        guard
            let scene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
            let root = scene.windows.first?.rootViewController
        else { return }
        let alert = UIAlertController(title: "ATT Debug", message: label, preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: "OK", style: .default))
        topPresented(from: root).present(alert, animated: true)
    }

    private func topPresented(from vc: UIViewController) -> UIViewController {
        vc.presentedViewController.map { topPresented(from: $0) } ?? vc
    }
}

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
