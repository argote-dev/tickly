import Foundation
import UserNotifications

final class TicklyNotifications: NSObject, UNUserNotificationCenterDelegate {
    static let shared = TicklyNotifications()
    private let center = UNUserNotificationCenter.current()
    private let identifier = "tickly.interval.finished"

    private override init() {
        super.init()
        center.delegate = self
    }

    func userNotificationCenter(_: UNUserNotificationCenter, willPresent _: UNNotification) async -> UNNotificationPresentationOptions {
        [.banner, .sound]
    }

    func requestPermissionIfNeeded() async -> Bool {
        let settings = await center.notificationSettings()
        guard settings.authorizationStatus == .notDetermined else {
            return settings.authorizationStatus == .authorized || settings.authorizationStatus == .provisional
        }
        do { return try await center.requestAuthorization(options: [.alert, .sound, .badge]) }
        catch { return false }
    }

    func cancel() { center.removePendingNotificationRequests(withIdentifiers: [identifier]) }

    func isAuthorized() async -> Bool {
        let settings = await center.notificationSettings()
        return settings.authorizationStatus == .authorized || settings.authorizationStatus == .provisional
    }

    func schedule(after seconds: TimeInterval, title: String, body: String, soundIndex: Int) {
        cancel()
        guard seconds > 0 else { return }
        let content = UNMutableNotificationContent()
        content.title = title
        content.body = body
        content.sound = soundIndex < 0 ? nil : UNNotificationSound(named: UNNotificationSoundName("tickly-\(soundIndex).caf"))
        let trigger = UNTimeIntervalNotificationTrigger(timeInterval: max(1, seconds), repeats: false)
        center.add(UNNotificationRequest(identifier: identifier, content: content, trigger: trigger))
    }
}
