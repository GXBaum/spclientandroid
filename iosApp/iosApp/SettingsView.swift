import SwiftUI
import sharedKit

struct SettingsView: View {
    @StateObject private var storeOwner = IosViewModelStoreOwner()
    private var settingsViewModel: SettingsViewModel {
        storeOwner.viewModel()
    }

    @Environment(\.dismiss) private var dismiss

    @State private var showAddCourse = false

    var body: some View {
        NavigationStack {
            Observing(settingsViewModel.settingsScreenState) { state in
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
    }
}

// MARK: - Collection Safe Index Helper
private extension Collection {
    subscript(safe index: Index) -> Element? {
        indices.contains(index) ? self[index] : nil
    }
}
