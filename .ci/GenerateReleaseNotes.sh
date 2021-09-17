#!/usr/bin/env bash

# Retrieve version from pom.xml file.
readonly VERSION=$(mvn -q help:evaluate -Dexpression=project.version -DforceStdout)

# Retrieve changelog text for specific version: https://stackoverflow.com/a/68119286
awk -v ver=${VERSION} '/^#+ \[/ { if (p) { exit }; if ($2 == "["ver"]") { p=1; next} } p && NF' CHANGELOG.md > release_notes.md

# Retrieve checksums.
CHECKSUM_MD5=$(cat vibe-cli/target/checksums/vibe-with-dependencies-${VERSION}.jar.md5)
CHECKSUM_SHA256=$(cat vibe-cli/target/checksums/vibe-with-dependencies-${VERSION}.jar.sha256)
CHECKSUM_SHA512=$(cat vibe-cli/target/checksums/vibe-with-dependencies-${VERSION}.jar.sha512)
CHECKSUM_SHA3512=$(cat vibe-cli/target/checksums/vibe-with-dependencies-${VERSION}.jar.sha3512)

# Add checksum info to release notes.
cat << EOF >> release_notes.md
### Checksums
|algorithm|checksum|
|---|---|
|MD5|${CHECKSUM_MD5}|
|SHA-256|${CHECKSUM_SHA256}|
|SHA-512|${CHECKSUM_SHA512}|
|SHA3-512|${CHECKSUM_SHA3512}|
EOF
