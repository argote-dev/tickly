import SwiftUI

/// App composition wires native data adapters into feature presentation.
struct TicklyAppView: View {
    @StateObject private var store: TimerStore
    private let soundPreview: SoundPreviewing
    @State private var settingsShown = false
    @State private var showSplash = true
    @Environment(\.scenePhase) private var scenePhase

    init() {
        let notifications = TicklyNotifications.shared
        _store = StateObject(wrappedValue: TimerStore(
            snapshots: UserDefaultsTimerSnapshotRepository(),
            notifications: notifications,
            effects: UIKitTimerPlatformEffects()
        ))
        soundPreview = SoundPreview()
    }

    var body: some View {
        let accent = Color.ticklyAccent(at: store.accent.index)
        ZStack {
            TimerScreen(store: store) { settingsShown = true }
                .accessibilityHidden(showSplash)
                .allowsHitTesting(!showSplash)
            if showSplash {
                TicklySplash { showSplash = false }
                    .transition(.opacity)
                    .zIndex(1)
            }
        }
            .preferredColorScheme(.dark)
            .tint(accent)
            .sheet(isPresented: $settingsShown) {
                SettingsView(
                    settings: store.engine.settings,
                    strings: store.strings,
                    onSave: store.apply,
                    onPreviewSound: soundPreview.preview(soundIndex:)
                )
            }
            .onChange(of: scenePhase) { _, newValue in store.onScenePhase(newValue) }
    }
}
