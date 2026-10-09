import SwiftUI
import sharedKit

struct RootView: View {
    
    private let settingsRepo = KoinHelper.shared.settingsRepository
    @State private var isOnboardingCompleted: Bool? = nil
    
    var body: some View {
        Group {
            if isOnboardingCompleted == nil {
                ProgressView()
            } else if isOnboardingCompleted == true {
                VpView()
            } else {
                OnboardingView()
            }
        }
        .task {
            for await newState in settingsRepo.observeIsOnboardingCompleted() {
                withAnimation {
                    isOnboardingCompleted = newState.boolValue
                }
            }
        }
    }
}
