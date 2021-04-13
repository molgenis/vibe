[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=org.molgenis%3Avibe&metric=alert_status)](https://sonarcloud.io/dashboard?id=org.molgenis%3Avibe)

# VIBE

A tool to generate prioritized genes using phenotype information.

## Web app

* Available at: https://vibe.molgeniscloud.org/
* Source code: https://github.com/molgenis/molgenis-app-vibe

## Benchmark

* Available at: https://github.com/molgenis/vibe-suppl

## Quickstart

* Download [vibe .jar file][vibe_download]
* Download and extract [HDT][hdt_download]
* Download the [HPO.owl][hpo_owl]
* Make sure you have [Java 8 or higher][java_download]
* Open a terminal and run VIBE. `java -jar vibe-with-dependencies-<version>.jar -d -t vibe-<db-version>.hdt -o results.tsv -p HP:0002996 -p HP:0001377`

## Detailed instructions

### Requirements

* [Java 8 or higher][java_download]
* [A local HDT dataset][hdt_download]
* [Human Phenotype Ontology (HPO)][hpo_owl]

### Usage

`java -jar vibe-with-dependencies.jar [-h] [-v] [-d] [-f] -t <FILE> -w <FILE> [-n <NAME> -m <NUMBER>] [-o <FILE>] [-l] [-u] -p <HPO ID> [-p <HPO ID>]...`

**IMPORTANT:** Do keep  [this](https://github.com/molgenis/vibe/issues/25) in mind. Especially when using `-n`.

### Examples

Using only the user-defined phenotypes:

`java -jar vibe-with-dependencies-<version>.jar -d -t vibe-<db-version>.hdt -w hp.owl -o results.tsv -p HP:0002996 -p HP:0001377`

---

Using the user-defined phenotypes and phenotypes that are related to them with a maximum distance of 1:

`java -jar vibe-with-dependencies-<version>.jar -d -t vibe-<db-version>.hdt -w hp.owl -n distance -m 1 -o results.tsv -p HP:0002996`

---

Using the user-defined phenotypes and their (grand)children with a maximum distance of 2:

`java -jar vibe-with-dependencies-<version>.jar -d -t vibe-<db-version>.hdt -w hp.owl -n children -m 2 -o results.tsv -p HP:0002996`

### Output format

There are currently 2 options for the output. By default the output will look something like:

```
gene (NCBI)	gene symbol (HGNC)	highest GDA score	diseases (UMLS) with sources per disease
1311	COMP	1.0	C0410538 (1.0):10405447,10753957,10852928,11084047,11691584,11745002,11746044,11746045,11782471,11891674,11968079,12479386,12483304,12483437,12768438,12792737,12819015,14580238,15094116,15183431,15266613,15337766,15551305,15579310,15694129,15756302,15880723,16199550,16514635,16520029,17200202,17307347,17394206,17570134,17579668,17588960,18193163,18546327,19762713,20301660,20578249,20819661,20936634,21042783,21599986,21922596,22006726,23562786,24194321,24595329,24892720,24997222,29104872,7670471,7670472,9021009,9184241,9188668,9388247,9452026,9452063,9463320,9632164,9749943,9880218,9887340,9921895,9923655|C1838280 (0.74):10405447,11084047,11565064,11782471,12483304,12819015,14684695,20301302,21922596,7670472,9021009,9184241,9452026,9463320,9887340,9921895,9923655|C0026760 (0.6):10364514,10655510,11084047,11479597,11528506,11565064,11782471,11968079,12479386,12483304,12819015,15183431,15337766,15694129,15756302,15880723,16199550,16514635,17133256,17200202,17570134,18193163,18682400,19808781,20578249,20936634,21922596,24595329,24997222,7670472,9184241,9463320,9921895,9923655|C0013336 (0.13):11691584,12483304,15472220|C1867103 (0.1)|C0029422 (0.03):11891674,12768438,17579668
4010	LMX1B	1.0	C0027341 (1.0):10425280,10537763,10571942,10660670,10767331,10854116,11668639,11956244,11978876,12215822,12792813,12819019,15562281,15638822,15774843,15785774,15928687,16825280,17166916,17431898,17515884,17657578,17710881,18414507,18535845,18538102,18562181,18595794,18634531,18952915,19147669,19222527,19721866,20199424,20531206,20568247,21184584,21850167,22211385,23687361,24042019,24720768,25380522,25898926,26380986,26560070,28335748,9590287,9590288,9618165,9664684,9837817|C0029422 (0.11):10767331|C1867103 (0.1)|C1867439 (0.1)
```

Here, each row represents a gene. Each row consists out of several (tab-seperated) fields. First is the NCBI gene id, then the highest found gene-disease association score found in DisGeNET for that gene, and finally all found associated diseases with the gene-disease association score specific for that gene-disease combination followed by the evidence IDs (pubmed if no URL is shown) for it.

Alternatively, the option `-l` can be added when running the tool. If this is done, the output will only contain the genes (separated by comma's). The example output above would look like:

```
1311,4010
```

The `-u` option changes the output to use URI's instead of ID's for certain fields. As this strongly increases the output size, in general it is not advised to use this option.

## For developers

Below are the instructions on how to build the app. For instructions on how to create the HDT, please check [here](./database/README.md). 

### Requirements
- Apache Maven ([download][maven_download] and [install][maven_install])

### Preperations
Before building/testing, be sure the needed test resources are downloaded. This can be done by running `TestsPreprocessor.sh`. When checking out a commit that uses a different version of the test resources (as defined by the `pom.xml` properties `vibe-database.version` and `hpo-owl.tag`), be sure to re-run this script so that the correct resources are used for testing. 

### Building executables

1. Run `mvn clean install` to create all needed files.
   - An überjar for the command-line tool can be found at `vibe-cli/target/vibe-with-dependencies-<version>.jar` .
   - Checksums are available in the `<sub-module>/target/checksums/` folders for the following formats:
     - MD5
     - SHA-512
     - SHA3-512

### Notes

- The different modules partly use the same resources. To not create duplicate data, these are stored in the `shared_resources/shared` folder. When adding new test resources to a module, ensure that there is no `src/test/resources/shared` directory.
- In case tests fail on Jenkins due to Jenkins-specific reasons (f.e. due to requiring to change a file permission of a test resource), the JUnit tag `skipOnJenkins` can be used so that these test still occur when building locally.

[vibe_download]: https://github.com/molgenis/vibe/releases/latest
[java_download]:https://www.java.com/download
[hdt_download]: https://downloads.molgeniscloud.org/downloads/vibe/vibe-5.0.0-hdt.tar.gz
[jena_download]:https://jena.apache.org/download/index.cgi
[jena_configure]: https://jena.apache.org/documentation/tools/#setting-up-your-environment
[hpo_owl]:http://purl.obolibrary.org/obo/hp.owl
[maven_download]:https://maven.apache.org/download.cgi
[maven_install]:https://maven.apache.org/install.html
