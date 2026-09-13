import SwiftUI

/// App composition wires native data adapters into feature presentation.
struct TicklyAppView: View {
    @StateObject private var store: TimerStore
    private let soundPreview: SoundPreviewing
    @State private var settingsShown = false
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
        TimerScreen(store: store) { settingsShown = true }
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
