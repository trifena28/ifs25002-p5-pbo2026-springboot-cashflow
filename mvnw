#!/bin/sh
set -e

BASE_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
PROPS_FILE="$BASE_DIR/.mvn/wrapper/maven-wrapper.properties"

DIST_URL=$(grep '^distributionUrl=' "$PROPS_FILE" | cut -d'=' -f2- | tr -d '\r')
ZIP_NAME=$(basename "$DIST_URL")
MVN_DIR_NAME=$(echo "$ZIP_NAME" | sed 's/-bin\.zip$//')

WRAPPER_DISTS="$HOME/.m2/wrapper/dists/$MVN_DIR_NAME"
MVN_BIN="$WRAPPER_DISTS/$MVN_DIR_NAME/bin/mvn"

if [ ! -x "$MVN_BIN" ]; then
  mkdir -p "$WRAPPER_DISTS"
  TMP_ZIP="$WRAPPER_DISTS/$ZIP_NAME"
  echo "Downloading Maven from $DIST_URL ..."
  if command -v curl >/dev/null 2>&1; then
    curl -fsSL "$DIST_URL" -o "$TMP_ZIP"
  elif command -v wget >/dev/null 2>&1; then
    wget -q "$DIST_URL" -O "$TMP_ZIP"
  else
    java -e "
      import java.net.*; import java.nio.file.*;
      public class Dl { public static void main(String[] a) throws Exception {
        try (var in = URI.create(a[0]).toURL().openStream()) {
          Files.copy(in, Path.of(a[1]), StandardCopyOption.REPLACE_EXISTING);
        }
      }}
    " "$DIST_URL" "$TMP_ZIP"
  fi

  if command -v unzip >/dev/null 2>&1; then
    unzip -q -o "$TMP_ZIP" -d "$WRAPPER_DISTS"
  else
    (cd "$WRAPPER_DISTS" && jar xf "$TMP_ZIP")
  fi
  rm -f "$TMP_ZIP"
  chmod +x "$MVN_BIN"
fi

exec "$MVN_BIN" "$@"