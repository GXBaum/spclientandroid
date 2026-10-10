import SwiftUI
import sharedKit

struct SettingsView: View {
    private let settingsViewModel = KoinHelper.shared.settingsViewModel

    @State private var state = KoinHelper.shared.settingsViewModel.settingsScreenState.value
    @Environment(\.dismiss) private var dismiss

    @State private var showAddCourse = false
    
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
                    
                    Button {
                        showAddCourse = true
                    } label: {
                        HStack {
                            Image(systemName: "plus")
                            Text("Klasse hinzufügen")
                        }
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
            .sheet(isPresented: $showAddCourse) {
                CourseAddView()
            }
        }
        .task {
            for await newState in settingsViewModel.settingsScreenState {
                self.state = newState
            }
        }
    }
}

// MARK: - Collection Safe Index Helper
private extension Collection {
    subscript(safe index: Index) -> Element? {
        indices.contains(index) ? self[index] : nil
    }
}
