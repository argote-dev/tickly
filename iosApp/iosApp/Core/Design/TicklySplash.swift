import SwiftUI

struct TicklySplash: View {
    let onFinished: () -> Void
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    @State private var reveal = false
    @State private var fadeOut = false
    @State private var wordWidth: CGFloat = 0

    var body: some View {
        GeometryReader { proxy in
            let markWidth: CGFloat = 60
            let travel = max(0, (wordWidth - markWidth) / 2)
            ZStack {
                Color.ticklySplashBackground.ignoresSafeArea()
                HStack(spacing: 0) {
                    TicklyBrandMark()
                        .frame(width: markWidth, height: markWidth)
                    Text("ickly")
                        .font(.system(size: 54, weight: .regular, design: .rounded))
                        .tracking(-2)
                        .lineLimit(1)
                        .minimumScaleFactor(0.6)
                        .foregroundStyle(Color.ticklyBrandLavender)
                        .mask(DiagonalRevealMask(progress: reveal ? 1 : 0))
                }
                .background {
                    GeometryReader { wordProxy in
                        Color.clear.preference(key: BrandWordWidthKey.self, value: wordProxy.size.width)
                    }
                }
                .offset(x: reveal ? 0 : travel)
                .frame(maxWidth: proxy.size.width - 32)
            }
            .opacity(fadeOut ? 0 : 1)
        }
        .onPreferenceChange(BrandWordWidthKey.self) { wordWidth = $0 }
        .accessibilityElement(children: .ignore)
        .accessibilityLabel("Tickly")
        .allowsHitTesting(true)
        .task { await playSequence() }
    }

    private struct BrandWordWidthKey: PreferenceKey {
        static let defaultValue: CGFloat = 0
        static func reduce(value: inout CGFloat, nextValue: () -> CGFloat) { value = nextValue() }
    }

    @MainActor
    private func playSequence() async {
        guard !reduceMotion else {
            onFinished()
            return
        }
        try? await Task.sleep(for: .seconds(2))
        guard !Task.isCancelled else { return }
        withAnimation(.easeInOut(duration: 0.8)) { reveal = true }
        try? await Task.sleep(for: .seconds(1.3))
        guard !Task.isCancelled else { return }
        withAnimation(.easeInOut(duration: 0.3)) { fadeOut = true }
        try? await Task.sleep(for: .seconds(0.3))
        guard !Task.isCancelled else { return }
        onFinished()
    }
}

extension Color {
    static let ticklyBrandLavender = Color(red: 184 / 255, green: 161 / 255, blue: 1)
    static let ticklySplashBackground = Color(red: 16 / 255, green: 16 / 255, blue: 21 / 255)
}
