#!/bin/sh
# Maven Wrapper script
# Downloads Maven if not present and runs it.
# Based on the official Maven Wrapper concept.

set -e

MAVEN_VERSION="3.9.6"
MAVEN_HOME="${HOME}/.m2/wrapper/dists/apache-maven-${MAVEN_VERSION}"
MAVEN_URL="https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz"

if [ ! -f "${MAVEN_HOME}/bin/mvn" ]; then
    echo "Maven ${MAVEN_VERSION} not found. Downloading..."
    mkdir -p "${MAVEN_HOME}"
    curl -fsSL "${MAVEN_URL}" | tar xz -C "${MAVEN_HOME}" --strip-components=1
    echo "Maven ${MAVEN_VERSION} installed to ${MAVEN_HOME}"
fi

exec "${MAVEN_HOME}/bin/mvn" "$@"
