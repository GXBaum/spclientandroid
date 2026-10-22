import SwiftUI
import sharedKit

struct Page: Identifiable {
    let id = UUID()
    let title: String
    let description: String
    let image: Image
}

struct OnboardingView: View {
    let pages = [
        Page(
            title: "HvK Client",
            description: "",
            image: Image(systemName: "graduationcap")
        ),
        Page(
            title: "Vertretungsplan Benachrichtigungen",
            description: "Du kannst über ausgewählte Klassen benachrichtigt werden.",
            image: Image(systemName: "bell")
        ),
        Page(
            title: "Vertretungsplan Informationen",
            description: "Über die Infos oben auf dem Plan wirst du auch informiert.",
            image: Image(systemName: "info.circle")
        ),
        Page(
            title: "Teile die App",
            description: "Empfehle sie deinen Mitschülern weiter.",
            image: Image(systemName: "megaphone")
        )
    ]
    
    @State private var currentPage = 0
    
    private let settingsRepo = KoinHelper.shared.settingsRepository
    private let onboardingViewModel = KoinHelper.shared.onboardingViewModel
    
    var body: some View {
        
        ZStack {
            VStack {
                TabView(selection: $currentPage) {
                    ForEach(pages.indices, id: \.self) { index in
                        OnboardingCardView(page: pages[index])
                            .tag(index)
                    }
                }
                .tabViewStyle(.page)
                .indexViewStyle(.page(backgroundDisplayMode: .always))
                
                Button {
                    withAnimation {
                        if currentPage == pages.count - 1 {
                            onboardingViewModel.createAccount()
                            Task {
                                do {
                                    try await settingsRepo.setOnboardingCompleted(completed: true)
                                } catch {
                                    
                                }
                            }
                        } else {
                            currentPage += 1
                        }
                    }
                } label: {
                    Text(currentPage == pages.count - 1 ? "Fertig" : "Weiter")
                        .font(.headline)
                        .frame(maxWidth: .infinity)
                }
                .buttonStyle(.borderedProminent)
                .controlSize(.large)
                .padding(.horizontal)

            }
        }
    }
}

#Preview {
    OnboardingView()
}


struct OnboardingCardView: View {
    let page: Page
    
    var body: some View {
        VStack {
            page.image
                .resizable()
                .scaledToFit()
                .frame(height: 120)

            Text(page.title)
                .font(.largeTitle)
                .fontWeight(.heavy)
                .multilineTextAlignment(.center)
                .padding(.bottom)
            
            Text(page.description)
            
        }
    }
}
