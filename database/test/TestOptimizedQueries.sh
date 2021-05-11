#!/usr/bin/env bash

#########################################################################
#Name:     TestOptimizedQueries.sh                                      #
#Function: Script for validating/comparing SPARQL query files.          #
#                                                                       #
#Usage:    TestOptimizedQueries.sh                                      #
#########################################################################

# Defines error echo.
errcho() { echo "$@" 1>&2; }

# Describes usage.
readonly USAGE="Usage: TestOptimizedQueries.sh [-h] -or <DIR> -op <DIR>
Description: Runs preparations for unit testing.
Arguments:
-h   --help       Shows this help message.
-or  --original   Path to directory containing the complete TDB.
-op  --optimized  Path to directory containing the optimized HDT.

IMPORTANT:  Requires Apache Jena TDB Command-line Utilities to be configured.
            See https://jena.apache.org/documentation/tools/#setting-up-your-environment for more information.

            Requires hdt-java to be configured.
            See https://github.com/rdfhdt/hdt-java#compiling for compiling and add it to your class path in .bashrc:
                export HDTJAVA_HOME=/path/to/hdt-java-package-<version>
                export PATH=$PATH:$HDTJAVA_HOME/bin
"

# Base path (to script).
BASE_PATH=$(realpath "$0") && readonly BASE_PATH=${BASE_PATH%/*}

main() {
  digestCommandLine "$@"
  runTests
}

digestCommandLine() {
  # Checks if any arguments were given.
  if [ $# -eq 0 ]; then errcho "No arguments were given.\n\n$USAGE"; exit 1; fi

  #Digests the command line arguments.
  while [[ $# -gt 0 ]]
  do
    key="$1"
    case $key in
      -or|--original)
      readonly TDB_original="$2"
      shift # argument
      shift # value
      ;;
      -op|--optimized)
      readonly HDT_optimized="$2"
      shift # argument
      shift # value
      ;;
      -h|--help)
      local help=TRUE
      shift # argument
      ;;
      *)    # unknown option
      shift # argument
      ;;
    esac
  done

  # Checks if usage is requested.
  if [[ ${help} == TRUE ]]; then echo "$USAGE"; exit 0; fi

  # Checks if variable is set. -> http://wiki.bash-hackers.org/syntax/pe#use_an_alternate_value
  if [[ ! ${TDB_original+isset} == isset ]]; then errcho "Missing required argument: -or/--original <DIR>\n\n$USAGE"; exit 1; fi

  # Checks if variable is set. -> http://wiki.bash-hackers.org/syntax/pe#use_an_alternate_value
  if [[ ! ${HDT_optimized+isset} == isset ]]; then errcho "Missing required argument: -op/--optimized <FILE>\n\n$USAGE"; exit 1; fi

  # Checks if given argument is an existing directory.
  if [ ! -d "$TDB_original" ]; then errcho "Path to original TDB is not an existing directory.\n\n$USAGE"; exit 1; fi

  # Checks if given argument is an existing directory.
  if [ ! -f "$HDT_optimized" ]; then errcho "Path to optimized HDT file is not a readable file.\n\n$USAGE"; exit 1; fi
}

runTests() {
  # Sets path to parent dir of tests.
  cd "$BASE_PATH"
  cd ../

  # Runs queries.
  echo "### Running original TDB/query."
  time tdbquery --results=CSV --loc="$TDB_original" --query="sparql_queries/tdb_comparison/original_tdb/genes_for_hpo.rq" 1>"test/genes_for_hpo-original.tsv"
  echo "### Running optimized HDT/query."
  QUERY=$(cat "sparql_queries/tdb_comparison/optimized_tdb/genes_for_hpo.rq")
  time hdtsparql.sh ${HDT_optimized} "${QUERY}" 1>"test/genes_for_hpo-optimized.tsv"

  # Compare shasums.
  echo "### Validating if optimized TDB/query output files are equal to their original counterparts."
  local CHECKSUM_GENES_FOR_PHENOTYPES=$(shasum -a 256 test/genes_for_hpo-original.tsv | cut -d ' ' -f1)
  shasum -a 256 -c <<< "$CHECKSUM_GENES_FOR_PHENOTYPES  test/genes_for_hpo-optimized.tsv" # double space is needed! Second space indicates text file type.
}

main "$@"
