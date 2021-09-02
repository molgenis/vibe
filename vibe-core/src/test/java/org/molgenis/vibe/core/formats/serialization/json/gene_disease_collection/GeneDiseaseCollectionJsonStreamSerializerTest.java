package org.molgenis.vibe.core.formats.serialization.json.gene_disease_collection;

import com.google.gson.Gson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

class GeneDiseaseCollectionJsonStreamSerializerTest extends GeneDiseaseCollectionJsonSerializationTest {
    Gson gson = generateGsonBuilder().create();

    @Test
    void testWriteJsonStream() throws IOException {
        try(OutputStream outputStream = new ByteArrayOutputStream()) {
            try (GeneDiseaseCollectionJsonStreamSerializer serializer =
                         new GeneDiseaseCollectionJsonStreamSerializer(gson, outputStream)) {
                serializer.writeJsonStream(expectedGeneDiseaseCollection);
            }
            Assertions.assertEquals(expectedJson, outputStream.toString());
        }
    }

    @Test
    void testWriteJsonStreamWithoutEvidence() throws IOException {
        try(OutputStream outputStream = new ByteArrayOutputStream()) {
            try (GeneDiseaseCollectionJsonStreamSerializer serializer =
                         new GeneDiseaseCollectionJsonStreamSerializer(gson, outputStream)) {
                serializer.writeJsonStream(gdcWithoutCombinationEvidence);
            }
            Assertions.assertEquals(jSonWithoutCombinationEvidence, outputStream.toString());
        }
    }

    @Test
    void testWriteJsonStreamWithoutScore() throws IOException {
        try(OutputStream outputStream = new ByteArrayOutputStream()) {
            try (GeneDiseaseCollectionJsonStreamSerializer serializer =
                         new GeneDiseaseCollectionJsonStreamSerializer(gson, outputStream)) {
                serializer.writeJsonStream(gdcWithoutScore);
            }
            Assertions.assertEquals(jSonWithoutScore, outputStream.toString());
        }
    }
}
