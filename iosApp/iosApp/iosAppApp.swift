import SwiftUI
import sharedKit
import FirebaseCore
import FirebaseMessaging

class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate, MessagingDelegate {
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        FirebaseApp.configure()
        
        
        UNUserNotificationCenter.current().delegate = self
        let authOptions: UNAuthorizationOptions = [.alert, .badge, .sound]
        
        UNUserNotificationCenter.current().requestAuthorization(
            options: authOptions,
            completionHandler: { _, _ in }
        )
        
        application.registerForRemoteNotifications()
        
        Messaging.messaging().delegate = self
        
        
        return true
    }
    
    func application(_ application: UIApplication,
                     didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data
    ) {
        print("APNs registration succeeded")
        Messaging.messaging().apnsToken = deviceToken
    }

    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                willPresent notification: UNNotification,
                                withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        completionHandler([.banner, .list, .sound])
    }
    
    func messaging(_ messaging: Messaging, didReceiveRegistration installationId: String?) {
        print("Firebase Installation ID: \(String(describing: installationId))")
        // Note: This callback is fired at each app startup.

        if let installationId = installationId {
            // Send the Firebase Installation ID to your app server.
            //sendRegistrationToServer(installationId)
            Task {
                do {
                    try await KoinHelper.shared.notificationTokenProvider.onNewFcmToken(token: installationId)
                } catch {
                    print("Failed to process Firebase Installation Id: \(error)")
                }
            }
        }
    }
        
}

@main
struct iosAppApp: App {
    // register App delegate for Firebase setup
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate
    
    init() {
        KoinInitIosKt.doInitKoinIos()
    }
    
    var body: some Scene {
        WindowGroup {
            RootView()
        }
    }
}
