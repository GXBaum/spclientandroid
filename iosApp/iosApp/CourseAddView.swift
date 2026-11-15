import SwiftUI
import sharedKit

struct CourseAddView: View {
    @StateObject private var storeOwner = IosViewModelStoreOwner()
    private var viewModel: SettingsViewModel {
        storeOwner.viewModel()
    }

    @State private var searchInput = ""
    @Environment(\.dismiss) private var dismiss

    @State private var selectedCourse: String?
    @State private var showLoadingError = false

    var body: some View {
        NavigationStack {
            Observing(viewModel.settingsScreenState) { state in
                VStack {
                    List {
                        ForEach(state.courseSearch, id: \.self) { course in
                            Button {
                                selectedCourse = course
                                viewModel.postSelectedCourse(courseName: course)
                            } label: {
                                HStack {
                                    Text(course)
                                    Spacer()

                                    if state.courseSaveState is CourseSaveState.Loading && course == selectedCourse {
                                        ProgressView()
                                    }
                                }
                                .contentShape(Rectangle())
                            }
                            .buttonStyle(.plain)
                            .disabled(state.courseSaveState is CourseSaveState.Loading)
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
                    .onChange(of: state.courseSaveState is CourseSaveState.Success) { success in
                        if success {
                            dismiss()
                            showLoadingError = false
                        }
                    }
                    .onChange(of: state.courseSaveState is CourseSaveState.Error) { error in
                        if error {
                            showLoadingError = true
                        }
                    }
                    .alert(
                        "Klasse konnte nicht hinzugefügt werden",
                        isPresented: $showLoadingError
                    ) {
                        Button("OK", role: .cancel) {}
                    } message: {
                        if let error = state.courseSaveState as? CourseSaveState.Error {
                            Text(error.message)
                        }
                    }
                    .interactiveDismissDisabled(
                        state.courseSaveState is CourseSaveState.Loading
                    )
                }
                .toolbar {
                    ToolbarItem(placement: .cancellationAction) {
                        Button("Fertig") {
                            dismiss()
                        }
                        .disabled(state.courseSaveState is CourseSaveState.Loading)
                    }
                }
                .navigationTitle("Klasse hinzufügen")
                .navigationBarTitleDisplayMode(.inline)
            }
        }
    }
}
