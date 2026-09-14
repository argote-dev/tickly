import SwiftUI

struct DiagonalRevealMask: Shape {
    var progress: CGFloat

    var animatableData: CGFloat {
        get { progress }
        set { progress = newValue }
    }

    func path(in rect: CGRect) -> Path {
        let slice = rect.height * 0.28
        // Its leading edge begins and ends outside the word, preventing early glyph flashes.
        let leading = (rect.width + 2 * slice) * progress
        var path = Path()
        path.move(to: .zero)
        path.addLine(to: CGPoint(x: leading, y: 0))
        path.addLine(to: CGPoint(x: leading - 2 * slice, y: rect.height))
        path.addLine(to: CGPoint(x: 0, y: rect.height))
        path.closeSubpath()
        return path
    }

}
