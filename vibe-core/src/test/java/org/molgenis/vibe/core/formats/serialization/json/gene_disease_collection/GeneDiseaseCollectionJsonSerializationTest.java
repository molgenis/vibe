package org.molgenis.vibe.core.formats.serialization.json.gene_disease_collection;

import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.molgenis.vibe.core.formats.Disease;
import org.molgenis.vibe.core.formats.Gene;
import org.molgenis.vibe.core.formats.GeneDiseaseCollection;
import org.molgenis.vibe.core.formats.GeneDiseaseCombination;
import org.molgenis.vibe.core.formats.GeneSymbol;
import org.molgenis.vibe.core.formats.PubmedEvidence;
import org.molgenis.vibe.core.formats.Source;

import java.net.URI;
import java.util.Arrays;

class GeneDiseaseCollectionJsonSerializationTest {
    static GeneDiseaseCollection expectedGeneDiseaseCollection;
    static GeneDiseaseCollection gdcWithoutCombinationEvidence;
    static GeneDiseaseCollection gdcWithoutScore;

    // The data within the combinations JsonObject ordered according to these rules:
    // (integer means ordered as integer, not necessarily output is integer)
    // the combinations array: gene (integer, ascending), disease (string, ascending)
    // sources within a combination: name (string, ascending)
    // pubmed evidence from a single source: year (integer, descending) -> id (integer, ascending)

