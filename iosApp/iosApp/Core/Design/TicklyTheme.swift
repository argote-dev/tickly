import SwiftUI

extension Color {
    static let ticklyAccents: [Color] = [
        Color(red: 0.55, green: 0.60, blue: 1.0), Color(red: 0.30, green: 0.86, blue: 1.0),
        Color(red: 0.34, green: 0.95, blue: 0.72), Color(red: 0.75, green: 0.52, blue: 1.0),
        Color(red: 1.0, green: 0.70, blue: 0.28), Color(red: 1.0, green: 0.48, blue: 0.70)
    ]

    static func ticklyAccent(at index: Int) -> Color {
        ticklyAccents[min(max(index, 0), ticklyAccents.count - 1)]
    }
}
