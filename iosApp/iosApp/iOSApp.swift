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
        guard ATTrackingManager.authorizationStatus == .notDetermined else { return }
        DispatchQueue.main.asyncAfter(deadline: .now() + 2.5) {
            ATTrackingManager.requestTrackingAuthorization { _ in }
        }
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
