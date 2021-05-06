#!/usr/bin/env bash

#########################################################################
# Name:       GenerateDatabase.sh                                       #
# Function:   Generates database for vibe.                              #
#                                                                       #
# Usage:      see USAGE variable below or use the -h option             #
# Developers: Be sure to follow the accompanying README in the same     #
#             directory when making changes in regards to the data      #
#             that is being downloaded (or if updating checksums).      #
#########################################################################

# Defines error echo.
errcho() { echo "$@" 1>&2; }

# Describes usage.
readonly USAGE="Usage: GenerateDatabase.sh [-h] [-1] [-2] [-3] [-4] [-5]
Description: Generates database for vibe.
The process is split into multiple phases which can be chosen individually.
If no phase is given, runs phase 1-6 one after another.

Arguments:
-h  --help      Shows this help message.
-1              Download sources.
-2              Validates/prepares sources.
-3              Create initial TDB.
-4              Create optimized TTL files.
-5              Merge optimized TTL files.
-6              Create HDT + index + adds other relevant files to dir.
-7              Create distributable archive.

IMPORTANT:  Be sure to run this from the directory where all the data should be created!

            Requires Apache Jena TDB Command-line Utilities to be configured.
            See https://jena.apache.org/documentation/tools/#setting-up-your-environment for more information.

            Requires hdt-java to be configured.
            See https://github.com/rdfhdt/hdt-java#compiling for compiling and add it to your class path in .bashrc:
                export HDTJAVA_HOME=/path/to/hdt-java-package-<version>
                export PATH=$PATH:$HDTJAVA_HOME/bin

            Requires GNU awk (gawk) to be installed.

            Requires wget to be installed.
"

# Base paths (to current dir/script).
readonly CURRENT_PATH=$(pwd)
readonly BASE_PATH=$(sed 's/GenerateDatabase.sh$//' <<< $0 | sed -e 's/^$/.\//g')

# Retrieves vibe version from pom.xml file.
cd ${BASE_PATH}
readonly VIBE_VERSION=$(grep -A 1 '<artifactId>vibe</artifactId>' ../pom.xml | grep '<version>' | sed 's/[^0-9.]//g')
cd ${CURRENT_PATH}

# Defines directory names to be used.
readonly SOURCES_DIR=vibe-${VIBE_VERSION}-sources
readonly INITIAL_TDB_DIR=vibe-${VIBE_VERSION}-sources-tdb
readonly TTL_DIR=vibe-${VIBE_VERSION}-ttl
readonly FINAL_HDT_DIR=vibe-${VIBE_VERSION}-hdt
readonly FINAL_HDT_ARCHIVE=${FINAL_HDT_DIR}.tar.gz

# Defines filenames to be used.
readonly FINAL_TTL_FILE=vibe-${VIBE_VERSION}.ttl
readonly FINAL_HDT_FILE=${FINAL_HDT_DIR}/vibe-${VIBE_VERSION}.hdt # Be sure to adjust changes in pom file as well.
readonly FINAL_INFO_FILE=${FINAL_HDT_DIR}/"info.txt"

# Retrieve system information.
readonly THREADS=$(nproc) # available to process (no `-all`)

main() {
    digestCommandLine $@
    
    if [[ ${doDownload} == true ]]
    then
        downloadData
    fi

    if [[ ${doPrepare} == true ]]
    then
        prepareData
    fi

    if [[ ${doOriginalTdb} == true ]]
    then
        createInitialTdb
    fi

    if [[ ${doOptimizedTtl} == true ]]
    then
        createOptimizedTtlFiles
    fi

    if [[ ${doMergeOptimizedTtl} == true ]]
    then
        mergeOptimizedData
    fi

    if [[ ${doOptimizedDatabase} == true ]]
    then
        createHdt
        copyLicensesToDatabaseDir
        addVersionInformationToDatabaseDir
    fi

    if [[ ${doOptimizedDatabaseArchive} == true ]]
    then
        createArchive
    fi
}

