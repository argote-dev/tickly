import SwiftUI
import SharedLogic

struct TimerScreen: View {
    @ObservedObject var store: TimerStore
    let onSettings: () -> Void
    @State private var actionToConfirm: TimerAction?
    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    private enum TimerAction: Identifiable {
        case restart, reset, skip

        var id: Int {
            switch self {
            case .restart: 0
            case .reset: 1
            case .skip: 2
            }
        }
    }

    var body: some View {
        let accent = Color.ticklyAccent(at: store.accent.index)
        ZStack {
            TicklyBackground(
                accent: accent,
                animated: store.engine.settings.animatedBackground,
                sceneActive: store.isSceneActive
            )
            ScrollView(.vertical, showsIndicators: false) {
                VStack(spacing: 26) {
                    HStack {
                        Text("Tickly").font(.title2.weight(.semibold)).tracking(0.5)
                        Spacer()
                        Button(action: onSettings) {
                            Image(systemName: "slider.horizontal.3").font(.title3.weight(.semibold)).padding(12)
                        }
                        .foregroundStyle(accent)
                        .glassButton(accent: accent)
                        .accessibilityLabel(store.strings.text(.settings))
                    }

                    Spacer(minLength: 8)
                    VStack(spacing: 14) {
                        Text(store.phaseName.uppercased())
                            .font(.caption.weight(.bold))
                            .tracking(2)
                            .foregroundStyle(accent)
                        ZStack {
                            Circle().stroke(.white.opacity(0.10), lineWidth: 10)
                            Circle()
                                .trim(from: 0, to: store.progress)
                                .stroke(accent, style: .init(lineWidth: 10, lineCap: .round))
                                .rotationEffect(.degrees(-90))
                                .animation(reduceMotion ? nil : .easeInOut(duration: 0.25), value: store.progress)
                            Text(store.timeText)
                                .font(.system(size: 66, weight: .thin, design: .rounded))
                                .monospacedDigit()
                        }
                        .frame(width: 220, height: 220)
                        .accessibilityElement(children: .combine)
                        .accessibilityLabel("\(store.phaseName), \(store.timeText)")
                        Text(
                            "\(store.engine.completedFocusBlocks) / "
                                + "\(store.engine.settings.blocksUntilLongBreak) "
                                + "\(store.strings.text(.blocks))"
                        )
                            .font(.subheadline).foregroundStyle(.secondary)
                    }
                    .padding(.vertical, 22)

                    VStack(spacing: 12) {
                        Button(action: store.primaryAction) {
                            Label(store.primaryLabel, systemImage: store.isRunning ? "pause.fill" : "play.fill")
                                .frame(maxWidth: .infinity).font(.headline).padding(.vertical, 18)
                                // Plain buttons otherwise hit-test only their rendered label.
                                .contentShape(.interaction, Capsule())
                        }
                        .buttonStyle(.plain).foregroundStyle(.black).background(accent, in: Capsule())
                        .accessibilityHint(store.isRunning ? store.strings.text(.pause) : store.strings.text(.start))
                        HStack(spacing: 12) {
                            Button { actionToConfirm = .restart } label: {
                                Label(store.strings.text(.restart), systemImage: "arrow.counterclockwise")
                            }
                            Button {
                                store.engine.phase == .focus ? actionToConfirm = .skip : store.skip()
                            } label: {
                                Label(store.strings.text(.skip), systemImage: "forward.fill")
                            }
                            Button { actionToConfirm = .reset } label: {
                                Label(store.strings.text(.reset), systemImage: "stop.fill")
                            }
                        }
                        .font(.footnote.weight(.medium)).foregroundStyle(accent)
                        .buttonStyle(.plain)
                    }
                    .padding(18).glassCard(accent: accent)
                    Spacer(minLength: 18)
                }
                .padding(.horizontal, 20).padding(.vertical, 14)
            }
            .safeAreaInset(edge: .bottom) {
                if store.notificationsUnavailable {
                    Button { store.showNotificationExplanation = true } label: {
                        Label(store.strings.text(.notificationsUnavailable), systemImage: "bell.slash.fill")
                            .font(.footnote.weight(.medium)).padding(.vertical, 10).padding(.horizontal, 14)
                    }
                    .foregroundStyle(accent)
                    .glassCard(accent: accent)
                    .padding(.bottom, 4)
                }
            }
        }
        .alert(item: $actionToConfirm) { action in
            Alert(
                title: Text(confirmationTitle(for: action)),
                message: Text(confirmationMessage(for: action)),
                primaryButton: .destructive(Text(store.strings.text(.confirm))) {
                    confirm(action)
                },
                secondaryButton: .cancel(Text(store.strings.text(.cancel)))
            )
        }
        .alert(store.strings.text(.notificationsDisabled), isPresented: $store.showNotificationExplanation) {
            Button(store.strings.text(.allowNotifications)) { store.requestNotifications() }
            Button(store.strings.text(.cancel), role: .cancel) { }
        } message: { Text(store.strings.text(.notificationsExplanation)) }
    }

    private func confirmationTitle(for action: TimerAction) -> String {
        switch action {
        case .restart: store.strings.text(.restartTitle)
        case .reset: store.strings.text(.resetTitle)
        case .skip: store.strings.text(.abandonTitle)
        }
    }

    private func confirmationMessage(for action: TimerAction) -> String {
        switch action {
        case .restart: store.strings.text(.restartMessage)
        case .reset: store.strings.text(.resetMessage)
        case .skip: store.strings.text(.abandonMessage)
        }
    }

    private func confirm(_ action: TimerAction) {
        switch action {
        case .restart: store.restart()
        case .reset: store.reset()
        case .skip: store.skip()
        }
    }
}
