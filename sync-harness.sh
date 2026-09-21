#!/usr/bin/env bash
set -e

# Detect platform slice
OS="$(uname -s | tr '[:upper:]' '[:lower:]')"
ARCH="$(uname -m)"

case "$OS" in
  darwin)
    OS_SLICE="osx"
    PLATFORM_PATTERN="macosx"
    ;;
  linux)
    OS_SLICE="linux"
    PLATFORM_PATTERN="manylinux"
    ;;
  msys*|mingw*|cygwin*)
    OS_SLICE="windows"
    PLATFORM_PATTERN="win"
    ;;
  *)
    echo "Unsupported OS: $OS"
    exit 1
    ;;
esac

case "$ARCH" in
  arm64|aarch64)
    ARCH_SLICE="aarch64"
    ARCH_PATTERN="arm64"
    ;;
  x86_64|amd64)
    ARCH_SLICE="x86_64"
    ARCH_PATTERN="x86_64"
    ;;
  *)
    echo "Unsupported Arch: $ARCH"
    exit 1
    ;;
esac

SLICE="${OS_SLICE}-${ARCH_SLICE}"
echo "Target platform slice: $SLICE"

TARGET_DIR="./src/main/resources/google/antigravity/bin/$SLICE"
mkdir -p "$TARGET_DIR"

BINARY_NAME="localharness"
if [ "$OS_SLICE" = "windows" ]; then
  BINARY_NAME="localharness.exe"
fi

if [ -f "$TARGET_DIR/$BINARY_NAME" ]; then
  echo "Binary already present at $TARGET_DIR/$BINARY_NAME"
  exit 0
fi

echo "Fetching latest package metadata from PyPI..."
PACKAGE_INFO=$(curl -sSL https://pypi.org/pypi/google-antigravity/json)

WHEEL_URL=$(echo "$PACKAGE_INFO" | jq -r --arg plt "$PLATFORM_PATTERN" --arg arc "$ARCH_PATTERN" \
  '.urls[] | select(.filename | contains($plt) and contains($arc)) | .url' | head -n 1)

if [ -z "$WHEEL_URL" ] || [ "$WHEEL_URL" = "null" ]; then
  echo "Error: Could not find wheel matching $PLATFORM_PATTERN and $ARCH_PATTERN"
  exit 1
fi

TMP_WHEEL="/tmp/antigravity_wheel_${SLICE}.whl"
echo "Downloading wheel from $WHEEL_URL..."
curl -sSL -o "$TMP_WHEEL" "$WHEEL_URL"

echo "Extracting $BINARY_NAME into $TARGET_DIR..."
unzip -p "$TMP_WHEEL" "google/antigravity/bin/$BINARY_NAME" > "$TARGET_DIR/$BINARY_NAME"
chmod +x "$TARGET_DIR/$BINARY_NAME"
rm -f "$TMP_WHEEL"

echo "Done! $BINARY_NAME successfully installed to $TARGET_DIR"
