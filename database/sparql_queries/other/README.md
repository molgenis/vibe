# Other SPARQL queries

A collection of SPARQL queries that might be useful can be found here. Note that these are currently not used for VIBE directly, but were added either as initial base to include within VIBE later, were needed for [vibe-suppl][vibe-suppl] (but as these are dependent on the used TDB were added here) or some other reason.

## For initial TDB

### Generate csv file with gene IDs and their symbol

```bash
tdbquery --results="CSV" --loc=/path/to/initial/TDB --query=/path/to/vibe/database/sparql_queries/other/gene_ids_symbols.rq 1> outputFile.csv
```

### Retrieve GDAs & VDAs for specified gene symbol

**IMPORTANT:** Be sure to adjust the query with the actual gene-symbol (see instructions withint the file) before actually running it!

```bash
tdbquery --time --loc=/path/to/initial/TDB --query=/path/to/vibe/database/sparql_queries/other/associations_for_geneSymbol.rq
```

**NOTE:** As some of the GDAs within DisGeNET were inferred from the VDAs stored in the dataset, duplicate results might be present. See also "Inferred Data" on http://disgenet.org/dbinfo.


## For optimized HDT
### All stored gene IDs & their symbol

```bash
hdtsparql.sh vibe-<version>-hdt/vibe-<version>.hdt "$(cat /path/to/database/sparql_queries/other/optimized_database/gene_symbols.rq)" 1> output.tsv
```




[vibe_suppl]:https://github.com/molgenis/vibe-suppl

