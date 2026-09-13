import AudioToolbox
import Foundation

/// Native adapter that plays an in-app completion sound without timer lifecycle effects.
final class SoundPreview: SoundPreviewing {
    func preview(soundIndex index: Int) {
        guard let url = Bundle.main.url(forResource: "tickly-\(index)", withExtension: "caf") else { return }
        var sound: SystemSoundID = 0
        AudioServicesCreateSystemSoundID(url as CFURL, &sound)
        AudioServicesPlaySystemSound(sound)
        DispatchQueue.main.asyncAfter(deadline: .now() + 1) {
            AudioServicesDisposeSystemSoundID(sound)
        }
    }
}
