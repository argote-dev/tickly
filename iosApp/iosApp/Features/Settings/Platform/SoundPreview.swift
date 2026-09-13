import AudioToolbox
import Foundation

/// Plays an in-app completion sound without affecting the timer lifecycle.
enum SoundPreview {
    static func play(_ index: Int) {
        guard let url = Bundle.main.url(forResource: "tickly-\(index)", withExtension: "caf") else { return }
        var sound: SystemSoundID = 0
        AudioServicesCreateSystemSoundID(url as CFURL, &sound)
        AudioServicesPlaySystemSound(sound)
        DispatchQueue.main.asyncAfter(deadline: .now() + 1) {
            AudioServicesDisposeSystemSoundID(sound)
        }
    }
}
