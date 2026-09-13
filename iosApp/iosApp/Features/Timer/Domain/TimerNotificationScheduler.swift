import Foundation

protocol TimerNotificationScheduler {
    func requestPermissionIfNeeded() async -> Bool
    func isAuthorized() async -> Bool
    func needsPermissionExplanation() async -> Bool
    func isDenied() async -> Bool
    func cancel()
    func schedule(after seconds: TimeInterval, title: String, body: String, soundIndex: Int)
}
