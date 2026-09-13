import SwiftUI

/// Composes feature screens while retaining the one timer lifecycle owner.
struct TicklyAppView: View {
    @StateObject private var store = TimerStore()
    @State private var settingsShown = false
    @Environment(\.scenePhase) private var scenePhase

    var body: some View {
        let accent = Color.ticklyAccent(at: store.accent.index)
        TimerScreen(store: store) {
            settingsShown = true
        }
        .preferredColorScheme(.dark)
        .tint(accent)
        .sheet(isPresented: $settingsShown) {
            SettingsView(store: store)
        }
        .onChange(of: scenePhase) { _, newValue in
            store.onScenePhase(newValue)
        }
    }
}