digestCommandLine() {
    # Sets defaults for phases.
    doDownload=false
    doPrepare=false
    doOriginalTdb=false
    doOptimizedTtl=false
    doMergeOptimizedTtl=false
    doOptimizedDatabase=false
    doOptimizedDatabaseArchive=false

    #Digests the command line arguments.
    while [[ $# -gt 0 ]]
    do
        key="$1"
        case $key in
            -1)
            doDownload=true
            shift # argument
            ;;
            -2)
            doPrepare=true
            shift # argument
            ;;
            -3)
            doOriginalTdb=true
            shift # argument
            ;;
            -4)
            doOptimizedTtl=true
            shift # argument
            ;;
            -5)
            doMergeOptimizedTtl=true
            shift # argument
            ;;
            -6)
            doOptimizedDatabase=true
            shift # argument
            ;;
            -7)
            doOptimizedDatabaseArchive=true
            shift # argument
            ;;
            -h|--help)
            local help=true
            shift # argument
            ;;
            *)    # unknown option
            shift # argument
            ;;
        esac
    done

    # Checks if usage is requested.
    if [[ ${help} == true ]]; then echo "$USAGE"; exit 0; fi

    # If no phase is set, defaults most to true.
    if [[ ${doDownload} == false ]] && [[ ${doPrepare} == false ]] && [[ ${doOriginalTdb} == false ]] &&
      [[ ${doOptimizedTtl} == false ]] && [[ ${doMergeOptimizedTtl} == false ]] &&
      [[ ${doOptimizedDatabase} == false ]] && [[ ${doOptimizedDatabaseArchive} == false ]]
    then
        doDownload=true
        doPrepare=true
        doOriginalTdb=true
        doOptimizedTtl=true
        doMergeOptimizedTtl=true
        doOptimizedDatabase=true
        # doOptimizedDatabaseArchive stays false
    fi

    # Make phase variables readonly.
    readonly doDownload=${doDownload}
    readonly doPrepare=${doPrepare}
    readonly doOriginalTdb=${doOriginalTdb}
    readonly doOptimizedTtl=${doOptimizedTtl}
    readonly doMergeOptimizedTtl=${doMergeOptimizedTtl}
    readonly doOptimizedDatabase=${doOptimizedDatabase}
    readonly doOptimizedDatabaseArchive=${doOptimizedDatabaseArchive}

    # Prints for each phase whether it will be run.
    echo "######## ######## ######## Selected phases ######## ######## ########"
    echo "download:${doDownload}"
    echo "prepare:${doPrepare}"
    echo "initial TDB:${doOriginalTdb}"
    echo "optimized TTL:${doOptimizedTtl}"
    echo "nmerge optimized TTL:${doMergeOptimizedTtl}"
    echo "optimized database:${doOptimizedDatabase}"
    echo "archive:${doOptimizedDatabaseArchive}"

    # Check whether directories/files might already exist.
    validatePhasesRunnable
}

validatePhasesRunnable() {
    echo "######## ######## ######## Checking existence phase-specific directories ######## ######## ########"
    # Done separately (instead of integrated into main) so that first all necessary directories are
    # checked whether they already exist before anything is actually done.
    
    # Boolean for when a directory/file which should be created already exists.
    local phaseRunnableError=false

    # Checks directories for the different phases.
    if [[ ${doDownload} == true ]]
    then
        # This phase check.
        if [ -d "$SOURCES_DIR" ]; then phaseRunnableError=true; errcho "${SOURCES_DIR} already exists."; fi
    fi

    if [[ ${doPrepare} == true ]]
    then
        # Previous phase check.
        if [ ${doDownload} == false ] && [ ! -d "$SOURCES_DIR" ]; then phaseRunnableError=true; errcho "${SOURCES_DIR} does not exists."; fi
        # This phase check.
        if [ -f "$SOURCES_DIR"/.prepared ]; then phaseRunnableError=true; errcho "${SOURCES_DIR} already prepared."; fi
    fi

    if [[ ${doOriginalTdb} == true ]]
    then
        # Previous phase check.
        if [ ${doPrepare} == false ] && [ ! -f "$SOURCES_DIR"/.prepared ]; then phaseRunnableError=true; errcho "${SOURCES_DIR} not yet prepared."; fi
        # This phase check.
        if [ -d "$INITIAL_TDB_DIR" ]; then phaseRunnableError=true; errcho "${INITIAL_TDB_DIR} already exists."; fi
    fi

    if [[ ${doOptimizedTtl} == true ]]
    then
        # Previous phase check.
        if [ ${doOriginalTdb} == false ] && [ ! -d "$INITIAL_TDB_DIR" ]; then phaseRunnableError=true; errcho "${INITIAL_TDB_DIR} does not exists."; fi
        # This phase check.
        if [ -d "$TTL_DIR" ]; then phaseRunnableError=true; errcho "${TTL_DIR} already exists."; fi
    fi

    if [[ ${doMergeOptimizedTtl} == true ]]
    then
        # Previous phase check.
        if [ ${doOptimizedTtl} == false ] && [ ! -d "$TTL_DIR" ]; then phaseRunnableError=true; errcho "${TTL_DIR} does not exists."; fi
        # This phase check.
        if [ -f "$FINAL_TTL_FILE" ]; then phaseRunnableError=true; errcho "${FINAL_TTL_FILE} already exists."; fi
    fi

    if [[ ${doOptimizedDatabase} == true ]]
    then
        # Previous phase check.
        if [ ${doMergeOptimizedTtl} == false ] && [ ! -f "$FINAL_TTL_FILE" ]; then phaseRunnableError=true; errcho "${FINAL_TTL_FILE} does not exists."; fi
        # This phase check.
        if [ -d "$FINAL_HDT_DIR" ]; then phaseRunnableError=true; errcho "${FINAL_HDT_DIR} already exists."; fi
    fi

    if [[ ${doOptimizedDatabaseArchive} == true ]]
    then
        # Previous phase check.
        if [ ${doOptimizedDatabase} == false ] && [ ! -d "$FINAL_HDT_DIR" ]; then phaseRunnableError=true; errcho "${FINAL_HDT_DIR} does not exists."; fi
        # This phase check.
        if [ -d "$FINAL_HDT_ARCHIVE" ]; then phaseRunnableError=true; errcho "${FINAL_HDT_ARCHIVE} already exists."; fi
    fi

    # If a directory already exists, exits script.
    if [[ ${phaseRunnableError} == true ]]; then errcho "Exiting."; exit 1; fi
}

