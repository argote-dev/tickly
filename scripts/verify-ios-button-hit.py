#!/usr/bin/env python3
"""Probe the full hit area of Tickly's iOS primary timer capsule.

Requirements: macOS with the Orca CLI, an iPhone 17 Pro simulator running
iOS 26.5, and Tickly open on its current portrait Spanish paused home screen
(413 x 885 window).  The fixed coordinate (90, 600) is deliberately in the
left side of the visible primary capsule, not on its text.  The probe changes
the timer by a few seconds while starting and pausing it; it does not reset
the timer or change any settings.

This is a focused simulator regression probe, not a device-independent UI
test. Run it manually with: python3 scripts/verify-ios-button-hit.py
"""

import json
import re
import subprocess
import sys


ORCA = ["orca", "computer"]
APP = ["--app", "com.apple.iphonesimulator"]
CAPSULE_POINT = ("90", "600")


def tree() -> str:
    result = subprocess.check_output(
        ORCA + ["get-app-state"] + APP + ["--json", "--no-screenshot"],
        text=True,
    )
    return json.loads(result)["result"]["snapshot"]["treeText"]


def tap_capsule() -> None:
    subprocess.check_output(
        ORCA
        + ["click"]
        + APP
        + ["--x", CAPSULE_POINT[0], "--y", CAPSULE_POINT[1], "--json", "--no-screenshot"],
        text=True,
    )


def pause_semantically(snapshot: str) -> None:
    match = re.search(r"\n\s*(\d+) button Pausar,", snapshot)
    if not match:
        raise AssertionError("Cannot find the Pausar accessibility button for cleanup")
    subprocess.check_output(
        ORCA
        + ["click"]
        + APP
        + ["--element-index", match.group(1), "--json", "--no-screenshot"],
        text=True,
    )


def main() -> int:
    before = tree()
    assert "button Iniciar," in before, "Precondition failed: Spanish paused timer is required"

    tap_capsule()
    running = tree()
    if "button Pausar," not in running:
        print("FAIL: left side of primary capsule did not start the timer")
        return 1

    # Repeat exactly the same lateral tap; this must pause without relying on
    # the label's smaller hit target.
    tap_capsule()
    after_lateral_pause = tree()
    if "button Iniciar," in after_lateral_pause:
        print("PASS: lateral capsule tap starts and pauses the timer")
        return 0

    # Leave the simulator paused even when the regression is detected, so the
    # next invocation has the documented precondition. Semantic pause is only
    # cleanup; the failed lateral interaction still returns a failing status.
    pause_semantically(after_lateral_pause)
    assert "button Iniciar," in tree(), "Semantic fallback failed to restore paused state"
    print("FAIL: lateral capsule tap started but did not pause the timer; restored state semantically")
    return 1


if __name__ == "__main__":
    try:
        sys.exit(main())
    except (AssertionError, subprocess.CalledProcessError, KeyError, json.JSONDecodeError) as error:
        print(f"FAIL: {error}", file=sys.stderr)
        sys.exit(1)
