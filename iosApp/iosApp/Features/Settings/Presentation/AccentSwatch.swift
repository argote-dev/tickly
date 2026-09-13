import SwiftUI

struct AccentSwatch: View {
    let color: Color
    let name: String
    let selected: Bool
    let selectedValue: String
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Circle()
                .fill(color)
                .frame(width: 44, height: 44)
                .overlay {
                    if selected {
                        Image(systemName: "checkmark")
                            .font(.headline.weight(.black))
                            .foregroundStyle(.black)
                    }
                }
                .overlay { Circle().stroke(.white.opacity(selected ? 0.9 : 0.18), lineWidth: selected ? 3 : 1) }
        }
        .buttonStyle(.plain)
        .accessibilityLabel(name)
        .accessibilityValue(selected ? selectedValue : "")
        .accessibilityAddTraits(selected ? .isSelected : [])
    }
}