downloadData() {
    echo "######## ######## ######## Downloading files ######## ######## ########"

    # Create & go to download dir.
    mkdir ${SOURCES_DIR}
    cd ${SOURCES_DIR}

    # DisGeNET (general)
    wget --no-verbose --show-progress -r --no-parent --no-host-directories --cut-dirs=1 -A '*.ttl.gz' -R '*-dump.*' 'http://rdf.disgenet.org/download/v7.0.0/'
    # DisGeNET (data from v5.0.0 for phenotype-disease associations)
    wget --no-verbose --show-progress -r --no-parent --no-host-directories --cut-dirs=1 -A 'pda.ttl.tar.gz,phenotype.ttl.tar.gz,void.ttl.tar.gz' 'http://rdf.disgenet.org/download/v5.0.0/'
    # Semanticscience Integrated Ontology
    wget --no-verbose --show-progress 'https://raw.githubusercontent.com/MaastrichtU-IDS/semanticscience/65336c5ba45d4a4cd36b8bec2042c2786bc4ca1f/ontology/sio/release/sio-release.owl'
    # Orphadata HOOM
    wget --no-verbose --show-progress -O owlapi.xml 'http://data.bioontology.org/ontologies/HOOM/download?apikey=8b5b7825-538d-40e0-9e9e-5ab9274a9aeb&download_format=rdf'

    # Returns to original dir.
    cd ${CURRENT_PATH}
}

prepareData() {
    # Validates downloaded files.
    cd ${SOURCES_DIR}
    validateDownloads
    unpackDownloadedArchives
    touch .prepared
    cd ${CURRENT_PATH}
}

validateDownloads() {
    echo "######## ######## ######## Validating downloaded files ######## ######## ######## "
    shasum -a 256 -c ${BASE_PATH}sources_checksums.txt
    if (($? != 0))
    then
        errcho "Checksum for downloaded sources failed. Exiting."
        exit 1
    fi
}

unpackDownloadedArchives() {
    echo "######## ######## ######## Unpacking archives ######## ######## ########"

    # Extract gzip archives.
    find . -name "*.gz" -print0 | xargs -0 -P ${THREADS} -n 1 gunzip

    # Untar v5.0.0 data.
    cd v5.0.0
    find . -name '*.tar' -print0 | xargs -0 -P ${THREADS} -n 1 tar -xvf
    rm *.tar
    cd ../
}

createInitialTdb() {
    echo "######## ######## ######## Creating initial TDB ######## ######## ########"
    tdbloader2 --loc ${INITIAL_TDB_DIR} ${SOURCES_DIR}/sio-release.owl ${SOURCES_DIR}/owlapi.xml $(find ${SOURCES_DIR} -name '*.ttl')
}

