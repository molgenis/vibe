package org.molgenis.vibe.core;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.molgenis.vibe.core.formats.*;

import java.io.IOException;
import java.net.URI;
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
class GeneDiseaseCollectionRetrievalRunnerIT {
    private static GeneDiseaseCollectionRetrievalRunner runner;

    @Test
    void retrieveGeneDiseaseCollectionForHpo0008438() throws IOException {
        /**
         * obo:HP_0008438  a        sio:SIO_010056 ;
         *         sio:SIO_000001   ordo:Orphanet_85184 ;
         *         sio:SIO_000212   pda:DGN8a0e701b56199eca12831b38431d7959 ;
         *         skos:exactMatch  umls:C1835764 .
         */
        Disease[] diseases = new Disease[]{
                new Disease("umls:C0265292", "Schwartz-Lelek syndrome"), // ordo:Orphanet_85184
                new Disease("umls:C0220687", "KBG syndrome"), // pda:DGN8a0e701b56199eca12831b38431d7959
                new Disease("umls:C1835764", "Vertebral arch anomaly"), // skos:exactMatch
        };

        Gene[] genes = new Gene[]{
                new Gene("ncbigene:479", new GeneSymbol("hgnc:ATP12A")),
                new Gene("ncbigene:56172", new GeneSymbol("hgnc:ANKH")),
                new Gene("ncbigene:286", new GeneSymbol("hgnc:ANK1")),
                new Gene("ncbigene:2697", new GeneSymbol("hgnc:GJA1")),
                new Gene("ncbigene:29123", new GeneSymbol("hgnc:ANKRD11")),
        };

        GeneDiseaseCombination[] geneDiseaseCombinations = new GeneDiseaseCombination[]{
                // GDAs umls:C0265292
                new GeneDiseaseCombination(genes[0], diseases[0], 0.02E0),
                new GeneDiseaseCombination(genes[1], diseases[0], 0.37E0),
                new GeneDiseaseCombination(genes[2], diseases[0], 0.03E0),
                new GeneDiseaseCombination(genes[3], diseases[0], 0.31E0),

                // GDAs umls:C0220687
                new GeneDiseaseCombination(genes[4], diseases[1], 0.8E0), // [4]

                // GDAs umls:C1835764
                new GeneDiseaseCombination(genes[4], diseases[2], 0.1E0), // [5]
        };

        Source[] sources = new Source[] {
                new Source(URI.create("http://rdf.disgenet.org/v6.0.0/void/BEFREE"), "BeFree 2018 Dataset Distribution", Source.Level.LITERATURE),
                new Source(URI.create("http://rdf.disgenet.org/v6.0.0/void/ORPHANET"), "Orphanet 2017 Dataset Distribution", Source.Level.CURATED),
                new Source(URI.create("http://rdf.disgenet.org/v6.0.0/void/CLINVAR"), "ClinVar 2018 Dataset Distribution", Source.Level.CURATED),
                new Source(URI.create("http://rdf.disgenet.org/v6.0.0/void/UNIPROT"), "UniProt 2018 Dataset Distribution", Source.Level.CURATED),
                new Source(URI.create("http://rdf.disgenet.org/v6.0.0/void/CTD_human"), "CTD_human 2018 Dataset Distribution", Source.Level.CURATED),
                new Source(URI.create("http://rdf.disgenet.org/v6.0.0/void/HPO"), "HPO", Source.Level.CURATED),
        };

        // GDAs umls:C0265292
        geneDiseaseCombinations[0].add(sources[0], new PubmedEvidence(URI.create("http://identifiers.org/pubmed/8630510"), 1996));
        geneDiseaseCombinations[0].add(sources[0], new PubmedEvidence(URI.create("http://identifiers.org/pubmed/7678608"), 1993));
        geneDiseaseCombinations[1].add(sources[0], new PubmedEvidence(URI.create("http://identifiers.org/pubmed/16462526"), 2006));
        geneDiseaseCombinations[1].add(sources[0], new PubmedEvidence(URI.create("http://identifiers.org/pubmed/20943778"), 2011));
        geneDiseaseCombinations[1].add(sources[0], new PubmedEvidence(URI.create("http://identifiers.org/pubmed/20358596"), 2010));
        geneDiseaseCombinations[1].add(sources[0], new PubmedEvidence(URI.create("http://identifiers.org/pubmed/19257826"), 2009));
        geneDiseaseCombinations[1].add(sources[0], new PubmedEvidence(URI.create("http://identifiers.org/pubmed/21149338"), 2011));
        geneDiseaseCombinations[2].add(sources[0], new PubmedEvidence(URI.create("http://identifiers.org/pubmed/16462526"), 2006));
        geneDiseaseCombinations[1].add(sources[0], new PubmedEvidence(URI.create("http://identifiers.org/pubmed/26820766"), 2016));
        geneDiseaseCombinations[1].add(sources[1]);
        geneDiseaseCombinations[3].add(sources[1]);
        geneDiseaseCombinations[3].add(sources[0], new PubmedEvidence(URI.create("http://identifiers.org/pubmed/23951358"), 2014));
        geneDiseaseCombinations[1].add(sources[0], new PubmedEvidence(URI.create("http://identifiers.org/pubmed/20186813"), 2010));
        geneDiseaseCombinations[2].add(sources[0], new PubmedEvidence(URI.create("http://identifiers.org/pubmed/19257826"), 2009));
        geneDiseaseCombinations[2].add(sources[0], new PubmedEvidence(URI.create("http://identifiers.org/pubmed/21149338"), 2011));

        // GDAs umls:C0220687
        geneDiseaseCombinations[4].add(sources[0], new PubmedEvidence(URI.create("http://identifiers.org/pubmed/25413698"), 2015));
        geneDiseaseCombinations[4].add(sources[0], new PubmedEvidence(URI.create("http://identifiers.org/pubmed/28250421"), 2018));
        geneDiseaseCombinations[4].add(sources[0], new PubmedEvidence(URI.create("http://identifiers.org/pubmed/23369839"), 2014));
        geneDiseaseCombinations[4].add(sources[2], new PubmedEvidence(URI.create("http://identifiers.org/pubmed/25741868"), 2015));
        geneDiseaseCombinations[4].add(sources[2], new PubmedEvidence(URI.create("http://identifiers.org/pubmed/15523620"), 2004));
        geneDiseaseCombinations[4].add(sources[2], new PubmedEvidence(URI.create("http://identifiers.org/pubmed/27900361"), 2016));
        geneDiseaseCombinations[4].add(sources[2], new PubmedEvidence(URI.create("http://identifiers.org/pubmed/15378538"), 2005));
        geneDiseaseCombinations[4].add(sources[2], new PubmedEvidence(URI.create("http://identifiers.org/pubmed/21782149"), 2011));
        geneDiseaseCombinations[4].add(sources[3], new PubmedEvidence(URI.create("http://identifiers.org/pubmed/21782149"), 2011));
        geneDiseaseCombinations[4].add(sources[0], new PubmedEvidence(URI.create("http://identifiers.org/pubmed/23184435"), 2013));
        geneDiseaseCombinations[4].add(sources[0], new PubmedEvidence(URI.create("http://identifiers.org/pubmed/25424714"), 2016));
        geneDiseaseCombinations[4].add(sources[0], new PubmedEvidence(URI.create("http://identifiers.org/pubmed/22307766"), 2012));
        geneDiseaseCombinations[4].add(sources[0], new PubmedEvidence(URI.create("http://identifiers.org/pubmed/21782149"), 2011));
        geneDiseaseCombinations[4].add(sources[2], new PubmedEvidence(URI.create("http://identifiers.org/pubmed/27667800"), 2017));
        geneDiseaseCombinations[4].add(sources[2], new PubmedEvidence(URI.create("http://identifiers.org/pubmed/29565525"), 1993));
        geneDiseaseCombinations[4].add(sources[4]);
        geneDiseaseCombinations[4].add(sources[3], new PubmedEvidence(URI.create("http://identifiers.org/pubmed/25413698"), 2015));
        geneDiseaseCombinations[4].add(sources[2]);
        geneDiseaseCombinations[4].add(sources[0], new PubmedEvidence(URI.create("http://identifiers.org/pubmed/25464108"), 2015));
        geneDiseaseCombinations[4].add(sources[2], new PubmedEvidence(URI.create("http://identifiers.org/pubmed/28708303"), 2018));
        geneDiseaseCombinations[4].add(sources[0], new PubmedEvidence(URI.create("http://identifiers.org/pubmed/25125236"), 2015));
        geneDiseaseCombinations[4].add(sources[0], new PubmedEvidence(URI.create("http://identifiers.org/pubmed/25187894"), 2015));
        geneDiseaseCombinations[4].add(sources[0], new PubmedEvidence(URI.create("http://identifiers.org/pubmed/24838796"), 2015));
        geneDiseaseCombinations[4].add(sources[2], new PubmedEvidence(URI.create("http://identifiers.org/pubmed/24088041"), 2013));
        geneDiseaseCombinations[4].add(sources[2], new PubmedEvidence(URI.create("http://identifiers.org/pubmed/25125236"), 2015));
        geneDiseaseCombinations[4].add(sources[0], new PubmedEvidence(URI.create("http://identifiers.org/pubmed/29224748"), 2018));
        geneDiseaseCombinations[4].add(sources[0], new PubmedEvidence(URI.create("http://identifiers.org/pubmed/23463723"), 2013));
        geneDiseaseCombinations[4].add(sources[0], new PubmedEvidence(URI.create("http://identifiers.org/pubmed/23494856"), 2013));
        geneDiseaseCombinations[4].add(sources[2], new PubmedEvidence(URI.create("http://identifiers.org/pubmed/26633545"), 2017));
        geneDiseaseCombinations[4].add(sources[1]);

        // GDAs umls:C1835764
        geneDiseaseCombinations[5].add(sources[5]);

        // Create collection.
        GeneDiseaseCollection expectedCollection = new GeneDiseaseCollection();
        expectedCollection.addAll(Arrays.asList(geneDiseaseCombinations));

        runner = new GeneDiseaseCollectionRetrievalRunner(
                TestData.TDB.getFullPath(), new HashSet<>(Arrays.asList(new Phenotype("hp:0008438"))));
        GeneDiseaseCollection actualCollection = runner.call();

        Assertions.assertAll(
            () -> Assertions.assertEquals(expectedCollection, actualCollection),
            () -> Assertions.assertTrue(expectedCollection.allFieldsEquals(actualCollection))
        );
    }

