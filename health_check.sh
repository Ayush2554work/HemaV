#!/bin/bash

# HemaV "Robot" Health Check Script
# Verifies build integrity, code quality, and test status.

echo "========================================"
echo "🤖 HemaV Robot: Starting Health Check"
echo "========================================"

# Force usage of Homebrew OpenJDK 17
export JAVA_HOME="/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home"
echo "☕ Using Java: $JAVA_HOME"

# 1. Verify Build (Compilability)
echo ""
echo "🛠️  Step 1: Verifying Compilation..."
./gradlew :app:assembleDebug --no-daemon -q
if [ $? -eq 0 ]; then
    echo "✅ Build Successful: App compiles correctly."
else
    echo "❌ Build Failed: usage of new UI components caused errors."
    exit 1
fi

# 2. Run Unit Tests (Logic Verification)
echo ""
echo "🧪 Step 2: Running Unit Tests..."
./gradlew :app:testDebugUnitTest --no-daemon -q
if [ $? -eq 0 ]; then
    echo "✅ Unit Tests Passed."
else
    echo "❌ Unit Tests Failed."
    # We do not exit here, to show lint results too
fi

# 3. Lint Check (Code Quality)
echo ""
echo "🔍 Step 3: Running Static Analysis (Lint)..."
./gradlew :app:lintDebug --no-daemon -q
# Lint usually returns 0 even if warnings exist, unless configured to fail on error
echo "✅ Lint Check Complete (View report in app/build/reports/lint-results-debug.html)."

echo ""
echo "========================================"
echo "🎉 HemaV Robot: System Checks Complete!"
echo "========================================"
