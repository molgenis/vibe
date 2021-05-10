#!/usr/bin/env bash

###########################################################################
# Name:         TurtleFinder.sh                                           #
# Function:     Searches for triples in a Turtle file.                    #
#                                                                         #
# Usage:        see USAGE variable below or use no arguments              #
###########################################################################

# Defines error echo.
errcho() { echo "$@" 1>&2; }

# Describes usage.
readonly USAGE="Usage: TurtleFinder.sh { --hpo | --orpha | --pda | --umls | --gda | --gene | --pmid } <pattern>
Description: Retrieves data from the turtle files generated by the GenerateDatabase.sh script.
             Multiple values can be given by separating them with a comma.
             For example: TurtleFinder.sh --umls C1835764,C0265292,C0220687

IMPORTANT: Be sure to first move to the generated vibe-<version>-ttl folder before running this script!

Arguments:
-hpo            Get the triples for this HPO (format: 0008438).
-orpha          Get the triples for this orphanet ID (format: 85184).
-pda            Get the triples for this pda (format: DGN8a0e701b56199eca12831b38431d7959).
-umls           Get the triples for this umls (format: C1835764).
-gda            Get the triples for this gda (format: DGNab104bee05182ff779c15cf93aba276a).
-gene           Get the triples for this gene (format: 29123).
-pmid           Get the triples for this pmid  (format: 25413698).
"

# Base paths (to current dir/script).
readonly CURRENT_PATH=$(pwd)
readonly BASE_PATH=$(sed 's/GenerateDatabase.sh$//' <<< $0 | sed -e 's/^$/.\//g')
ADD_PREFIX=1

main() {
    #Digests the command line arguments.
    while [[ $# -gt 0 ]]
    do
      key="$1"
      case $key in
        --hpo)
          printMatches "$2" '^obo:HP_' 'hpo.ttl'
          exit 0
          ;;
        --orpha)
          printMatches "$2" '^ordo:Orphanet_' 'hpo.ttl'
          exit 0
          ;;
        --pda)
          printMatches "$2" '^pda:' 'hpo.ttl'
          exit 0
          ;;
        --umls)
          printMatches "$2" '^umls:' 'disease.ttl'
          exit 0
          ;;
        --gda)
          printMatches "$2" '^gda:' 'gda.ttl'
          exit 0
          ;;
        --gene)
          printMatches "$2" '^ncbigene:' 'gene.ttl'
          exit 0
          ;;
        --pmid)
          printMatches "$2" '^pmid:' 'pubmed.ttl'
          exit 0
          ;;
        *)    # unknown option
          shift # argument
          ;;
        esac
    done

    # If no argument found, shows usage.
    echo "$USAGE";
}

# Arguments:
#   $1 the pattern to use
#   $2 the prefix to use (for each occurrence of the pattern)
#   $3 the file to search in
function printMatches {
  # Replaces comma with prefix if multiple items are present (comma-separated)
  adjustedString=$(echo "$1" | sed "s/ *, */|$2/g")
  adjustedString="$2$adjustedString"
  echo "### $adjustedString in $3"
  gawk -v pattern="$adjustedString" '$0 ~ pattern {print_lines=1} print_lines==1 {print} NF==0 {print_lines=0}' "$3"
}

main "$@"