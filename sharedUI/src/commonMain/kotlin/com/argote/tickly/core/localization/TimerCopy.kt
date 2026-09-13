package com.argote.tickly.core.localization

/** Localized copy owned by the timer and settings presentation slices. */
data class TimerCopy(
    val focus: String,
    val shortBreak: String,
    val longBreak: String,
    val remaining: String,
    val finished: String,
    val ready: String,
    val blocks: String,
    val start: String,
    val pause: String,
    val restart: String,
    val skip: String,
    val skipFocus: String,
    val startOver: String,
    val settings: String,
    val done: String,
    val focusDuration: String,
    val shortDuration: String,
    val longDuration: String,
    val longAfter: String,
    val accent: String,
    val sound: String,
    val silent: String,
    val soundOne: String,
    val soundTwo: String,
    val soundThree: String,
    val vibration: String,
    val animatedBackground: String,
    val keepScreenOn: String,
    val language: String,
    val system: String,
    val confirm: String,
    val cancel: String,
    val previewSound: String,
    val restartMessage: String,
    val skipMessage: String,
    val resetMessage: String,
) {
    companion object {
        fun forLanguage(language: String) = if (language == "es") es else en

        val en = TimerCopy(
            "Focus", "Short break", "Long break", "Remaining", "Finished", "Ready", "Focus blocks",
            "Start", "Pause", "Restart", "Skip", "Skip focus", "Start over", "Settings", "Done",
            "Focus duration", "Short break duration", "Long break duration", "Long break after", "Accent",
            "Sound", "Silent", "Calm bell", "Soft chime", "Warm tone", "Vibration", "Animated background",
            "Keep screen on during focus", "Language", "System", "Confirm", "Cancel", "Preview sound",
            "Restart this interval?", "This focus block will not count. A short break will be offered.",
            "Clear completed blocks and return to focus?",
        )
        val es = TimerCopy(
            "Enfoque", "Descanso corto", "Descanso largo", "Restante", "Finalizado", "Listo",
            "Bloques de enfoque", "Iniciar", "Pausar", "Reiniciar", "Saltar", "Saltar enfoque",
            "Empezar de cero", "Ajustes", "Listo", "Duración de enfoque", "Duración descanso corto",
            "Duración descanso largo", "Descanso largo después de", "Acento", "Sonido", "Silencio",
            "Campana suave", "Campanilla", "Tono cálido", "Vibración", "Fondo animado",
            "Mantener pantalla encendida en enfoque", "Idioma", "Sistema", "Confirmar", "Cancelar",
            "Previsualizar sonido", "¿Reiniciar este intervalo?",
            "Este bloque no contará. Se ofrecerá un descanso corto.",
            "¿Borrar bloques completados y volver a enfoque?",
        )
    }
}
