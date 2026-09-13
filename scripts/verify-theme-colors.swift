import AppKit
import Foundation

// Focused visual regression check, not a general UI test: full-screen portrait
// home captures from iPhone 17 Pro / Android Medium Phone at default text size,
// with no permission banner. Compare the primary accent to Settings (iOS) or
// the restart tonal button (Android). Neutral labels and black on-accent icons
// are intentionally excluded. No image is modified.
// Usage: swift scripts/verify-theme-colors.swift screenshot.png ios|android
let arguments = CommandLine.arguments
guard arguments.count == 3, ["ios", "android"].contains(arguments[2]) else {
    print("Usage: swift scripts/verify-theme-colors.swift screenshot.png ios|android")
    exit(2)
}
guard let data = try? Data(contentsOf: URL(fileURLWithPath: arguments[1])),
      let bitmap = NSBitmapImageRep(data: data) else {
    print("FAIL: cannot read screenshot")
    exit(2)
}
let isIOS = arguments[2] == "ios"

func averageHue(in rectangle: [Double]) -> Double {
    var cosineSum = 0.0
    var sineSum = 0.0
    var sampleCount = 0
    let xRange = Int(Double(bitmap.pixelsWide) * rectangle[0])..<Int(Double(bitmap.pixelsWide) * rectangle[2])
    let yRange = Int(Double(bitmap.pixelsHigh) * rectangle[1])..<Int(Double(bitmap.pixelsHigh) * rectangle[3])
    for y in yRange {
        for x in xRange {
            guard let color = bitmap.colorAt(x: x, y: y)?.usingColorSpace(.deviceRGB),
                  color.saturationComponent > 0.18,
                  color.brightnessComponent > (isIOS ? 0.6 : 0.2) else { continue }
            let angle = Double(color.hueComponent) * 2 * Double.pi
            cosineSum += cos(angle)
            sineSum += sin(angle)
            sampleCount += 1
        }
    }
    guard sampleCount > 5 else {
        print("FAIL: no chromatic samples; verify the expected screen/layout")
        exit(1)
    }
    return (atan2(sineSum, cosineSum) / (2 * Double.pi) + 1).truncatingRemainder(dividingBy: 1)
}

let accent = averageHue(in: isIOS ? [0.16, 0.63, 0.84, 0.68] : [0.15, 0.61, 0.85, 0.65])
let control = averageHue(in: isIOS ? [0.85, 0.10, 0.93, 0.125] : [0.07, 0.69, 0.47, 0.72])
let distance = min(abs(accent - control), 1 - abs(accent - control))
let passed = distance < 0.10
print(String(format: "%@ accent hue %.0f°, control hue %.0f°, difference %.0f°: %@",
             arguments[2], accent * 360, control * 360, distance * 360, passed ? "PASS" : "FAIL"))
exit(passed ? 0 : 1)
