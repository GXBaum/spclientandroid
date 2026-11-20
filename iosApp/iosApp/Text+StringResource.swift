import SwiftUI
import sharedKit

// extension to accept resource without bundle
extension Text {
    init(_ resource: StringResource) {
        self.init(LocalizedStringKey(resource.resourceId), bundle: resource.bundle)
    }
}
