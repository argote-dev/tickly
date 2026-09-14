import SwiftUI

struct TicklyBrandMark: View {
    var color: Color = .ticklyBrandLavender

    var body: some View {
        Canvas { context, size in
            let scaleX = size.width / 48
            let scaleY = size.height / 48
            var main = Path()
            main.move(to: CGPoint(x: 3 * scaleX, y: 0))
            main.addQuadCurve(to: CGPoint(x: 0, y: 3 * scaleY), control: CGPoint(x: 0, y: 0))
            main.addLine(to: CGPoint(x: 0, y: 9 * scaleY))
            main.addQuadCurve(to: CGPoint(x: 3 * scaleX, y: 12 * scaleY), control: CGPoint(x: 0, y: 12 * scaleY))
            main.addLine(to: CGPoint(x: 17 * scaleX, y: 12 * scaleY))
            main.addLine(to: CGPoint(x: 17 * scaleX, y: 45 * scaleY))
            main.addQuadCurve(
                to: CGPoint(x: 20 * scaleX, y: 48 * scaleY),
                control: CGPoint(x: 17 * scaleX, y: 48 * scaleY)
            )
            main.addLine(to: CGPoint(x: 27 * scaleX, y: 48 * scaleY))
            main.addQuadCurve(
                to: CGPoint(x: 30 * scaleX, y: 45 * scaleY),
                control: CGPoint(x: 30 * scaleX, y: 48 * scaleY)
            )
            main.addLine(to: CGPoint(x: 30 * scaleX, y: 12 * scaleY))
            main.addLine(to: CGPoint(x: 37 * scaleX, y: 0))
            main.closeSubpath()
            context.fill(main, with: .color(color))
            var rightBar = Path()
            rightBar.move(to: CGPoint(x: 40 * scaleX, y: 0))
            rightBar.addLine(to: CGPoint(x: 45 * scaleX, y: 0))
            rightBar.addQuadCurve(to: CGPoint(x: 48 * scaleX, y: 3 * scaleY), control: CGPoint(x: 48 * scaleX, y: 0))
            rightBar.addLine(to: CGPoint(x: 48 * scaleX, y: 9 * scaleY))
            rightBar.addQuadCurve(
                to: CGPoint(x: 45 * scaleX, y: 12 * scaleY),
                control: CGPoint(x: 48 * scaleX, y: 12 * scaleY)
            )
            rightBar.addLine(to: CGPoint(x: 33 * scaleX, y: 12 * scaleY))
            rightBar.closeSubpath()
            context.fill(rightBar, with: .color(color))
        }
        .aspectRatio(1, contentMode: .fit)
    }
}
