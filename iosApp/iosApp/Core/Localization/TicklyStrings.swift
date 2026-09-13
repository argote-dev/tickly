import Foundation

struct TicklyStrings {
    let spanish: Bool

    init(language: String) {
        switch language {
        case "es": spanish = true
        case "en": spanish = false
        default: spanish = Locale.preferredLanguages.first?.hasPrefix("es") == true
        }
    }

    func text(_ key: Key) -> String { spanish ? key.es : key.en }
    func accentName(_ index: Int) -> String {
        let spanishNames = ["Índigo", "Cian", "Menta", "Violeta", "Ámbar", "Rosa"]
        let englishNames = ["Indigo", "Cyan", "Mint", "Violet", "Amber", "Pink"]
        return (spanish ? spanishNames : englishNames)[max(0, min(index, 5))]
    }

    enum Key {
        case focus, shortBreak, longBreak, start, pause, restart, skip, reset
        case settings, done, intervals, focusMinutes, shortBreakMinutes, longBreakMinutes
        case blocksUntilLongBreak, appearance, accent, sounds, silence, vibration
        case animatedBackground, keepScreenOn, language, system, spanish, english
        case notificationsDisabled, notificationsExplanation, allowNotifications, cancel
        case restartTitle, restartMessage, resetTitle, resetMessage, confirm, blocks
        case minutes, soundOne, soundTwo, soundThree, preview, timer, startBreak, startFocus, abandonTitle, abandonMessage, notificationsUnavailable, vibrationSystemNote

        var es: String {
            switch self {
            case .focus: "Enfoque"; case .shortBreak: "Descanso corto"; case .longBreak: "Descanso largo"
            case .start: "Iniciar"; case .pause: "Pausar"; case .restart: "Reiniciar"; case .skip: "Saltar"; case .reset: "Empezar de cero"
            case .settings: "Ajustes"; case .done: "Listo"; case .intervals: "Intervalos"; case .focusMinutes: "Enfoque (min)"
            case .shortBreakMinutes: "Descanso corto (min)"; case .longBreakMinutes: "Descanso largo (min)"; case .blocksUntilLongBreak: "Bloques hasta descanso largo"
            case .appearance: "Apariencia"; case .accent: "Acento"; case .sounds: "Avisos"; case .silence: "Silencio"; case .vibration: "Vibración"
            case .animatedBackground: "Fondo animado"; case .keepScreenOn: "Mantener pantalla encendida en enfoque"; case .language: "Idioma"
            case .system: "Sistema"; case .spanish: "Español"; case .english: "English"; case .notificationsDisabled: "Avisos desactivados"
            case .notificationsExplanation: "Activa los avisos para recibir un recordatorio discreto cuando termine el intervalo."; case .allowNotifications: "Permitir avisos"
            case .cancel: "Cancelar"; case .restartTitle: "¿Reiniciar intervalo?"; case .restartMessage: "El intervalo volverá a empezar. Tu avance se conservará."
            case .resetTitle: "¿Empezar de cero?"; case .resetMessage: "Se borrará el avance de esta tanda y volverás a enfoque."; case .confirm: "Confirmar"
            case .abandonTitle: "¿Abandonar enfoque?"; case .abandonMessage: "Este enfoque no contará para tu descanso largo."; case .notificationsUnavailable: "Los avisos están desactivados"; case .vibrationSystemNote: "iOS controla la vibración de avisos mientras la app está en segundo plano."; case .blocks: "bloques"; case .startBreak: "Iniciar descanso"; case .startFocus: "Iniciar enfoque"; case .minutes: "min"; case .soundOne: "Brisa"; case .soundTwo: "Gota"; case .soundThree: "Campana"; case .preview: "Previsualizar"; case .timer: "Temporizador"
            }
        }
        var en: String {
            switch self {
            case .focus: "Focus"; case .shortBreak: "Short break"; case .longBreak: "Long break"
            case .start: "Start"; case .pause: "Pause"; case .restart: "Restart"; case .skip: "Skip"; case .reset: "Start over"
            case .settings: "Settings"; case .done: "Done"; case .intervals: "Intervals"; case .focusMinutes: "Focus (min)"
            case .shortBreakMinutes: "Short break (min)"; case .longBreakMinutes: "Long break (min)"; case .blocksUntilLongBreak: "Focus blocks until long break"
            case .appearance: "Appearance"; case .accent: "Accent"; case .sounds: "Alerts"; case .silence: "Silence"; case .vibration: "Vibration"
            case .animatedBackground: "Animated background"; case .keepScreenOn: "Keep screen on during focus"; case .language: "Language"
            case .system: "System"; case .spanish: "Español"; case .english: "English"; case .notificationsDisabled: "Notifications off"
            case .notificationsExplanation: "Enable notifications for a gentle reminder when an interval ends."; case .allowNotifications: "Allow notifications"
            case .cancel: "Cancel"; case .restartTitle: "Restart interval?"; case .restartMessage: "The interval will start over. Your progress will be kept."
            case .resetTitle: "Start over?"; case .resetMessage: "This clears the current set’s progress and returns to focus."; case .confirm: "Confirm"
            case .abandonTitle: "Abandon focus?"; case .abandonMessage: "This focus block will not count toward your long break."; case .notificationsUnavailable: "Notifications are turned off"; case .vibrationSystemNote: "iOS controls notification vibration while the app is in the background."; case .blocks: "blocks"; case .startBreak: "Start break"; case .startFocus: "Start focus"; case .minutes: "min"; case .soundOne: "Breeze"; case .soundTwo: "Drop"; case .soundThree: "Bell"; case .preview: "Preview"; case .timer: "Timer"
            }
        }
    }
}
