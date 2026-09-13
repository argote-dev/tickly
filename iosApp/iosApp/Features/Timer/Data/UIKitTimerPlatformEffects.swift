import AudioToolbox
import UIKit

final class UIKitTimerPlatformEffects: TimerPlatformEffects {
    func setScreenIdleDisabled(_ disabled: Bool) { UIApplication.shared.isIdleTimerDisabled = disabled }
    func vibrate() { AudioServicesPlaySystemSound(kSystemSoundID_Vibrate) }
}
