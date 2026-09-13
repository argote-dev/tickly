import Foundation
import Combine
import SharedLogic
import UIKit
import SwiftUI
import AudioToolbox

@MainActor
final class TimerStore: ObservableObject {
    @Published private(set) var engine: TimerEngine
    @Published private(set) var remainingMillis: Int64 = 0
    @Published var showNotificationExplanation = false
    @Published private(set) var notificationsUnavailable = false
    @Published private(set) var isSceneActive = true

    private let snapshotKey = "tickly.timer.snapshot"
    private var ticker: AnyCancellable?
    private var lastSavedSnapshot: String?
    private var notificationGeneration = 0

    init() {
        engine = TimerEngine(snapshot: UserDefaults.standard.string(forKey: snapshotKey))
        lastSavedSnapshot = engine.serialize()
        refresh()
        refreshNotificationAvailability()
        startTicker()
    }

    var strings: TicklyStrings { TicklyStrings(language: engine.settings.language) }
    var accent: ColorProxy { ColorProxy(index: Int(engine.settings.accentIndex)) }
    var isRunning: Bool { engine.status == .running }
    var phaseName: String {
        switch engine.phase { case .focus: strings.text(.focus); case .shortBreak: strings.text(.shortBreak); default: strings.text(.longBreak) }
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
        let now = epochMillis()
        engine.start(nowMillis: now)
        publish()
        persist()
        refreshNotification()
        Task {
            let notificationSettings = await UNUserNotificationCenter.current().notificationSettings()
            if notificationSettings.authorizationStatus == .notDetermined {
                showNotificationExplanation = true
            }
        }
    }

    func pause() {
        engine.pause(nowMillis: epochMillis())
        publish()
        persist()
        invalidateScheduledNotification()
        updateScreenIdleTimer()
    }

    func restart() {
        engine.restart(nowMillis: epochMillis())
        publish()
        persist()
        refreshNotification()
    }

    func skip() {
        engine.skip(nowMillis: epochMillis())
        publish()
        persist()
        invalidateScheduledNotification()
        updateScreenIdleTimer()
    }

    func reset() {
        engine.reset(nowMillis: epochMillis())
        publish()
        persist()
        invalidateScheduledNotification()
        updateScreenIdleTimer()
    }

    func apply(settings: TimerSettings) {
        engine.updateSettings(settings: settings, nowMillis: epochMillis())
        publish()
        persist()
        refreshNotification()
    }

    func requestNotifications() {
        Task {
            let granted = await TicklyNotifications.shared.requestPermissionIfNeeded()
            if granted { showNotificationExplanation = false; refreshNotification() }
            refreshNotificationAvailability()
        }
    }

    func refresh(allowVibration: Bool = false) {
        let now = epochMillis()
        let beforeTick = engine.serialize()
        engine.tick(nowMillis: now)
        if engine.serialize() != beforeTick {
            publish()
            if allowVibration && engine.status == .finished && engine.settings.vibrationEnabled {
                AudioServicesPlaySystemSound(kSystemSoundID_Vibrate)
            }
        }
        remainingMillis = engine.remainingMillis(nowMillis: now)
        persist()
        updateScreenIdleTimer()
    }

    func onScenePhase(_ phase: ScenePhase) {
        isSceneActive = phase == .active
        if phase == .active {
            startTicker()
            refresh(allowVibration: false)
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
        TicklyNotifications.shared.cancel()
        guard engine.status == .running else { updateScreenIdleTimer(); return }
        let deadline = engine.deadlineMillis
        let phaseText = phaseName
        let finished = strings.spanish ? "Tu intervalo de \(phaseText.lowercased()) terminó." : "Your \(phaseText.lowercased()) interval has ended."
        let soundIndex = Int(engine.settings.soundIndex)
        Task { [weak self] in
            guard let self, await TicklyNotifications.shared.isAuthorized() else { return }
            guard self.notificationGeneration == generation,
                  self.engine.status == .running,
                  self.engine.deadlineMillis == deadline else { return }
            let remainingSeconds = Double(self.engine.remainingMillis(nowMillis: self.epochMillis())) / 1_000
            TicklyNotifications.shared.schedule(
                after: remainingSeconds,
                title: "Tickly · \(phaseText)",
                body: finished,
                soundIndex: soundIndex
            )
        }
        updateScreenIdleTimer()
    }

    private func invalidateScheduledNotification() {
        notificationGeneration &+= 1
        TicklyNotifications.shared.cancel()
    }

    private func refreshNotificationAvailability() {
        Task {
            let notificationSettings = await UNUserNotificationCenter.current().notificationSettings()
            notificationsUnavailable = notificationSettings.authorizationStatus == .denied
        }
    }

    private func updateScreenIdleTimer() {
        UIApplication.shared.isIdleTimerDisabled = isSceneActive && isRunning && engine.phase == .focus && engine.settings.keepScreenOn
    }

    private func persist() {
        let snapshot = engine.serialize()
        guard snapshot != lastSavedSnapshot else { return }
        UserDefaults.standard.set(snapshot, forKey: snapshotKey)
        lastSavedSnapshot = snapshot
    }
    private func publish() { objectWillChange.send() }
    private func epochMillis() -> Int64 { Int64(Date().timeIntervalSince1970 * 1_000) }
}

struct ColorProxy { let index: Int }
