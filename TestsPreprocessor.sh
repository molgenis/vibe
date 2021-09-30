#!/bin/bash

pwd

has_param() {
    local term="$1"
    shift
    for arg; do
        if [[ $arg == "$term" ]]; then
            return 0
        fi
    done
    return 1
}

if has_param '-f' "$@"; then
    FORCE=1;
fi

echo "# Retrieving information from pom.xml"
readonly HDT_ARCHIVE=$(mvn -q help:evaluate -Dexpression=vibe-database.archive -DforceStdout)
readonly HDT_DOWNLOAD=$(mvn -q help:evaluate -Dexpression=vibe-database.download -DforceStdout)
readonly HPO_DOWNLOAD=$(mvn -q help:evaluate -Dexpression=hpo-owl.download -DforceStdout)

cd shared_testdata/shared

if [ $FORCE ]; then
  echo "# Removing old data"
  rm -rf tdb # Removes deprecated TDB if still present.
  rm -rf hdt
  rm -f hp.owl
fi

if [[ ! -d hdt ]]; then
  ##if not exists
  echo "# Downloading data"
  curl -L -O ${HDT_DOWNLOAD} -O ${HPO_DOWNLOAD}

  echo "# Preparing data for unit-tests"
  tar -xzvf ${HDT_ARCHIVE}
  mv ${HDT_ARCHIVE%.tar.gz} hdt
  cd hdt
  mv ${HDT_ARCHIVE%-hdt.tar.gz}.hdt vibe.hdt
  mv ${HDT_ARCHIVE%-hdt.tar.gz}.hdt.index.v1-1 vibe.hdt.index.v1-1
  cd ../
  rm ${HDT_ARCHIVE}
else
  echo "Data already exists, skipping download."
fi
cd ../../