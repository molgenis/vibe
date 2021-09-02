package org.molgenis.vibe.core.formats.serialization.json.gene_disease_collection;

import com.google.gson.Gson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.molgenis.vibe.core.formats.GeneDiseaseCollection;

class GeneDiseaseCollectionJsonMemorySerializerTest extends GeneDiseaseCollectionJsonSerializationTestsParent {
    Gson gson = generateGsonBuilder().registerTypeAdapter(
            GeneDiseaseCollection.class, new GeneDiseaseCollectionJsonMemorySerializer()).create();

    @Test
    void testSerializer() {
        String actualOutput = gson.toJson(expectedGeneDiseaseCollection);
        Assertions.assertEquals(expectedJson, actualOutput);
    }

    @Test
    void testSerializerWithoutEvidence() {
        String actualOutput = gson.toJson(gdcWithoutCombinationEvidence);
        Assertions.assertEquals(jSonWithoutCombinationEvidence, actualOutput);
    }

    @Test
    void testSerializerWithoutScore() {
        String actualOutput = gson.toJson(gdcWithoutScore);
        Assertions.assertEquals(jSonWithoutScore, actualOutput);
    }
}
