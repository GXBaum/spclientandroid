import SwiftUI
import sharedKit

struct CourseAddView: View {
    private let viewModel = KoinHelper.shared.settingsViewModel
    
    @State private var state = KoinHelper.shared.settingsViewModel.settingsScreenState.value
    @State private var searchInput = ""
    @Environment(\.dismiss) private var dismiss
        
    var body: some View {
        NavigationStack {
            VStack {
                List {
                    ForEach(state.courseSearch, id: \.self) { course in
                        Button {
                            viewModel.postSelectedCourse(courseName: course)
                            dismiss()
                        } label: {
                            HStack {
                                Text(course)
                                Spacer()
                            }
                            .contentShape(Rectangle())
                        }
                        .buttonStyle(.plain)
                    }
                }
                .searchable(
                    text: $searchInput,
                    prompt: "Klassenname (z. B. G10a, Q1/Q2)"
                )
                .onChange(of: searchInput) { newValue in
                    viewModel.searchCourses(courseName: newValue)
                }
                .onAppear {
                    viewModel.searchCourses(courseName: "")
                }
            }
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Fertig") {
                        dismiss()
                    }
                }
            }
            .navigationTitle("Klasse hinzufügen")
            .navigationBarTitleDisplayMode(.inline)
        }
        .task {
            for await newState in viewModel.settingsScreenState {
                state = newState
            }
        }
    }
}
