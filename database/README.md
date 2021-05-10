# Database creation

VIBE needs a local database to run. While a downloadable version is available (see [here](../README.md#quickstart)), it is possible to generate one as well. To do this, go to the directory in which the database should be created and run the following command:

```bash
/path/to/vibe/database/GenerateDatabase.sh
```

Do **NOT** move the bash script from it's original position, as it uses the `pom.xml` file for retrieving information such as the version number that should be for the created database.

The script consists of different phases which are ran one after another. It is also possible to run a selection of the phases. Please view the `-h` option for more information about this.

**Note that only the final HDT file/archive can be used with VIBE! Never use VIBE with any of the intermediate output!**

## Requirements

- Apache Jena ([download][jena_download] and [configure][jena_configure])
- hdt-java ([download][hdt-java_download], [compile][hdt-java_compiling] and configure)
- GNU AWK
- wget

Be sure that both Apache Jena and hdt-java are added to `$PATH` in `.bashrc`:

```bash
# Apache Jena
export JENA_HOME=/path/to/apache-jena-<version>
export PATH=$PATH:$JENA_HOME/bin

# hdt-java
export HDTJAVA_HOME=/path/to/hdt-java-package-<version>
export PATH=$PATH:$HDTJAVA_HOME/bin
```

### MacOS

It is highly suggested to use a package manager on MacOS where possible, such as [MacPorts][macports]. You can easily install some of the requirements using:

````
sudo port install gawk
sudo port install wget
````

### Windows

Currently there isn't a bat script that offers automated database creation. Please use the already created database as available on the molgenis download server (see the [main README](../README.md#quickstart)).

## Updating the database

**Path explanation:**

- `/path/to/vibe/` → the directory where the vibe git repository is stored
- `/path/to/data/` → the directory where the database is generated in

**Step-by-step instructions:**

1. Check whether any breaking changes were made in the new version of the sources.

   - If there are no breaking changes, simply continue to the next step (minor release).

   - If there are breaking changes, a new major release might be required with adjustments to the java-app and the SPARQL queries in `/path/to/vibe/database/sparql_queries/*`. These steps can still be used as a guideline, but changes might need to be made before actually running them. Be sure to add/update scripts in `/path/to/vibe/database/tdb_comparison/` to reflect the querries used in the vibe-app.

2. Change the project's `version` and `vibe-database.version` in the `/path/to/vibe/pom.xml` to reflect the new release.

3. Update the links in the `DownloadData()` method from `/path/to/vibe/database/GenerateDatabase.sh`  to the new versions of the used sources & ensure the dump file (if present) is excluded correctly.

4. Update the `dvoid` prefix to the new DisGeNET version in the scripts stored at `/path/to/vibe/database/sparql_queries` & `/path/to/vibe/vibe-core/src/main/java/org/molgenis/vibe/core/database_processing/query_string_creation/QueryStrinGenerator.java`.

5. Use `cd` to a directory where you want to create the new database (`cd /path/to/data/`) and run `/path/to/vibe/database/GenerateDatabase.sh -1` from there.

6. Go to the newly created directory (`cd /path/to/data/vibe-<version>-sources`), run `shasum -a 256 $(find . -not -path '*/.*' -type f) > path/to/vibe/database/checksums/sources.sha256` and check the created output (checksums for all needed files and only those).

7. Validate whether `/path/to/vibe/database/LICENSES.md` is still up-to-date, and if not, adjust it.

8. Go back to the parent data directory (`cd /path/to/data/`) and run  `/path/to/vibe/database/GenerateDatabase.sh -2 -3 -4 -5 -6`.

   - Note that this step can take several hours.

   - If any error is thrown or the generated output files seem incorrect, a breaking change might exist and adjustments should be made to the process accordingly.

9. Run `/path/to/vibe/database/test/TestOptimizedQueries.sh`.

   - If it fails, something went wrong in creating the optimized database from the original one as they do not return the exact same information. This might be caused by a breaking change which requires an adjustment to the database creation process.

10. Copy the `/path/to/data/vibe-<version>-hdt` directory to `/path/to/vibe/shared_testdata/shared`, rename the folder to `hdt` and remove the version number (including the `-`) from the files in the folder.

11. Update the value of `DISGENET_VERSION` in `/path/to/vibe/vibe-core/src/test/java/org/molgenis/vibe/core/TestConstants.java`.

12. Run `mvn clean install` in `/path/to/vibe/` and check for any failing test.

    - If a test fails due to small changes such as a new results, simply fix the unit-test. The `/path/to/vibe/database/TurtleFinder.sh` script can be used for retrieving data in plaint text from the generated `.ttl` files which were generated through `/path/to/vibe/database/GenerateDatabase.sh -4` (be sure to `cd` to the `/path/to/data/vibe-<version>-ttl` directory before using this script).

    - If larger problems occur, there might be a breaking change and adjustments to the java-app and/or TDB creation might be required. Be sure to add/update scripts in `/path/to/vibe/database/tdb_comparison/` to reflect the querries used in the vibe-app. 

13. Run the vibe-cli jar with the new database and check whether it still works (in theory this shouldn't cause any issues):  `java -jar /path/to/vibe/vibe-cli/target/vibe-with-dependencies-<version>-SNAPSHOT.jar -t /path/to/data/vibe-5.1.0-hdt/vibe-5.1.0.hdt -w /path/to/hp.owl -p HP:0002996`

14. Run `/path/to/vibe/database/GenerateDatabase.sh -7` from the `/path/to/data` directory.

15. Run `shasum -a 256 vibe-5.1.0-hdt.tar.gz > /path/to/vibe/database/checksums/database.sha256`

16. Upload the new TDB archive to the download server.

17. Test whether `/path/to/vibe/TestsPreprocessor.sh` still functions correctly.

18. Make a pull-request with the changes.

## F.A.Q.

**Q:** Why do I get an `org.apache.jena.atlas.RuntimeIOException: java.nio.charset.MalformedInputException: Input length = 1` error when trying to create a TDB from the generated `.ttl` files?

**A:** This might be caused by the generated `.ttl` files having an incorrect file encoding. Please make sure the generated `.ttl` files have the encoding `UTF-8`. If this is not the case, manually change it to  `UTF_8`.

[jena_download]: https://jena.apache.org/download/index.cgi
[jena_configure]: https://jena.apache.org/documentation/tools/#setting-up-your-environment
[hdt-java_download]: https://github.com/rdfhdt/hdt-java/releases
[hdt-java_compiling]: https://github.com/rdfhdt/hdt-java#compiling
[macports]: https://www.macports.org/