createOptimizedTtlFiles() {
    echo "######## ######## ######## Creating optimized TTL files ######## ######## ########"
    mkdir ${TTL_DIR}

    echo "Generating: hpo.ttl"
    tdbquery --loc=${INITIAL_TDB_DIR} --query=${BASE_PATH}/sparql_queries/optimized_construct/hpo.rq 1> ${TTL_DIR}/hpo.ttl
    echo "Generating: disease.ttl"
    tdbquery --loc=${INITIAL_TDB_DIR} --query=${BASE_PATH}/sparql_queries/optimized_construct/disease.rq 1> ${TTL_DIR}/disease.ttl
    echo "Generating: gene.ttl"
    tdbquery --loc=${INITIAL_TDB_DIR} --query=${BASE_PATH}/sparql_queries/optimized_construct/gene.rq 1> ${TTL_DIR}/gene.ttl
    echo "Generating: gda.ttl"
    tdbquery --loc=${INITIAL_TDB_DIR} --query=${BASE_PATH}/sparql_queries/optimized_construct/gda.rq 1> ${TTL_DIR}/gda.ttl
    echo "Generating: pubmed.ttl"
    tdbquery --loc=${INITIAL_TDB_DIR} --query=${BASE_PATH}/sparql_queries/optimized_construct/pubmed.rq 1> ${TTL_DIR}/pubmed.ttl
    echo "Generating: source.ttl"
    tdbquery --loc=${INITIAL_TDB_DIR} --query=${BASE_PATH}/sparql_queries/optimized_construct/source.rq 1> ${TTL_DIR}/source.ttl
}

mergeOptimizedData() {
  echo "######## ######## ######## Merging files into single TTL ######## ######## ########"
  riot --output=turtle ${TTL_DIR}/*.ttl ${SOURCES_DIR}/sio-release.owl > ${FINAL_TTL_FILE}
}

createHdt() {
    echo "######## ######## ######## Creating HDT files ######## ######## ########"
    mkdir ${FINAL_HDT_DIR}
    JAVA_OPTIONS='-Xmx8g' rdf2hdt.sh -rdftype turtle ${FINAL_TTL_FILE} ${FINAL_HDT_FILE}
    echo 'exit' | hdtSearch.sh ${FINAL_HDT_FILE}
}

copyLicensesToDatabaseDir() {
    echo "######## ######## ######## Adding licenses file to optimized database ######## ######## ########"
    cp ${BASE_PATH}/LICENSES.md ${FINAL_HDT_DIR}
}

addVersionInformationToDatabaseDir() {
    echo "######## ######## ######## Adding versions of used tools to create database ######## ######## ########"

    echo "### compiled with ###" > ${FINAL_INFO_FILE}
    riot --version | head -n 2 >> ${FINAL_INFO_FILE}
    echo "hdt-java " $(rdf2hdt.sh -version) >> ${FINAL_INFO_FILE}

    echo "\n### used database versions ###" >> ${FINAL_INFO_FILE}
    gawk 'match($0, /<versionInfo rdf:datatype="http:\/\/www.w3.org\/2001\/XMLSchema#string">([0-9.]+)<\/versionInfo>/, arr) {print "HOOM v" arr[1]; exit}' ${SOURCES_DIR}/owlapi.xml >> ${FINAL_INFO_FILE}
    gawk 'match($0, /<owl:versionInfo rdf:datatype="http:\/\/www.w3.org\/2001\/XMLSchema#string">([0-9.]+)<\/owl:versionInfo>/, arr) {print "SIO v" arr[1]; exit}' ${SOURCES_DIR}/sio-release.owl >> ${FINAL_INFO_FILE}
    find ${SOURCES_DIR} -type d -not -name ".*" -maxdepth 1 | gawk 'match($0, /(v[0-9]+\.[0-9]+\.[0-9]+)/, arr) {print "DisGeNET " arr[1]}'

    # Manually add note in regards to v5.0.0 data from DisGeNET
    printf "(v5.0.0: pda.ttl, phenotype.ttl & void.ttl only)" >> ${FINAL_INFO_FILE}
}

createArchive() {
    echo "######## ######## ######## Creating archive from optimized database ######## ######## ########"
    tar -czvf ${FINAL_HDT_ARCHIVE} ${FINAL_HDT_DIR}
}

main $@