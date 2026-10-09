import SwiftUI
import sharedKit

struct SettingsView: View {
    private let settingsViewModel = KoinHelper.shared.settingsViewModel

    @State private var state = KoinHelper.shared.settingsViewModel.settingsScreenState.value
    @State private var selectedCourseName: String = ""
    @State private var newCourseInput = ""
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            Form {
                Section(header: Text("Ausgewählte Klassen")) {
                    ForEach(state.vpSelectedCourse, id: \.id) { course in
                        HStack {
                            Image(systemName: "book.closed")
                            
                            Text(course.name)
                                .fontWeight(.medium)
                            Spacer()
                            if !course.verified {
                                Text("Unbestätigt")
                                    .font(.caption2)
                                    .padding(.horizontal, 6)
                                    .padding(.vertical, 2)
                                    .background(Color.orange.opacity(0.15))
                                    .foregroundColor(.orange)
                                    .clipShape(Capsule())
                            }
                        }
                    }
                    .onDelete { indexSet in
                        for index in indexSet {
                            if let course = state.vpSelectedCourse[safe: index] {
                                settingsViewModel.deleteVpSelectedCourse(courseId: course.id)
                            }
                        }
                    }
                    HStack {
                        Image(systemName: "plus")
                        
                        TextField("Klassenname (z. B. G10a, Q1/Q2)", text: $newCourseInput)
                            .textInputAutocapitalization(.characters)
                            .autocorrectionDisabled()
                        
                        Button("Hinzufügen") {
                            addCourse(newCourseInput)
                        }
                        .disabled(newCourseInput.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)
                    }
                }
            }
            .navigationTitle("Klassen verwalten")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Fertig") {
                        dismiss()
                    }
                }
            }
        }
        .task {
            for await newState in settingsViewModel.settingsScreenState {
                self.state = newState
            }
        }
    }
    
    private func addCourse(_ name: String) {
        settingsViewModel.postSelectedCourse(courseName: name)
        newCourseInput = ""
        selectedCourseName = name
    }
}

// MARK: - Collection Safe Index Helper
private extension Collection {
    subscript(safe index: Index) -> Element? {
        indices.contains(index) ? self[index] : nil
    }
}
