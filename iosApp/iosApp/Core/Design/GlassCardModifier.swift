import SwiftUI

extension View {
    func glassCard(accent: Color) -> some View {
        modifier(GlassCardModifier(accent: accent))
    }

    func glassButton(accent: Color) -> some View {
        modifier(GlassCardModifier(accent: accent, cornerRadius: 18))
    }
}

private struct GlassCardModifier: ViewModifier {
    @Environment(\.accessibilityReduceTransparency) private var reduceTransparency
    let accent: Color
    var cornerRadius: CGFloat = 28

    func body(content: Content) -> some View {
        let shape = RoundedRectangle(cornerRadius: cornerRadius)
        if reduceTransparency {
            content
                .background(.black.opacity(0.94), in: shape)
                .overlay { shape.strokeBorder(accent.opacity(0.28), lineWidth: 1) }
        } else {
            content.glassEffect(.regular.tint(accent.opacity(0.14)), in: .rect(cornerRadius: cornerRadius))
        }
    }
}
