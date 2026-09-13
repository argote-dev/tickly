import Foundation

final class UserDefaultsTimerSnapshotRepository: TimerSnapshotRepository {
    private let defaults: UserDefaults
    private let key = "tickly.timer.snapshot"

    init(defaults: UserDefaults = .standard) { self.defaults = defaults }
    func readSnapshot() -> String? { defaults.string(forKey: key) }
    func writeSnapshot(_ snapshot: String) { defaults.set(snapshot, forKey: key) }
}
