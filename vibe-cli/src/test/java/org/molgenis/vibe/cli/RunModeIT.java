package org.molgenis.vibe.cli;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vibe.cli.io.options_digestion.VibeOptions;
import org.molgenis.vibe.cli.io.output.format.gene_prioritized.GenePrioritizedOutputFormatWriterFactory;
import org.molgenis.vibe.cli.io.output.target.StdoutOutputWriter;
import org.molgenis.vibe.core.formats.Phenotype;
import org.molgenis.vibe.core.io.input.ModelReaderFactory;
import org.molgenis.vibe.core.io.input.VibeDatabase;
import org.molgenis.vibe.core.ontology_processing.PhenotypesRetrieverFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashSet;

import static org.mockito.Mockito.when;

/**
 * Tests to ensure the logic that calls the different processes does not cause any errors. Note that
 * {@link org.molgenis.vibe.cli.properties.VibeProperties} is not loaded, therefore fields such as the version number
 * will not be shown correctly.
 */
@ExtendWith(MockitoExtension.class)
class RunModeIT {
    @Mock private VibeOptions mockedVibeOptions;

    // Define stdout information.
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    /**
     * Custom "stdout".
     */
    @BeforeEach
    void beforeEach() {
        System.setOut(new PrintStream(outContent));
    }

    /**
     * Resets stdout.
     */
    @AfterEach
    void afterEach() {
        System.setOut(originalOut);
    }

    @Test
    void testIfHelpWorks() {
        // Simply tests if calling the help message will not generate any errors.
        Assertions.assertDoesNotThrow(() -> RunMode.HELP.run(mockedVibeOptions));
    }

    @Test
    void testIfVersionWorks() {
        // Simply tests if showing the version number will not generate any errors.
        Assertions.assertDoesNotThrow(() -> RunMode.VERSION.run(mockedVibeOptions));
    }

    @Test
    void testIfGenesForPhenotypesWorks() throws Exception {
        when(mockedVibeOptions.getPhenotypes())
                .thenReturn(new HashSet<>(Arrays.asList(new Phenotype("hp:0008438"))));

        when(mockedVibeOptions.getVibeDatabase())
                .thenReturn(new VibeDatabase(TestData.HDT.getFullPath(), ModelReaderFactory.HDT));

        when(mockedVibeOptions.getGenePrioritizedOutputFormatWriterFactory())
                .thenReturn(GenePrioritizedOutputFormatWriterFactory.SIMPLE);
        when(mockedVibeOptions.getOutputWriter()).thenReturn(new StdoutOutputWriter());

        // Main goal is to ensure no errors are thrown, but output validation purely for gene order is present as well.
        // Based on: GeneDiseaseCollectionRetrievalRunnerIT if sorted by GenePrioritizedOutputFormatWriterFactory.SIMPLE
        RunMode.GENES_FOR_PHENOTYPES.run(mockedVibeOptions);
        Assertions.assertEquals("29123,56172,2697,2475,23522,286,23028", outContent.toString());
    }

    @Test
    void testIfGenesForPhenotypesWithAssociatedPhenotypesWorks() throws Exception {
        when(mockedVibeOptions.getPhenotypes())
                .thenReturn(new HashSet<>(Arrays.asList(new Phenotype("hp:0008438"))));
        when(mockedVibeOptions.getHpoOntology()).thenReturn(TestData.HPO_OWL.getFullPath());
        when(mockedVibeOptions.getPhenotypesRetrieverFactory()).thenReturn(PhenotypesRetrieverFactory.CHILDREN);
        when(mockedVibeOptions.getOntologyMaxDistance()).thenReturn(0);

        when(mockedVibeOptions.getVibeDatabase())
                .thenReturn(new VibeDatabase(TestData.HDT.getFullPath(), ModelReaderFactory.HDT));

        when(mockedVibeOptions.getGenePrioritizedOutputFormatWriterFactory())
                .thenReturn(GenePrioritizedOutputFormatWriterFactory.SIMPLE);
        when(mockedVibeOptions.getOutputWriter()).thenReturn(new StdoutOutputWriter());

        // Main goal is to ensure no errors are thrown, but output validation purely for gene order is present as well.
        // Note that while ontology retrieval is done, due to distance 0 the output is the same. This might change when
        // https://github.com/molgenis/vibe/issues/25 gets implemented.
        // Based on: GeneDiseaseCollectionRetrievalRunnerIT if sorted by GenePrioritizedOutputFormatWriterFactory.SIMPLE
        RunMode.GENES_FOR_PHENOTYPES_WITH_ASSOCIATED_PHENOTYPES.run(mockedVibeOptions);
        Assertions.assertEquals("29123,56172,2697,2475,23522,286,23028", outContent.toString());
    }
}
