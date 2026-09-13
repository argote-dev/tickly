import SharedLogic
import SwiftUI

struct SettingsView: View {
    let settings: TimerSettings
    let strings: TicklyStrings
    let onSave: (TimerSettings) -> Void
    let onPreviewSound: (Int) -> Void
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
                Section(strings.text(.intervals)) {
                    Stepper("\(strings.text(.focusMinutes)): \(focus)", value: $focus, in: 1...180)
                    Stepper("\(strings.text(.shortBreakMinutes)): \(shortBreak)", value: $shortBreak, in: 1...60)
                    Stepper("\(strings.text(.longBreakMinutes)): \(longBreak)", value: $longBreak, in: 1...60)
                    Stepper(
                        "\(strings.text(.blocksUntilLongBreak)): \(blocks)",
                        value: $blocks,
                        in: 2...8
                    )
                }
                Section(strings.text(.appearance)) {
                    VStack(alignment: .leading, spacing: 10) {
                        Text(strings.text(.accent)).font(.subheadline)
                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack(spacing: 12) {
                                ForEach(0..<6, id: \.self) { index in
                                    AccentSwatch(
                                        color: Color.ticklyAccents[index],
                                        name: strings.accentName(index),
                                        selected: accent == index,
                                        selectedValue: strings.spanish ? "Seleccionado" : "Selected"
                                    ) { accent = index }
                                }
                            }
                            .padding(.vertical, 2)
                        }
                    }
                    Toggle(strings.text(.animatedBackground), isOn: $animated)
                }
                Section(strings.text(.sounds)) {
                    Picker(strings.text(.sounds), selection: $sound) {
                        Text(strings.text(.silence)).tag(-1)
                        Text(strings.text(.soundOne)).tag(0)
                        Text(strings.text(.soundTwo)).tag(1)
                        Text(strings.text(.soundThree)).tag(2)
                    }
                    // Form pickers bridge to UIKit. Recreate their presentation
                    // identity with the draft accent so a cached chevron/value
                    // cannot retain the previously saved tint.
                    .tint(draftAccent)
                    .id(accent)
                    if sound >= 0 {
                        Button(strings.text(.preview)) { onPreviewSound(sound) }
                    }
                    Toggle(strings.text(.vibration), isOn: $vibration)
                    Text(strings.text(.vibrationSystemNote))
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                }
                Section {
                    Toggle(strings.text(.keepScreenOn), isOn: $keepScreenOn)
                    Picker(strings.text(.language), selection: $language) {
                        Text(strings.text(.system)).tag("system")
                        Text(strings.text(.spanish)).tag("es")
                        Text(strings.text(.english)).tag("en")
                    }
                    .tint(draftAccent)
                    .id(accent)
                }
            }
            .scrollContentBackground(.hidden)
            .background(Color.black)
            .navigationTitle(strings.text(.settings))
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button(strings.text(.done)) {
                        save()
                        dismiss()
                    }
                }
            }
            .onAppear { load() }
            .onDisappear { save() }
        }
        .preferredColorScheme(.dark)
        // The sheet has its own presentation hierarchy. Apply the draft tint here so
        // standard controls preview the selected accent before settings are saved.
        .tint(draftAccent)
    }

    private func load() {
        let currentSettings = settings
        focus = Int(currentSettings.focusMinutes)
        shortBreak = Int(currentSettings.shortBreakMinutes)
        longBreak = Int(currentSettings.longBreakMinutes)
        blocks = Int(currentSettings.blocksUntilLongBreak)
        accent = Int(currentSettings.accentIndex)
        sound = Int(currentSettings.soundIndex)
        vibration = currentSettings.vibrationEnabled
        animated = currentSettings.animatedBackground
        keepScreenOn = currentSettings.keepScreenOn
        language = currentSettings.language
    }

    private func save() {
        onSave(
            TimerSettings(
                focusMinutes: Int32(focus),
                shortBreakMinutes: Int32(shortBreak),
                longBreakMinutes: Int32(longBreak),
                blocksUntilLongBreak: Int32(blocks),
                accentIndex: Int32(accent),
                soundIndex: Int32(sound),
                vibrationEnabled: vibration,
                animatedBackground: animated,
                keepScreenOn: keepScreenOn,
                language: language
            )
        )
    }
}