    // ncbigene, umls & sources JsonObjects were not ordered to reduce required processing power
    // (as otherwise each Set would need to be converted to a List first and then be sorted).
    static String expectedJson = "{\n" +
            "  \"ncbigene\": {\n" +
            "    \"1111111\": {\n" +
            "      \"hgnc\": \"AAA\"\n" +
            "    },\n" +
            "    \"2222222\": {\n" +
            "      \"hgnc\": \"BBB\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"umls\": {\n" +
            "    \"C2222222\": {\n" +
            "      \"name\": \"another disease name\"\n" +
            "    },\n" +
            "    \"C3333333\": {\n" +
            "      \"name\": \"yet another disease\"\n" +
            "    },\n" +
            "    \"C1111111\": {\n" +
            "      \"name\": \"a disease name\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"sources\": {\n" +
            "    \"Orphanet\": {\n" +
            "      \"fullName\": \"Orphanet dataset\",\n" +
            "      \"uri\": \"http://rdf.disgenet.org/v6.0.0/void/ORPHANET\",\n" +
            "      \"level\": \"curated\"\n" +
            "    },\n" +
            "    \"BeFree\": {\n" +
            "      \"fullName\": \"BeFree dataset\",\n" +
            "      \"uri\": \"http://rdf.disgenet.org/v6.0.0/void/BEFREE\",\n" +
            "      \"level\": \"literature\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"combinations\": [\n" +
            "    {\n" +
            "      \"ncbigene\": \"1111111\",\n" +
            "      \"umls\": \"C1111111\",\n" +
            "      \"score\": 0.5,\n" +
            "      \"sources\": [\n" +
            "        {\n" +
            "          \"name\": \"Orphanet\",\n" +
            "          \"count\": 1,\n" +
            "          \"pubmed\": []\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"ncbigene\": \"1111111\",\n" +
            "      \"umls\": \"C2222222\",\n" +
            "      \"score\": 0.3,\n" +
            "      \"sources\": [\n" +
            "        {\n" +
            "          \"name\": \"Orphanet\",\n" +
            "          \"count\": 1,\n" +
            "          \"pubmed\": []\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"ncbigene\": \"2222222\",\n" +
            "      \"umls\": \"C2222222\",\n" +
            "      \"score\": 0.6,\n" +
            "      \"sources\": [\n" +
            "        {\n" +
            "          \"name\": \"BeFree\",\n" +
            "          \"count\": 3,\n" +
            "          \"pubmed\": [\n" +
            "            {\n" +
            "              \"uri\": \"http://identifiers.org/pubmed/2\",\n" +
            "              \"year\": 2001\n" +
            "            },\n" +
            "            {\n" +
            "              \"uri\": \"http://identifiers.org/pubmed/3\",\n" +
            "              \"year\": 2001\n" +
            "            },\n" +
            "            {\n" +
            "              \"uri\": \"http://identifiers.org/pubmed/1\",\n" +
            "              \"year\": 2000\n" +
            "            }\n" +
            "          ]\n" +
            "        },\n" +
            "        {\n" +
            "          \"name\": \"Orphanet\",\n" +
            "          \"count\": 1,\n" +
            "          \"pubmed\": []\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"ncbigene\": \"2222222\",\n" +
            "      \"umls\": \"C3333333\",\n" +
            "      \"score\": 0.3,\n" +
            "      \"sources\": [\n" +
            "        {\n" +
            "          \"name\": \"Orphanet\",\n" +
            "          \"count\": 1,\n" +
            "          \"pubmed\": []\n" +
            "        }\n" +
            "      ]\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    static String jsonWithMissingGene = "{\n" +
            "  \"ncbigene\": {\n" +
            "    \"1111111\": {\n" +
            "      \"hgnc\": \"AAA\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"umls\": {\n" +
            "    \"C2222222\": {\n" +
            "      \"name\": \"another disease name\"\n" +
            "    },\n" +
            "    \"C3333333\": {\n" +
            "      \"name\": \"yet another disease\"\n" +
            "    },\n" +
            "    \"C1111111\": {\n" +
            "      \"name\": \"a disease name\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"sources\": {\n" +
            "    \"Orphanet\": {\n" +
            "      \"fullName\": \"Orphanet dataset\",\n" +
            "      \"uri\": \"http://rdf.disgenet.org/v6.0.0/void/ORPHANET\",\n" +
            "      \"level\": \"curated\"\n" +
            "    },\n" +
            "    \"BeFree\": {\n" +
            "      \"fullName\": \"BeFree dataset\",\n" +
            "      \"uri\": \"http://rdf.disgenet.org/v6.0.0/void/BEFREE\",\n" +
            "      \"level\": \"literature\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"combinations\": [\n" +
            "    {\n" +
            "      \"ncbigene\": \"1111111\",\n" +
            "      \"umls\": \"C1111111\",\n" +
            "      \"score\": 0.5,\n" +
            "      \"sources\": [\n" +
            "        {\n" +
            "          \"name\": \"Orphanet\",\n" +
            "          \"count\": 1,\n" +
            "          \"pubmed\": []\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"ncbigene\": \"1111111\",\n" +
            "      \"umls\": \"C2222222\",\n" +
            "      \"score\": 0.3,\n" +
            "      \"sources\": [\n" +
            "        {\n" +
            "          \"name\": \"Orphanet\",\n" +
            "          \"count\": 1,\n" +
            "          \"pubmed\": []\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"ncbigene\": \"2222222\",\n" +
            "      \"umls\": \"C2222222\",\n" +
            "      \"score\": 0.6,\n" +
            "      \"sources\": [\n" +
            "        {\n" +
            "          \"name\": \"BeFree\",\n" +
            "          \"count\": 3,\n" +
            "          \"pubmed\": [\n" +
            "            {\n" +
            "              \"uri\": \"http://identifiers.org/pubmed/2\",\n" +
            "              \"year\": 2001\n" +
            "            },\n" +
            "            {\n" +
            "              \"uri\": \"http://identifiers.org/pubmed/3\",\n" +
            "              \"year\": 2001\n" +
            "            },\n" +
            "            {\n" +
            "              \"uri\": \"http://identifiers.org/pubmed/1\",\n" +
            "              \"year\": 2000\n" +
            "            }\n" +
            "          ]\n" +
            "        },\n" +
            "        {\n" +
            "          \"name\": \"Orphanet\",\n" +
            "          \"count\": 1,\n" +
            "          \"pubmed\": []\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"ncbigene\": \"2222222\",\n" +
            "      \"umls\": \"C3333333\",\n" +
            "      \"score\": 0.3,\n" +
            "      \"sources\": [\n" +
            "        {\n" +
            "          \"name\": \"Orphanet\",\n" +
            "          \"count\": 1,\n" +
            "          \"pubmed\": []\n" +
            "        }\n" +
            "      ]\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    static String jsonWithEmptyGenes = "{\n" +
            "  \"ncbigene\": {\n" +
            "  },\n" +
            "  \"umls\": {\n" +
            "    \"C2222222\": {\n" +
            "      \"name\": \"another disease name\"\n" +
            "    },\n" +
            "    \"C3333333\": {\n" +
            "      \"name\": \"yet another disease\"\n" +
            "    },\n" +
            "    \"C1111111\": {\n" +
            "      \"name\": \"a disease name\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"sources\": {\n" +
            "    \"Orphanet\": {\n" +
            "      \"fullName\": \"Orphanet dataset\",\n" +
            "      \"uri\": \"http://rdf.disgenet.org/v6.0.0/void/ORPHANET\",\n" +
            "      \"level\": \"curated\"\n" +
            "    },\n" +
            "    \"BeFree\": {\n" +
            "      \"fullName\": \"BeFree dataset\",\n" +
            "      \"uri\": \"http://rdf.disgenet.org/v6.0.0/void/BEFREE\",\n" +
            "      \"level\": \"literature\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"combinations\": [\n" +
            "    {\n" +
            "      \"ncbigene\": \"1111111\",\n" +
            "      \"umls\": \"C1111111\",\n" +
            "      \"score\": 0.5,\n" +
            "      \"sources\": [\n" +
            "        {\n" +
            "          \"name\": \"Orphanet\",\n" +
            "          \"count\": 1,\n" +
            "          \"pubmed\": []\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"ncbigene\": \"1111111\",\n" +
            "      \"umls\": \"C2222222\",\n" +
            "      \"score\": 0.3,\n" +
            "      \"sources\": [\n" +
            "        {\n" +
            "          \"name\": \"Orphanet\",\n" +
            "          \"count\": 1,\n" +
            "          \"pubmed\": []\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"ncbigene\": \"2222222\",\n" +
            "      \"umls\": \"C2222222\",\n" +
            "      \"score\": 0.6,\n" +
            "      \"sources\": [\n" +
            "        {\n" +
            "          \"name\": \"BeFree\",\n" +
            "          \"count\": 3,\n" +
            "          \"pubmed\": [\n" +
            "            {\n" +
            "              \"uri\": \"http://identifiers.org/pubmed/2\",\n" +
            "              \"year\": 2001\n" +
            "            },\n" +
            "            {\n" +
            "              \"uri\": \"http://identifiers.org/pubmed/3\",\n" +
            "              \"year\": 2001\n" +
            "            },\n" +
            "            {\n" +
            "              \"uri\": \"http://identifiers.org/pubmed/1\",\n" +
            "              \"year\": 2000\n" +
            "            }\n" +
            "          ]\n" +
            "        },\n" +
            "        {\n" +
            "          \"name\": \"Orphanet\",\n" +
            "          \"count\": 1,\n" +
            "          \"pubmed\": []\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"ncbigene\": \"2222222\",\n" +
            "      \"umls\": \"C3333333\",\n" +
            "      \"score\": 0.3,\n" +
            "      \"sources\": [\n" +
            "        {\n" +
            "          \"name\": \"Orphanet\",\n" +
            "          \"count\": 1,\n" +
            "          \"pubmed\": []\n" +
            "        }\n" +
            "      ]\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    static String jsonWithoutDiseases = "{\n" +
            "  \"ncbigene\": {\n" +
            "    \"1111111\": {\n" +
            "      \"hgnc\": \"AAA\"\n" +
            "    },\n" +
            "    \"2222222\": {\n" +
            "      \"hgnc\": \"BBB\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"sources\": {\n" +
            "    \"Orphanet\": {\n" +
            "      \"fullName\": \"Orphanet dataset\",\n" +
            "      \"uri\": \"http://rdf.disgenet.org/v6.0.0/void/ORPHANET\",\n" +
            "      \"level\": \"curated\"\n" +
            "    },\n" +
            "    \"BeFree\": {\n" +
            "      \"fullName\": \"BeFree dataset\",\n" +
            "      \"uri\": \"http://rdf.disgenet.org/v6.0.0/void/BEFREE\",\n" +
            "      \"level\": \"literature\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"combinations\": [\n" +
            "    {\n" +
            "      \"ncbigene\": \"1111111\",\n" +
            "      \"umls\": \"C1111111\",\n" +
            "      \"score\": 0.5,\n" +
            "      \"sources\": [\n" +
            "        {\n" +
            "          \"name\": \"Orphanet\",\n" +
            "          \"count\": 1,\n" +
            "          \"pubmed\": []\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"ncbigene\": \"1111111\",\n" +
            "      \"umls\": \"C2222222\",\n" +
            "      \"score\": 0.3,\n" +
            "      \"sources\": [\n" +
            "        {\n" +
            "          \"name\": \"Orphanet\",\n" +
            "          \"count\": 1,\n" +
            "          \"pubmed\": []\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"ncbigene\": \"2222222\",\n" +
            "      \"umls\": \"C2222222\",\n" +
            "      \"score\": 0.6,\n" +
            "      \"sources\": [\n" +
            "        {\n" +
            "          \"name\": \"BeFree\",\n" +
            "          \"count\": 3,\n" +
            "          \"pubmed\": [\n" +
            "            {\n" +
            "              \"uri\": \"http://identifiers.org/pubmed/2\",\n" +
            "              \"year\": 2001\n" +
            "            },\n" +
            "            {\n" +
            "              \"uri\": \"http://identifiers.org/pubmed/3\",\n" +
            "              \"year\": 2001\n" +
            "            },\n" +
            "            {\n" +
            "              \"uri\": \"http://identifiers.org/pubmed/1\",\n" +
            "              \"year\": 2000\n" +
            "            }\n" +
            "          ]\n" +
            "        },\n" +
            "        {\n" +
            "          \"name\": \"Orphanet\",\n" +
            "          \"count\": 1,\n" +
            "          \"pubmed\": []\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"ncbigene\": \"2222222\",\n" +
            "      \"umls\": \"C3333333\",\n" +
            "      \"score\": 0.3,\n" +
            "      \"sources\": [\n" +
            "        {\n" +
            "          \"name\": \"Orphanet\",\n" +
            "          \"count\": 1,\n" +
            "          \"pubmed\": []\n" +
            "        }\n" +
            "      ]\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    static String jsonWithhoutCombinations = "{\n" +
            "  \"ncbigene\": {\n" +
            "    \"1111111\": {\n" +
            "      \"hgnc\": \"AAA\"\n" +
            "    },\n" +
            "    \"2222222\": {\n" +
            "      \"hgnc\": \"BBB\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"umls\": {\n" +
            "    \"C2222222\": {\n" +
            "      \"name\": \"another disease name\"\n" +
            "    },\n" +
            "    \"C3333333\": {\n" +
            "      \"name\": \"yet another disease\"\n" +
            "    },\n" +
            "    \"C1111111\": {\n" +
            "      \"name\": \"a disease name\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"sources\": {\n" +
            "    \"Orphanet\": {\n" +
            "      \"fullName\": \"Orphanet dataset\",\n" +
            "      \"uri\": \"http://rdf.disgenet.org/v6.0.0/void/ORPHANET\",\n" +
            "      \"level\": \"curated\"\n" +
            "    },\n" +
            "    \"BeFree\": {\n" +
            "      \"fullName\": \"BeFree dataset\",\n" +
            "      \"uri\": \"http://rdf.disgenet.org/v6.0.0/void/BEFREE\",\n" +
            "      \"level\": \"literature\"\n" +
            "    }\n" +
            "  }\n" +
            "}";

    static String jSonWithoutCombinationEvidence = "{\n" +
            "  \"ncbigene\": {\n" +
            "    \"1111111\": {\n" +
            "      \"hgnc\": \"AAA\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"umls\": {\n" +
            "    \"C1111111\": {\n" +
            "      \"name\": \"a disease name\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"sources\": {},\n" +
            "  \"combinations\": [\n" +
            "    {\n" +
            "      \"ncbigene\": \"1111111\",\n" +
            "      \"umls\": \"C1111111\",\n" +
            "      \"score\": 0.0,\n" +
            "      \"sources\": []\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    static String jSonWithoutScore = "{\n" +
            "  \"ncbigene\": {\n" +
            "    \"1111111\": {\n" +
            "      \"hgnc\": \"AAA\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"umls\": {\n" +
            "    \"C1111111\": {\n" +
            "      \"name\": \"a disease name\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"sources\": {\n" +
            "    \"Orphanet\": {\n" +
            "      \"fullName\": \"Orphanet dataset\",\n" +
            "      \"uri\": \"http://rdf.disgenet.org/v6.0.0/void/ORPHANET\",\n" +
            "      \"level\": \"curated\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"combinations\": [\n" +
            "    {\n" +
            "      \"ncbigene\": \"1111111\",\n" +
            "      \"umls\": \"C1111111\",\n" +
            "      \"sources\": [\n" +
            "        {\n" +
            "          \"name\": \"Orphanet\",\n" +
            "          \"count\": 1,\n" +
            "          \"pubmed\": []\n" +
            "        }\n" +
            "      ]\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    @BeforeAll
    static void beforeAll() {
        // expectedJson
        Gene[] genes = new Gene[]{
                new Gene("ncbigene:1111111", new GeneSymbol("hgnc:AAA")),
                new Gene("ncbigene:2222222", new GeneSymbol("hgnc:BBB"))
        };

        Disease[] diseases = new Disease[]{
                new Disease("umls:C1111111", "a disease name"),
                new Disease("umls:C2222222", "another disease name"),
                new Disease("umls:C3333333", "yet another disease")
        };

        GeneDiseaseCombination[] gdcs = new GeneDiseaseCombination[]{
                new GeneDiseaseCombination(genes[0], diseases[0], 0.5), // only counts
                new GeneDiseaseCombination(genes[0], diseases[1], 0.3), // only counts
                new GeneDiseaseCombination(genes[1], diseases[1], 0.6), // includes pubmeds
                new GeneDiseaseCombination(genes[1], diseases[2], 0.3) // only counts
        };

        Source[] sources = new Source[]{
                new Source(URI.create("http://rdf.disgenet.org/v6.0.0/void/ORPHANET"), "Orphanet dataset", Source.Level.CURATED),
                new Source(URI.create("http://rdf.disgenet.org/v6.0.0/void/BEFREE"), "BeFree dataset", Source.Level.LITERATURE)
        };

        // Adds counts.
        gdcs[0].add(sources[0]);
        gdcs[1].add(sources[0]);
        gdcs[3].add(sources[0]);

        // Adds pubmed sources.
        gdcs[2].add(sources[0]);
        gdcs[2].add(sources[1], new PubmedEvidence(URI.create("http://identifiers.org/pubmed/1"), 2000));
        gdcs[2].add(sources[1], new PubmedEvidence(URI.create("http://identifiers.org/pubmed/3"), 2001));
        gdcs[2].add(sources[1], new PubmedEvidence(URI.create("http://identifiers.org/pubmed/2"), 2001));

        // Generates GeneDiseaseCollection.
        expectedGeneDiseaseCollection = new GeneDiseaseCollection();
        expectedGeneDiseaseCollection.addAll(Arrays.asList(gdcs));

        // jSonWithoutCombinationEvidence
        gdcWithoutCombinationEvidence = new GeneDiseaseCollection();
        gdcWithoutCombinationEvidence.add(new GeneDiseaseCombination(genes[0], diseases[0], 0.0));

        // jsonWithoutScore
        gdcWithoutScore = new GeneDiseaseCollection();
        GeneDiseaseCombination combinationNoScore = new GeneDiseaseCombination(genes[0], diseases[0]);
        combinationNoScore.add(sources[0]);
        gdcWithoutScore.add(combinationNoScore);
    }

    /**
     * Get {@link GsonBuilder} that formats data so that testing output adheres to {@code expectedJsonOutput}.
     * @return
     */
    static GsonBuilder generateGsonBuilder() {
        return new GsonBuilder().setPrettyPrinting();
    }
}
