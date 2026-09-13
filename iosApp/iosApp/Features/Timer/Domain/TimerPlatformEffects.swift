import Foundation

protocol TimerPlatformEffects {
    func setScreenIdleDisabled(_ disabled: Bool)
    func vibrate()
}
