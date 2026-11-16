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

  TMP_DL=$(mktemp -d)
  cat << 'EOF' > "$TMP_DL/MvnDownloader.java"
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.zip.*;

public class MvnDownloader {
    public static void main(String[] args) throws Exception {
        Path zipPath = Path.of(args[1]);
        Path destDir = Path.of(args[2]);
        try (InputStream in = URI.create(args[0]).toURL().openStream()) {
            Files.copy(in, zipPath, StandardCopyOption.REPLACE_EXISTING);
        }
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipPath))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path outPath = destDir.resolve(entry.getName()).normalize();
                if (entry.isDirectory()) {
                    Files.createDirectories(outPath);
                } else {
                    Files.createDirectories(outPath.getParent());
                    Files.copy(zis, outPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }
}
EOF

  java "$TMP_DL/MvnDownloader.java" "$DIST_URL" "$TMP_ZIP" "$WRAPPER_DISTS"
  rm -rf "$TMP_DL" "$TMP_ZIP"
  chmod +x "$MVN_BIN"
fi

exec "$MVN_BIN" "$@"