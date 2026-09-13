import SwiftUI

struct TicklyBackground: View {
    let accent: Color
    let animated: Bool
    let sceneActive: Bool
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    @State private var drift = false

    var body: some View {
        ZStack {
            Color.black
            LinearGradient(
                colors: [accent.opacity(0.34), .black, accent.opacity(0.10)],
                startPoint: drift ? .topTrailing : .topLeading,
                endPoint: drift ? .bottomLeading : .bottomTrailing
            )
            .blur(radius: 42)
            .scaleEffect(1.15)
            .animation(shouldAnimate ? .easeInOut(duration: 16).repeatForever(autoreverses: true) : nil, value: drift)
        }
        .ignoresSafeArea()
        .onAppear { if shouldAnimate { drift = true } }
        .onChange(of: shouldAnimate) { _, enabled in drift = enabled }
    }

    private var shouldAnimate: Bool { animated && sceneActive && !reduceMotion }
}
