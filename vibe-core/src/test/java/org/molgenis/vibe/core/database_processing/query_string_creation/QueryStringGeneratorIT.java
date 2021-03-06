package org.molgenis.vibe.core.database_processing.query_string_creation;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.molgenis.vibe.core.TestData;
import org.molgenis.vibe.core.io.input.HdtFileReader;
import org.molgenis.vibe.core.io.input.ModelReader;
import org.molgenis.vibe.core.database_processing.QueryRunner;

import java.io.IOException;
import java.util.*;

/**
 * Note that these tests use data from DisGeNET for validation. However, this was kept as minimal as possible while still
 * being able to actually test the functioning of the code and only reflects what is EXPECTED to be found within the
 * DisGeNET dataset when using the query (on a technical basis). Additionally, the actual data on which these tests are
 * executed on are available externally and are not included in the repository itself.
 *
 * The full DisGeNET RDF dataset can be downloaded from: http://rdf.disgenet.org/download/
 * The license can be found on: http://www.disgenet.org/ds/DisGeNET/html/legal.html
 */
@Execution(ExecutionMode.SAME_THREAD)
class QueryStringGeneratorIT {
    private static ModelReader reader;
    private QueryRunner runner;

    @BeforeAll
    static void beforeAll() throws IOException {
        reader = new HdtFileReader(TestData.HDT.getFullPathString());
    }

    @AfterAll
    static final void afterAll() throws IOException {
        if(reader != null) {
            reader.close();
        }
    }

    @AfterEach
    void afterEach() {
        runner.close();
    }

    @Test
    void testSourcesUnique() {
        QueryString queryString = QueryStringGenerator.getSources();
        runner = new QueryRunner(reader.getModel(), queryString);

        Set<String> sources = new HashSet<>();

        while(runner.hasNext()) {
            String source = runner.next().get("source").asResource().getURI();
            if(sources.contains(source)) {
                Assertions.fail("a source URI was found more than once");
            }
            sources.add(source);
        }
    }
}
