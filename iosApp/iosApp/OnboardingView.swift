import SwiftUI
import sharedKit

struct Page: Identifiable {
    let id = UUID()
    let title: String
    let description: String
    let image: Image
}

struct OnboardingView: View {
    private var onboardingViewModel = KoinHelper.shared.onboardingViewModel
    @State private var state = KoinHelper.shared.onboardingViewModel.loginState.value

    @State private var currentPage = 0

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

                if let error = state as? LoginState.Error {
                    Text(error.message)
                }


                let isLastPage = currentPage == pages.count - 1
                Button {
                    withAnimation {
                        if isLastPage {
                            onboardingViewModel.createAccount()
                        } else {
                            currentPage += 1
                        }
                    }
                } label: {
                    Text(
                        isLastPage
                            ? (state is LoginState.Error ? "Erneut versuchen" : "Fertig")
                            : "Weiter"
                    )
                    .font(.headline)
                    .frame(maxWidth: .infinity)
                }
                .buttonStyle(.borderedProminent)
                .controlSize(.large)
                .padding(.horizontal)
                .disabled(state is LoginState.Loading)
            }
            .animation(.spring(response: 0.5, dampingFraction: 0.6), value: state is LoginState.Error)
        }
        .task {
            for await newState in onboardingViewModel.loginState {
                state = newState
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
