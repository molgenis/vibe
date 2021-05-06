package org.molgenis.vibe.core;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.molgenis.vibe.core.formats.*;
import org.molgenis.vibe.core.io.input.ModelReaderFactory;
import org.molgenis.vibe.core.io.input.VibeDatabase;

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
    private static final String DISGENET_VERSION = "v7.0.0";

    @Test
    void retrieveGeneDiseaseCollectionForHpo0008438() throws IOException {
        // Diseases.
        Map<String,Disease> diseases = new HashMap<>();
        diseases.put("C0265292", new Disease("umls:C0265292", "Schwartz-Lelek syndrome")); // ordo:Orphanet_85184
        diseases.put("C0220687", new Disease("umls:C0220687", "KBG syndrome")); // pda:DGN8a0e701b56199eca12831b38431d7959
        diseases.put("C1835764", new Disease("umls:C1835764", "Vertebral arch anomaly")); // skos:exactMatch

        // Genes.
        Map<String,Gene> genes = new HashMap<>();
        genes.put("2475", new Gene("ncbigene:2475", new GeneSymbol("hgnc:MTOR")));
        genes.put("29123", new Gene("ncbigene:29123", new GeneSymbol("hgnc:ANKRD11")));
        genes.put("23522", new Gene("ncbigene:23522", new GeneSymbol("hgnc:KAT6B")));
        genes.put("23028", new Gene("ncbigene:23028", new GeneSymbol("hgnc:KDM1A")));
        genes.put("56172", new Gene("ncbigene:56172", new GeneSymbol("hgnc:ANKH")));
        genes.put("286", new Gene("ncbigene:286", new GeneSymbol("hgnc:ANK1")));
        genes.put("2697", new Gene("ncbigene:2697", new GeneSymbol("hgnc:GJA1")));

        // Combinations.
        Map<String,GeneDiseaseCombination> geneDiseaseCombinations = new HashMap<>();
        geneDiseaseCombinations.put("2475-C1835764", new GeneDiseaseCombination(genes.get("2475"), diseases.get("C1835764"), 0.1E0));
        geneDiseaseCombinations.put("29123-C1835764", new GeneDiseaseCombination(genes.get("29123"), diseases.get("C1835764"), 0.1E0));

        geneDiseaseCombinations.put("29123-C0220687", new GeneDiseaseCombination(genes.get("29123"), diseases.get("C0220687"), 0.8E0));
        geneDiseaseCombinations.put("23522-C0220687", new GeneDiseaseCombination(genes.get("23522"), diseases.get("C0220687"), 0.1E0));
        geneDiseaseCombinations.put("23028-C0220687", new GeneDiseaseCombination(genes.get("23028"), diseases.get("C0220687"), 0.01E0));

        geneDiseaseCombinations.put("56172-C0265292", new GeneDiseaseCombination(genes.get("56172"), diseases.get("C0265292"), 0.38E0));
        geneDiseaseCombinations.put("286-C0265292", new GeneDiseaseCombination(genes.get("286"), diseases.get("C0265292"), 0.04E0));
        geneDiseaseCombinations.put("2697-C0265292", new GeneDiseaseCombination(genes.get("2697"), diseases.get("C0265292"), 0.31E0));

        // Sources.
        HashMap<String,Source> sources = new HashMap<>();
        sources.put("BEFREE", new Source(URI.create("http://rdf.disgenet.org/" + DISGENET_VERSION + "/void/BEFREE"), "BeFree 2018 Dataset Distribution", Source.Level.LITERATURE));
        sources.put("ORPHANET", new Source(URI.create("http://rdf.disgenet.org/" + DISGENET_VERSION + "/void/ORPHANET"), "Orphanet 2017 Dataset Distribution", Source.Level.CURATED));
        sources.put("CLINVAR", new Source(URI.create("http://rdf.disgenet.org/" + DISGENET_VERSION + "/void/CLINVAR"), "ClinVar 2018 Dataset Distribution", Source.Level.CURATED));
        sources.put("UNIPROT", new Source(URI.create("http://rdf.disgenet.org/" + DISGENET_VERSION + "/void/UNIPROT"), "UniProt 2018 Dataset Distribution", Source.Level.CURATED));
        sources.put("CTD_human", new Source(URI.create("http://rdf.disgenet.org/" + DISGENET_VERSION + "/void/CTD_human"), "CTD_human 2018 Dataset Distribution", Source.Level.CURATED));
        sources.put("HPO", new Source(URI.create("http://rdf.disgenet.org/" + DISGENET_VERSION + "/void/HPO"), "HPO", Source.Level.CURATED));

        // Add info to combinations.
        geneDiseaseCombinations.get("2475-C1835764").add(sources.get("CLINVAR"), new PubmedEvidence(URI.create("http://identifiers.org/pubmed/27159400"), 2016));
        geneDiseaseCombinations.get("29123-C1835764").add(sources.get("HPO"));

        geneDiseaseCombinations.get("29123-C0220687").add(sources.get("BEFREE"), new PubmedEvidence(URI.create("http://identifiers.org/pubmed/28250421"), 2017));
        geneDiseaseCombinations.get("29123-C0220687").add(sources.get("BEFREE"), new PubmedEvidence(URI.create("http://identifiers.org/pubmed/27667800"), 2016));
        geneDiseaseCombinations.get("29123-C0220687").add(sources.get("UNIPROT"), new PubmedEvidence(URI.create("http://identifiers.org/pubmed/21782149"), 2011));
        geneDiseaseCombinations.get("29123-C0220687").add(sources.get("BEFREE"), new PubmedEvidence(URI.create("http://identifiers.org/pubmed/25464108"), 2015));
        geneDiseaseCombinations.get("29123-C0220687").add(sources.get("CLINVAR"), new PubmedEvidence(URI.create("http://identifiers.org/pubmed/27824329"), 2016));
        geneDiseaseCombinations.get("29123-C0220687").add(sources.get("BEFREE"), new PubmedEvidence(URI.create("http://identifiers.org/pubmed/27605097"), 2016));
        geneDiseaseCombinations.get("29123-C0220687").add(sources.get("BEFREE"), new PubmedEvidence(URI.create("http://identifiers.org/pubmed/24838796"), 2014));
        geneDiseaseCombinations.get("29123-C0220687").add(sources.get("CTD_human"));
        geneDiseaseCombinations.get("29123-C0220687").add(sources.get("BEFREE"), new PubmedEvidence(URI.create("http://identifiers.org/pubmed/30877071"), 2019));
        geneDiseaseCombinations.get("29123-C0220687").add(sources.get("BEFREE"), new PubmedEvidence(URI.create("http://identifiers.org/pubmed/25424714"), 2015));
        geneDiseaseCombinations.get("29123-C0220687").add(sources.get("BEFREE"), new PubmedEvidence(URI.create("http://identifiers.org/pubmed/29696793"), 2018));
        geneDiseaseCombinations.get("29123-C0220687").add(sources.get("CLINVAR"));
        geneDiseaseCombinations.get("29123-C0220687").add(sources.get("BEFREE"), new PubmedEvidence(URI.create("http://identifiers.org/pubmed/25125236"), 2014));
        geneDiseaseCombinations.get("29123-C0220687").add(sources.get("BEFREE"), new PubmedEvidence(URI.create("http://identifiers.org/pubmed/30088855"), 2018));
        geneDiseaseCombinations.get("29123-C0220687").add(sources.get("BEFREE"), new PubmedEvidence(URI.create("http://identifiers.org/pubmed/30642272"), 2019));
        geneDiseaseCombinations.get("29123-C0220687").add(sources.get("BEFREE"), new PubmedEvidence(URI.create("http://identifiers.org/pubmed/31566922"), 2019));
        geneDiseaseCombinations.get("29123-C0220687").add(sources.get("BEFREE"), new PubmedEvidence(URI.create("http://identifiers.org/pubmed/28422132"), 2017));
        geneDiseaseCombinations.get("29123-C0220687").add(sources.get("BEFREE"), new PubmedEvidence(URI.create("http://identifiers.org/pubmed/23184435"), 2013));
        geneDiseaseCombinations.get("29123-C0220687").add(sources.get("BEFREE"), new PubmedEvidence(URI.create("http://identifiers.org/pubmed/23494856"), 2013));
        geneDiseaseCombinations.get("29123-C0220687").add(sources.get("BEFREE"), new PubmedEvidence(URI.create("http://identifiers.org/pubmed/23369839"), 2013));
        geneDiseaseCombinations.get("29123-C0220687").add(sources.get("BEFREE"), new PubmedEvidence(URI.create("http://identifiers.org/pubmed/25413698"), 2015));
        geneDiseaseCombinations.get("23522-C0220687").add(sources.get("CLINVAR"));
        geneDiseaseCombinations.get("29123-C0220687").add(sources.get("BEFREE"), new PubmedEvidence(URI.create("http://identifiers.org/pubmed/25187894"), 2014));
        geneDiseaseCombinations.get("23028-C0220687").add(sources.get("BEFREE"), new PubmedEvidence(URI.create("http://identifiers.org/pubmed/24838796"), 2014));
        geneDiseaseCombinations.get("29123-C0220687").add(sources.get("BEFREE"), new PubmedEvidence(URI.create("http://identifiers.org/pubmed/29224748"), 2017));
        geneDiseaseCombinations.get("29123-C0220687").add(sources.get("BEFREE"), new PubmedEvidence(URI.create("http://identifiers.org/pubmed/22307766"), 2012));
        geneDiseaseCombinations.get("29123-C0220687").add(sources.get("BEFREE"), new PubmedEvidence(URI.create("http://identifiers.org/pubmed/29274743"), 2018));
        geneDiseaseCombinations.get("29123-C0220687").add(sources.get("BEFREE"), new PubmedEvidence(URI.create("http://identifiers.org/pubmed/23463723"), 2013));
        geneDiseaseCombinations.get("29123-C0220687").add(sources.get("CLINVAR"), new PubmedEvidence(URI.create("http://identifiers.org/pubmed/27605097"), 2016));
        geneDiseaseCombinations.get("29123-C0220687").add(sources.get("CLINVAR"), new PubmedEvidence(URI.create("http://identifiers.org/pubmed/27667800"), 2016));
        geneDiseaseCombinations.get("29123-C0220687").add(sources.get("CLINVAR"), new PubmedEvidence(URI.create("http://identifiers.org/pubmed/21782149"), 2011));
        geneDiseaseCombinations.get("29123-C0220687").add(sources.get("BEFREE"), new PubmedEvidence(URI.create("http://identifiers.org/pubmed/28449295"), 2017));
        geneDiseaseCombinations.get("29123-C0220687").add(sources.get("BEFREE"), new PubmedEvidence(URI.create("http://identifiers.org/pubmed/21782149"), 2011));
        geneDiseaseCombinations.get("29123-C0220687").add(sources.get("UNIPROT"), new PubmedEvidence(URI.create("http://identifiers.org/pubmed/25413698"), 2015));
        geneDiseaseCombinations.get("29123-C0220687").add(sources.get("ORPHANET"), new PubmedEvidence(URI.create("http://identifiers.org/pubmed/23184435"), 2013));
        geneDiseaseCombinations.get("29123-C0220687").add(sources.get("ORPHANET"), new PubmedEvidence(URI.create("http://identifiers.org/pubmed/21782149"), 2011));
        geneDiseaseCombinations.get("29123-C0220687").add(sources.get("CLINVAR"), new PubmedEvidence(URI.create("http://identifiers.org/pubmed/26633545"), 2016));

        geneDiseaseCombinations.get("56172-C0265292").add(sources.get("BEFREE"), new PubmedEvidence(URI.create("http://identifiers.org/pubmed/20358596"), 2010));
        geneDiseaseCombinations.get("56172-C0265292").add(sources.get("BEFREE"), new PubmedEvidence(URI.create("http://identifiers.org/pubmed/26820766"), 2016));
        geneDiseaseCombinations.get("56172-C0265292").add(sources.get("BEFREE"), new PubmedEvidence(URI.create("http://identifiers.org/pubmed/16462526"), 2006));
        geneDiseaseCombinations.get("286-C0265292").add(sources.get("BEFREE"), new PubmedEvidence(URI.create("http://identifiers.org/pubmed/16462526"), 2006));
        geneDiseaseCombinations.get("286-C0265292").add(sources.get("BEFREE"), new PubmedEvidence(URI.create("http://identifiers.org/pubmed/21149338"), 2011));
        geneDiseaseCombinations.get("286-C0265292").add(sources.get("BEFREE"), new PubmedEvidence(URI.create("http://identifiers.org/pubmed/19257826"), 2009));
        geneDiseaseCombinations.get("56172-C0265292").add(sources.get("BEFREE"), new PubmedEvidence(URI.create("http://identifiers.org/pubmed/21149338"), 2011));
        geneDiseaseCombinations.get("56172-C0265292").add(sources.get("BEFREE"), new PubmedEvidence(URI.create("http://identifiers.org/pubmed/29056330"), 2017));
        geneDiseaseCombinations.get("56172-C0265292").add(sources.get("ORPHANET"), new PubmedEvidence(URI.create("http://identifiers.org/pubmed/11326272"), 2001));
        geneDiseaseCombinations.get("2697-C0265292").add(sources.get("ORPHANET"), new PubmedEvidence(URI.create("http://identifiers.org/pubmed/23951358"), 2013));
        geneDiseaseCombinations.get("2697-C0265292").add(sources.get("BEFREE"), new PubmedEvidence(URI.create("http://identifiers.org/pubmed/23951358"), 2013));
        geneDiseaseCombinations.get("56172-C0265292").add(sources.get("ORPHANET"), new PubmedEvidence(URI.create("http://identifiers.org/pubmed/11326338"), 2001));
        geneDiseaseCombinations.get("56172-C0265292").add(sources.get("BEFREE"), new PubmedEvidence(URI.create("http://identifiers.org/pubmed/19257826"), 2009));
        geneDiseaseCombinations.get("56172-C0265292").add(sources.get("BEFREE"), new PubmedEvidence(URI.create("http://identifiers.org/pubmed/20186813"), 2010));
        geneDiseaseCombinations.get("56172-C0265292").add(sources.get("BEFREE"), new PubmedEvidence(URI.create("http://identifiers.org/pubmed/20943778"), 2011));
        geneDiseaseCombinations.get("286-C0265292").add(sources.get("BEFREE"), new PubmedEvidence(URI.create("http://identifiers.org/pubmed/20358596"), 2010));
        geneDiseaseCombinations.get("56172-C0265292").add(sources.get("ORPHANET"), new PubmedEvidence(URI.create("http://identifiers.org/pubmed/21438135"), 2011));

        // Collection.
        GeneDiseaseCollection expectedCollection = new GeneDiseaseCollection();
        for(GeneDiseaseCombination gda:geneDiseaseCombinations.values()) {
            expectedCollection.add(gda);
        }

        System.out.println();


        runner = new GeneDiseaseCollectionRetrievalRunner(
                new VibeDatabase(TestData.HDT.getFullPath(), ModelReaderFactory.HDT),
                new HashSet<>(Arrays.asList(new Phenotype("hp:0008438")))
        );
        GeneDiseaseCollection actualCollection = runner.call();

        System.out.println(expectedCollection.toString());
        System.out.println(actualCollection.toString());

        Assertions.assertAll(
            () -> Assertions.assertEquals(expectedCollection, actualCollection),
            () -> Assertions.assertTrue(expectedCollection.allFieldsEquals(actualCollection))
        );
    }
}
