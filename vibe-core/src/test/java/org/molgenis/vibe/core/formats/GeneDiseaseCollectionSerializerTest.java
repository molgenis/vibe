package org.molgenis.vibe.core.formats;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Arrays;

class GeneDiseaseCollectionSerializerTest {
    private static GeneDiseaseCollection geneDiseaseCollection;
    private static Gson gson;

    @BeforeAll
    static void beforeAll() {
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
        geneDiseaseCollection = new GeneDiseaseCollection();
        geneDiseaseCollection.addAll(Arrays.asList(gdcs));

        // Prepares Gson with custom (de)serializer.
        GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting();
        gsonBuilder.registerTypeAdapter(GeneDiseaseCollection.class, new GeneDiseaseCollectionSerializer());
        gsonBuilder.registerTypeAdapter(GeneDiseaseCollection.class, new GeneDiseaseCollectionDeserializer());
        gson = gsonBuilder.create();
    }

    /**
     * Tests generated Json. Json arrays should be ordered as following:
     * - gene-disease combinations: gene (integer, ascending) -> disease (string, ascending)
     * - sources: full name (string, ascending)
     * - pubmed evidence: year (int, descending) -> id (integer, ascending)
     */
    @Test
    void testSerializer() {
        String actualOutput = gson.toJson(geneDiseaseCollection);

        // ### Json combinations arrays ordering (integer means ordered as integer, not necessarily output is integer) ###
        // gene-disease combinations: gene (integer, ascending), disease (string, ascending)
        // sources: name (string, ascending)
        // pubmed evidence: year (integer, descending) -> id (integer, ascending)

        // ncbigene, umls & sources JsonObjects were not ordered to reduce required processing power (Set -> List -> sort() ).
        String expectedOutput = "{\n" +
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
                "  ],\n" +
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

        Assertions.assertEquals(expectedOutput, actualOutput);
    }

    /**
     * Goal of custom Gson is to generate less data than if using the default Gson class parser (by eliminating
     * duplicate data caused by highly the interlinked design).
     */
    @Test
    void testIfCustomIsSmaller() {
        // Gson without custom (de)serializer.
        GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting();
        Gson defaultJson = gsonBuilder.create();

        String defaultOutput = defaultJson.toJson(geneDiseaseCollection);
        String customOutput = gson.toJson(geneDiseaseCollection);

        Assertions.assertTrue(customOutput.length() < defaultOutput.length());
    }

    /**
     * Validates that when serializing and deserializing a {@link GeneDiseaseCollection},
     * an identical {@link GeneDiseaseCollection} is generated.
     */
    @Test
    void testIfObjectIsFullyEqualAfterBeingSerializedAndDeserialized() {
        String collectionAsJson = gson.toJson(geneDiseaseCollection);
        GeneDiseaseCollection returnedCollection = gson.fromJson(collectionAsJson, GeneDiseaseCollection.class);

        Assertions.assertTrue(returnedCollection.allFieldsEquals(geneDiseaseCollection));
    }

}
