#!/usr/bin/env bash
# Sumi quality gate — iOS compile + Android compile + detekt.
# Run this before marking a task complete. All three must pass.
#
# Usage:  ./qa.sh

set -e

./gradlew \
  :composeApp:compileKotlinIosSimulatorArm64 \
  :composeApp:compileAndroidMain \
  :composeApp:detekt
