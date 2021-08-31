package org.molgenis.vibe.core.formats.serialization.json.gene_disease_collection;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.molgenis.vibe.core.formats.GeneDiseaseCollection;

public class GeneDiseaseCollectionJsonMemoryDeserializerTest extends GeneDiseaseCollectionJsonSerializationTest {
    Gson gson = generateGsonBuilder().registerTypeAdapter(
            GeneDiseaseCollection.class, new GeneDiseaseCollectionJsonMemoryDeserializer()).create();

    @Test
    void testSerializer() {
        GeneDiseaseCollection actualGeneDiseaseCollection = gson.fromJson(expectedJson, GeneDiseaseCollection.class);
        Assertions.assertTrue(actualGeneDiseaseCollection.allFieldsEquals(expectedGeneDiseaseCollection));
    }

    @Test
    void testMissingGene() {
        Assertions.assertThrows(JsonParseException.class, () -> gson.fromJson(jsonWithMissingGene, GeneDiseaseCollection.class));
    }

    @Test
    void testEmptyGenes() {
        Assertions.assertThrows(JsonParseException.class, () -> gson.fromJson(jsonWithEmptyGenes, GeneDiseaseCollection.class));
    }

    @Test
    void testNoDiseases() {
        Assertions.assertThrows(JsonParseException.class, () -> gson.fromJson(jsonWithoutDiseases, GeneDiseaseCollection.class));
    }

    @Test
    void testNoCombinations() {
        Assertions.assertThrows(JsonParseException.class, () -> gson.fromJson(jsonWithhoutCombinations, GeneDiseaseCollection.class));
    }

    @Test
    void testNoCombinationEvidence() {
        GeneDiseaseCollection actualGeneDiseaseCollection = gson.fromJson(jSonWithoutCombinationEvidence, GeneDiseaseCollection.class);
        Assertions.assertTrue(actualGeneDiseaseCollection.allFieldsEquals(gdcWithoutCombinationEvidence));
    }

    @Test
    void testNoCombinationScore() {
        GeneDiseaseCollection actualGeneDiseaseCollection = gson.fromJson(jSonWithoutScore, GeneDiseaseCollection.class);
        Assertions.assertTrue(actualGeneDiseaseCollection.allFieldsEquals(gdcWithoutScore));
    }
}