    @Test
    void testPubmedSourcesForHavingMoreCountsThanPubmeds() throws IOException {
        runner = new GeneDiseaseCollectionRetrievalRunner(
                TestData.TDB.getFullPath(), new HashSet<>(Arrays.asList(new Phenotype("hp:0002996"))));
        GeneDiseaseCollection collection = runner.call();

        Assertions.assertTrue(assertCountsEqualToPubmeds(collection));
    }

    private boolean assertCountsEqualToPubmeds(GeneDiseaseCollection collection) {
        Source clinvar = new Source(URI.create("http://rdf.disgenet.org/v6.0.0/void/CLINVAR"),
                "ClinVar 2018 Dataset Distribution", Source.Level.CURATED);

        for(GeneDiseaseCombination combination : collection.getGeneDiseaseCombinations()) {
            for(Source source : combination.getSourcesWithPubmedEvidence()) {
                // Currently skips ClinVar due to sometimes having multiple GDAs with same PubMed.
                if(source.equals(clinvar)) continue;

                // If comparison fails, prints information to stderr and returns false.
                if(combination.getPubmedEvidenceForSource(source).size() != combination.getCountForSource(source)) {
                    System.err.println(combination.getGene().getFormattedId());
                    System.err.println(combination.getDisease().getFormattedId());
                    System.err.println(source.getName());
                    System.err.println(combination.getCountForSource(source));
                    System.err.println(combination.getPubmedEvidenceForSource(source).size());
                    System.err.println(combination.getPubmedEvidenceForSource(source).toString());
                    return false;
                }
            }
        }
        return true;
    }
}
