import Combine
import Foundation
import SharedLogic
import SwiftUI

@MainActor
final class TimerStore: ObservableObject {
    @Published private(set) var engine: TimerEngine
    @Published private(set) var remainingMillis: Int64 = 0
    @Published var showNotificationExplanation = false
    @Published private(set) var notificationsUnavailable = false
    @Published private(set) var isSceneActive = true

    private let snapshots: TimerSnapshotRepository
    private let notifications: TimerNotificationScheduler
    private let effects: TimerPlatformEffects
    private let now: () -> Int64
    private var ticker: AnyCancellable?
    private var lastSavedSnapshot: String?
    private var notificationGeneration = 0

    init(snapshots: TimerSnapshotRepository, notifications: TimerNotificationScheduler, effects: TimerPlatformEffects, now: @escaping () -> Int64 = { Int64(Date().timeIntervalSince1970 * 1_000) }) {
        self.snapshots = snapshots
        self.notifications = notifications
        self.effects = effects
        self.now = now
        engine = TimerEngine(snapshot: snapshots.readSnapshot())
        lastSavedSnapshot = engine.serialize()
        refresh()
        refreshNotificationAvailability()
        startTicker()
    }

    var strings: TicklyStrings { TicklyStrings(language: engine.settings.language) }
    var accent: ColorProxy { ColorProxy(index: Int(engine.settings.accentIndex)) }
    var isRunning: Bool { engine.status == .running }
    var phaseName: String {
        switch engine.phase {
        case .focus: strings.text(.focus)
        case .shortBreak: strings.text(.shortBreak)
        default: strings.text(.longBreak)
        }
    }
    var primaryLabel: String {
        if isRunning { return strings.text(.pause) }
        guard engine.status == .finished else { return strings.text(.start) }
        return engine.phase == .focus ? strings.text(.startBreak) : strings.text(.startFocus)
    }
    var progress: Double {
        guard engine.intervalDurationMillis > 0 else { return 0 }
        return min(1, max(0, 1 - Double(remainingMillis) / Double(engine.intervalDurationMillis)))
    }
    var timeText: String {
        let seconds = max(0, Int((remainingMillis + 999) / 1_000))
        return String(format: "%02d:%02d", seconds / 60, seconds % 60)
    }

    func primaryAction() {
        if isRunning { pause() } else { start() }
    }
    func start() {
        engine.start(nowMillis: now())
        publish()
        persist()
        refreshNotification()
        Task { if await notifications.needsPermissionExplanation() { showNotificationExplanation = true } }
    }
    func pause() {
        engine.pause(nowMillis: now())
        publish()
        persist()
        invalidateScheduledNotification()
        updateScreenIdleTimer()
    }
    func restart() {
        engine.restart(nowMillis: now())
        publish()
        persist()
        refreshNotification()
    }
    func skip() {
        engine.skip(nowMillis: now())
        publish()
        persist()
        invalidateScheduledNotification()
        updateScreenIdleTimer()
    }
    func reset() {
        engine.reset(nowMillis: now())
        publish()
        persist()
        invalidateScheduledNotification()
        updateScreenIdleTimer()
    }
    func apply(settings: TimerSettings) {
        engine.updateSettings(settings: settings, nowMillis: now())
        publish()
        persist()
        refreshNotification()
    }
    func requestNotifications() {
        Task {
            if await notifications.requestPermissionIfNeeded() {
                showNotificationExplanation = false
                refreshNotification()
            }
            refreshNotificationAvailability()
        }
    }
    func refresh(allowVibration: Bool = false) {
        let sampledNow = now()
        let beforeTick = engine.serialize()
        engine.tick(nowMillis: sampledNow)
        if engine.serialize() != beforeTick {
            publish()
            if allowVibration && engine.status == .finished && engine.settings.vibrationEnabled {
                effects.vibrate()
            }
        }
        remainingMillis = engine.remainingMillis(nowMillis: sampledNow)
        persist()
        updateScreenIdleTimer()
    }
    func onScenePhase(_ phase: ScenePhase) {
        isSceneActive = phase == .active
        if phase == .active {
            startTicker()
            refresh()
            refreshNotificationAvailability()
            if isRunning { refreshNotification() }
        } else {
            ticker?.cancel()
            ticker = nil
            persist()
            updateScreenIdleTimer()
        }
    }
    private func startTicker() {
        guard ticker == nil else { return }
        ticker = Timer.publish(every: 0.25, on: .main, in: .common)
            .autoconnect()
            .sink { [weak self] _ in self?.refresh(allowVibration: true) }
    }
    private func refreshNotification() {
        notificationGeneration &+= 1
        let generation = notificationGeneration
        notifications.cancel()
        guard engine.status == .running else {
            updateScreenIdleTimer()
            return
        }
        let deadline = engine.deadlineMillis
        let phaseText = phaseName
        let finished = strings.spanish ? "Tu intervalo de \(phaseText.lowercased()) terminó." : "Your \(phaseText.lowercased()) interval has ended."
        let soundIndex = Int(engine.settings.soundIndex)
        Task { [weak self] in
            guard let self, await notifications.isAuthorized() else { return }
            guard notificationGeneration == generation, engine.status == .running, engine.deadlineMillis == deadline else { return }
            let remainingSeconds = Double(engine.remainingMillis(nowMillis: now())) / 1_000
            notifications.schedule(after: remainingSeconds, title: "Tickly · \(phaseText)", body: finished, soundIndex: soundIndex)
        }
        updateScreenIdleTimer()
    }
    private func invalidateScheduledNotification() {
        notificationGeneration &+= 1
        notifications.cancel()
    }
    private func refreshNotificationAvailability() {
        Task { notificationsUnavailable = await notifications.isDenied() }
    }
    private func updateScreenIdleTimer() {
        effects.setScreenIdleDisabled(isSceneActive && isRunning && engine.phase == .focus && engine.settings.keepScreenOn)
    }
    private func persist() {
        let snapshot = engine.serialize()
        guard snapshot != lastSavedSnapshot else { return }
        snapshots.writeSnapshot(snapshot)
        lastSavedSnapshot = snapshot
    }
    private func publish() { objectWillChange.send() }
}
