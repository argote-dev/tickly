import SwiftUI
import SharedLogic

struct SettingsView: View {
    @ObservedObject var store: TimerStore
    @Environment(\.dismiss) private var dismiss
    @State private var focus = 25
    @State private var shortBreak = 5
    @State private var longBreak = 15
    @State private var blocks = 4
    @State private var accent = 0
    @State private var sound = 0
    @State private var vibration = true
    @State private var animated = true
    @State private var keepScreenOn = false
    @State private var language = "system"

    var body: some View {
        let draftAccent = Color.ticklyAccent(at: accent)
        NavigationStack {
            Form {
                Section(store.strings.text(.intervals)) {
                    Stepper("\(store.strings.text(.focusMinutes)): \(focus)", value: $focus, in: 1...180)
                    Stepper("\(store.strings.text(.shortBreakMinutes)): \(shortBreak)", value: $shortBreak, in: 1...60)
                    Stepper("\(store.strings.text(.longBreakMinutes)): \(longBreak)", value: $longBreak, in: 1...60)
                    Stepper("\(store.strings.text(.blocksUntilLongBreak)): \(blocks)", value: $blocks, in: 2...8)
                }
                Section(store.strings.text(.appearance)) {
                    VStack(alignment: .leading, spacing: 10) {
                        Text(store.strings.text(.accent)).font(.subheadline)
                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack(spacing: 12) {
                                ForEach(0..<6, id: \.self) { index in
                                    AccentSwatch(
                                        color: Color.ticklyAccents[index],
                                        name: store.strings.accentName(index),
                                        selected: accent == index,
                                        selectedValue: store.strings.spanish ? "Seleccionado" : "Selected"
                                    ) { accent = index }
                                }
                            }
                            .padding(.vertical, 2)
                        }
                    }
                    Toggle(store.strings.text(.animatedBackground), isOn: $animated)
                }
                Section(store.strings.text(.sounds)) {
                    Picker(store.strings.text(.sounds), selection: $sound) {
                        Text(store.strings.text(.silence)).tag(-1)
                        Text(store.strings.text(.soundOne)).tag(0)
                        Text(store.strings.text(.soundTwo)).tag(1)
                        Text(store.strings.text(.soundThree)).tag(2)
                    }
                    // Form pickers bridge to UIKit. Recreate their presentation
                    // identity with the draft accent so a cached chevron/value
                    // cannot retain the previously saved tint.
                    .tint(draftAccent)
                    .id(accent)
                    if sound >= 0 { Button(store.strings.text(.preview)) { SoundPreview.play(sound) } }
                    Toggle(store.strings.text(.vibration), isOn: $vibration)
                    Text(store.strings.text(.vibrationSystemNote)).font(.footnote).foregroundStyle(.secondary)
                }
                Section {
                    Toggle(store.strings.text(.keepScreenOn), isOn: $keepScreenOn)
                    Picker(store.strings.text(.language), selection: $language) {
                        Text(store.strings.text(.system)).tag("system"); Text(store.strings.text(.spanish)).tag("es"); Text(store.strings.text(.english)).tag("en")
                    }
                    .tint(draftAccent)
                    .id(accent)
                }
            }
            .scrollContentBackground(.hidden).background(Color.black)
            .navigationTitle(store.strings.text(.settings))
            .toolbar { ToolbarItem(placement: .confirmationAction) { Button(store.strings.text(.done)) { save(); dismiss() } } }
            .onAppear { load() }
            .onDisappear { save() }
        }
        .preferredColorScheme(.dark)
        // The sheet has its own presentation hierarchy. Apply the draft tint here so
        // standard controls preview the selected accent before settings are saved.
        .tint(draftAccent)
    }

    private func load() {
        let settings = store.engine.settings
        focus = Int(settings.focusMinutes); shortBreak = Int(settings.shortBreakMinutes); longBreak = Int(settings.longBreakMinutes); blocks = Int(settings.blocksUntilLongBreak)
        accent = Int(settings.accentIndex); sound = Int(settings.soundIndex); vibration = settings.vibrationEnabled; animated = settings.animatedBackground; keepScreenOn = settings.keepScreenOn; language = settings.language
    }

    private func save() {
        store.apply(settings: TimerSettings(focusMinutes: Int32(focus), shortBreakMinutes: Int32(shortBreak), longBreakMinutes: Int32(longBreak), blocksUntilLongBreak: Int32(blocks), accentIndex: Int32(accent), soundIndex: Int32(sound), vibrationEnabled: vibration, animatedBackground: animated, keepScreenOn: keepScreenOn, language: language))
    }
}
