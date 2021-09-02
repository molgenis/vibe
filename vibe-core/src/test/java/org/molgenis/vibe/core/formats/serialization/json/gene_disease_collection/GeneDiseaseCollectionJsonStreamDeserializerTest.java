package org.molgenis.vibe.core.formats.serialization.json.gene_disease_collection;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.molgenis.vibe.core.exceptions.JsonIOParseException;
import org.molgenis.vibe.core.formats.GeneDiseaseCollection;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

class GeneDiseaseCollectionJsonStreamDeserializerTest extends GeneDiseaseCollectionJsonSerializationTestsParent {
    @Test
    void testReadJsonStream() throws IOException {
        try(InputStream inputStream = new ByteArrayInputStream(expectedJson.getBytes())) {
            try(GeneDiseaseCollectionJsonStreamDeserializer deserializer =
                         new GeneDiseaseCollectionJsonStreamDeserializer(inputStream)) {
                GeneDiseaseCollection actualGeneDiseaseCollection = deserializer.readJsonStream();
                Assertions.assertTrue(actualGeneDiseaseCollection.allFieldsEquals(expectedGeneDiseaseCollection));
            }
        }
    }

    @Test
    void testMissingGene() throws IOException {
        try(InputStream inputStream = new ByteArrayInputStream(jsonWithMissingGene.getBytes())) {
            try(GeneDiseaseCollectionJsonStreamDeserializer deserializer =
                         new GeneDiseaseCollectionJsonStreamDeserializer(inputStream)) {
                Assertions.assertThrows(JsonIOParseException.class, () -> deserializer.readJsonStream());
            }
        }
    }

    @Test
    void testEmptyGenes() throws IOException {
        try(InputStream inputStream = new ByteArrayInputStream(jsonWithEmptyGenes.getBytes())) {
            try(GeneDiseaseCollectionJsonStreamDeserializer deserializer =
                         new GeneDiseaseCollectionJsonStreamDeserializer(inputStream)) {
                Assertions.assertThrows(JsonIOParseException.class, () -> deserializer.readJsonStream());
            }
        }
    }

    @Test
    void testNoDiseases() throws IOException {
        try(InputStream inputStream = new ByteArrayInputStream(jsonWithoutDiseases.getBytes())) {
            try(GeneDiseaseCollectionJsonStreamDeserializer deserializer =
                         new GeneDiseaseCollectionJsonStreamDeserializer(inputStream)) {
                Assertions.assertThrows(JsonIOParseException.class, () -> deserializer.readJsonStream());
            }
        }
    }

    @Test
    void testNoCombinations() throws IOException {
        try(InputStream inputStream = new ByteArrayInputStream(jsonWithhoutCombinations.getBytes())) {
            try (GeneDiseaseCollectionJsonStreamDeserializer deserializer =
                         new GeneDiseaseCollectionJsonStreamDeserializer(inputStream)) {
                Assertions.assertThrows(JsonIOParseException.class, () -> deserializer.readJsonStream());
            }
        }
    }

    @Test
    void testNoCombinationEvidence() throws IOException {
        try(InputStream inputStream = new ByteArrayInputStream(jSonWithoutCombinationEvidence.getBytes())) {
            try (GeneDiseaseCollectionJsonStreamDeserializer deserializer =
                         new GeneDiseaseCollectionJsonStreamDeserializer(inputStream)) {
                GeneDiseaseCollection actualGeneDiseaseCollection = deserializer.readJsonStream();
                Assertions.assertTrue(actualGeneDiseaseCollection.allFieldsEquals(gdcWithoutCombinationEvidence));
            }
        }
    }

    @Test
    void testNoCombinationScore() throws IOException {
        try(InputStream inputStream = new ByteArrayInputStream(jSonWithoutScore.getBytes())) {
            try (GeneDiseaseCollectionJsonStreamDeserializer deserializer =
                         new GeneDiseaseCollectionJsonStreamDeserializer(inputStream)) {
                GeneDiseaseCollection actualGeneDiseaseCollection = deserializer.readJsonStream();
                Assertions.assertTrue(actualGeneDiseaseCollection.allFieldsEquals(gdcWithoutScore));
            }
        }
    }
}
