import SwiftUI
import sharedKit

struct VpView: View {
    private let viewModel = KoinHelper.shared.vpViewModel
    private let settingsViewModel = KoinHelper.shared.settingsViewModel

    @State private var state = KoinHelper.shared.vpViewModel.vpScreenState.value
    
    @State private var showManageCoursesSheet = false
    @State private var selectedCourseId: String?
    
    var body: some View {
        NavigationStack {
            Group {
                if state.isLoading && state.substitutions == nil && state.selectedCourses.isEmpty {
                    ProgressView()
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else if state.selectedCourses.isEmpty {
                    VStack(spacing: 12) {
                        Text("Keine Klassen ausgewählt")
                        Button("Klasse hinzufügen") {
                            showManageCoursesSheet = true
                        }
                        .buttonStyle(.borderedProminent)
                    }
                } else {
                    pagingView
                }
            }
            .sheet(isPresented: $showManageCoursesSheet) {
                SettingsView()
            }
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        showManageCoursesSheet = true
                    } label: {
                        Image(systemName: "gearshape")
                    }
                }
                
                ToolbarItem(placement: .principal) {
                    if state.selectedCourses.count > 1 {
                        Picker("Picker", selection: $selectedCourseId.animation(.snappy(duration: 0.35))) {
                            ForEach(state.selectedCourses, id: \.id) { course in
                                Text(course.name)
                                    .tag(course.id)
                            }
                        }
                        .pickerStyle(.segmented)
                    } else {
                        // custom navigation Title for animation TODO not useful anymore
                        ZStack {
                            Text(navigationTitle)
                                .font(.headline)
                                .id(selectedCourseId)
                                //.transition(.blurReplace)
                        }
                        .animation(.snappy(duration: 0.3), value: selectedCourseId)
                    }
                }
            }
            .navigationTitle(navigationTitle)
            .navigationBarTitleDisplayMode(.inline)
            .task {
                for await newState in viewModel.vpScreenState {
                    state = newState
                    if selectedCourseId == nil {
                        selectedCourseId = state.selectedCourses.first?.id
                    }
                }
            }
        }
    }

    private var navigationTitle: String {
        state.selectedCourses.first(where: { $0.id == selectedCourseId })?.name ?? "Vertretungsplan"
    }
    
    @ViewBuilder
    private var pagingView: some View {
        if #available(iOS 17.0, *) {
            ScrollView(.horizontal, showsIndicators: false) {
                LazyHStack(spacing: 0) {
                    ForEach(state.selectedCourses, id: \.id) { course in
                        courseView(course: course)
                            .containerRelativeFrame(.horizontal)
                            .id(course.id)
                    }
                }
                .scrollTargetLayout()
            }
            .scrollTargetBehavior(.paging)
            .scrollPosition(id: $selectedCourseId)
        } else {
            TabView(selection: $selectedCourseId) {
                ForEach(state.selectedCourses, id: \.id) { course in
                    courseView(course: course)
                        .tag(course.id)
                }
            }
            .tabViewStyle(.page(indexDisplayMode: .never))
        }
    }
    
    private func courseView(course: SelectedCourse) -> some View {
        List {
            DaySection(title: "Heute", day: state.substitutions?.today, courseName: course.name)
            DaySection(title: "Nächster Schultag", day: state.substitutions?.tomorrow, courseName: course.name)
        }
        .refreshable {
            viewModel.refresh()
        }
    }
    
    
    
    
    
    struct CourseDayContent {
        let info: String?
        let substitutions: [VpSubstitution]
        let roomChanges: [VpSubstitution]
        let hasNoEntries: Bool

        init(day: VpDay, courseName: String) {
            let entries = day.substitutions.filter { $0.courseName == courseName }
            let infoText = day.info?.last?.text

            self.info = (infoText?.isEmpty ?? true) ? nil : infoText
            self.substitutions = entries.filter { $0.VpType == .substitution }
            self.roomChanges = entries.filter { $0.VpType == .differentRoom }
            self.hasNoEntries = entries.isEmpty
        }
    }
    
    // MARK: - Day sections
    struct DaySection: View {
        let title: String
        let day: VpDay?
        let courseName: String

        var body: some View {
            if let day {
                content(for: CourseDayContent(day: day, courseName: courseName), day: day)
            } else {
                Section {
                    Label("In Arbeit", systemImage: "hourglass")
                } header: {
                    DayHeader(title: title, day: day)
                }
            }
        }

        @ViewBuilder
        private func content(for content: CourseDayContent, day: VpDay) -> some View {
            let header = DayHeader(title: title, day: day)

            if let info = content.info {
                Section {
                    Text(info)
                } header: {
                    header
                }
                Section {
                    substitutionRows(for: content)
                }
            } else {
                Section {
                    substitutionRows(for: content)
                } header: {
                    header
                }
            }

            if !content.roomChanges.isEmpty {
                Section {
                    SubstitutionList(substitutions: content.roomChanges)
                } header: {
                    Text("Ersatzräume")
                }
            }
        }

        @ViewBuilder
        private func substitutionRows(for content: CourseDayContent) -> some View {
            if content.hasNoEntries {
                Text("Keine Einträge")
            } else if content.substitutions.isEmpty {
                // There are room changes below, so "Keine Einträge" would be misleading.
                Text("Keine Vertretungen")
            } else {
                SubstitutionList(substitutions: content.substitutions)
            }
        }
    }

    struct DayHeader: View {
        let title: String
        let day: VpDay?

        var body: some View {
            VStack(alignment: .leading) {
                Text(title)
                if let targetDate = day?.targetDate {
                    Text(relativeDateFormatter(date: targetDate))
                }
                if let dayString = day?.dayString {
                    Text(dayString)
                }
             }
        }
    }
    
    
    // MARK: - Substitution rows
    struct SubstitutionList: View {
        let substitutions: [VpSubstitution]

        private var active: [VpSubstitution] { substitutions.filter { !$0.isDeleted } }
        private var deleted: [VpSubstitution] { substitutions.filter { $0.isDeleted } }

        var body: some View {
            ForEach(active, id: \.id) { sub in
                SubstitutionRowView(sub: sub)
            }

            if !deleted.isEmpty {
                Label("Gelöscht", systemImage: "trash")
                    .listRowBackground(Color.red.opacity(0.5))
                    .foregroundStyle(.primary)
                    .listRowSeparator(.hidden)

                ForEach(deleted, id: \.id) { sub in
                    SubstitutionRowView(sub: sub)
                }
            }
        }
    }

    struct SubstitutionRowView: View {
        let sub: VpSubstitution

        @State private var isExpanded: Bool

        init(sub: VpSubstitution) {
            self.sub = sub
            self.isExpanded = sub.isDeleted
        }

        var body: some View {
            DisclosureGroup(isExpanded: $isExpanded) {
                VStack(alignment: .leading) {
                    Text("zuerst gesehen: \(relativeDateTimeFormatter(time: sub.createdAt))")

                    if sub.isDeleted {
                        // if it's deleted the last change should be the deletion, so this should be fine
                        Text("gelöscht: \(relativeDateTimeFormatter(time: sub.updatedAt))")
                    }
                }
                .foregroundStyle(.secondary)
                .frame(maxWidth: .infinity, alignment: .leading)
            } label: {
                HStack {
                    Text(sub.hour)
                        .frame(width: 20, alignment: .leading)
                    Text(sub.original)
                        .frame(width: 80, alignment: .leading)
                    Image(systemName: "arrow.right")
                        .font(.caption2)
                        .foregroundStyle(.secondary)
                    Text(sub.replacement)
                        .frame(width: 80, alignment: .leading)
                    Text(sub.description_)
                        .frame(maxWidth: .infinity, alignment: .leading)
                }
                //.strikethrough(sub.isDeleted)
                //.foregroundStyle(sub.isDeleted ? .secondary : .primary)
            }
            .font(.subheadline)
            .listRowBackground(sub.isDeleted ? Color.red.opacity(0.5) : nil)
        }
    }
}
