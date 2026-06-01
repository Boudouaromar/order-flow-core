#!/bin/sh
# ----------------------------------------------------------------------------
# Licensed to the Apache Software Foundation (ASF)
# Maven Wrapper startup script (no jar required)
# ----------------------------------------------------------------------------

set -e

MAVEN_WRAPPER_PROPERTIES=".mvn/wrapper/maven-wrapper.properties"
MAVEN_USER_HOME="${MAVEN_USER_HOME:-${HOME}/.m2}"
MAVEN_WRAPPER_CACHE="${MAVEN_USER_HOME}/wrapper"

# Read distributionUrl from properties
DISTRIBUTION_URL=$(grep "distributionUrl" "$MAVEN_WRAPPER_PROPERTIES" | sed 's/.*=//')
MAVEN_DIST_NAME=$(basename "$DISTRIBUTION_URL" .zip)
MAVEN_HOME="${MAVEN_WRAPPER_CACHE}/${MAVEN_DIST_NAME}"

# Download and unpack if not already cached
if [ ! -d "$MAVEN_HOME" ]; then
  echo "Downloading Maven: $DISTRIBUTION_URL"
  mkdir -p "$MAVEN_WRAPPER_CACHE"
  TMP_DOWNLOAD="${MAVEN_WRAPPER_CACHE}/tmp-download.zip"

  if command -v curl > /dev/null 2>&1; then
    curl -fsSL -o "$TMP_DOWNLOAD" "$DISTRIBUTION_URL"
  elif command -v wget > /dev/null 2>&1; then
    wget -q -O "$TMP_DOWNLOAD" "$DISTRIBUTION_URL"
  else
    echo "Error: curl or wget is required to download Maven"
    exit 1
  fi

  unzip -q "$TMP_DOWNLOAD" -d "$MAVEN_WRAPPER_CACHE"
  rm -f "$TMP_DOWNLOAD"
  echo "Maven extracted to $MAVEN_HOME"
fi

# Find java
if [ -n "$JAVA_HOME" ]; then
  JAVA_CMD="$JAVA_HOME/bin/java"
else
  JAVA_CMD="java"
fi

exec "$MAVEN_HOME/bin/mvn" "$@"
