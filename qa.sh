#!/usr/bin/env bash
# Sumi quality gate — iOS compile + Android compile + detekt + snapshot verify.
# Run this before marking a task complete. All four must pass.
#
# If the snapshot step fails after a deliberate visual change, look at the diff PNGs in
# composeApp/build/outputs/roborazzi/, then re-record and commit the new baselines:
#
#   ./gradlew :composeApp:recordRoborazziAndroidHostTest
#
# Usage:  ./qa.sh

set -e

./gradlew \
  :composeApp:compileKotlinIosSimulatorArm64 \
  :composeApp:compileAndroidMain \
  :composeApp:detekt \
  :composeApp:verifyRoborazziAndroidHostTest
