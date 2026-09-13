import Foundation

/// Platform-free boundaries consumed by timer presentation.
protocol TimerSnapshotRepository {
    func readSnapshot() -> String?
    func writeSnapshot(_ snapshot: String)
}
