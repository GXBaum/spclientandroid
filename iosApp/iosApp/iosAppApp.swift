import SwiftUI
import sharedKit

@main
struct iosAppApp: App {
    
    init() {
        KoinInitIosKt.doInitKoinIos()
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
