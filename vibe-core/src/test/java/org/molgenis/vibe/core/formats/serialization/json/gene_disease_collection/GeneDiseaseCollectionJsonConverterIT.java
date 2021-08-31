package org.molgenis.vibe.core.formats.serialization.json.gene_disease_collection;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.molgenis.vibe.core.formats.GeneDiseaseCollection;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.StringReader;
import java.io.StringWriter;

public class GeneDiseaseCollectionJsonConverterIT extends GeneDiseaseCollectionJsonSerializationTest {
    @Test
    void testMemorySerializationWithMemoryDeserialization() {
        GeneDiseaseCollection returnedCollection = GeneDiseaseCollectionJsonConverter.deserialize(
                GeneDiseaseCollectionJsonConverter.serialize(expectedGeneDiseaseCollection)
        );
        Assertions.assertTrue(returnedCollection.allFieldsEquals(expectedGeneDiseaseCollection));
    }

    /**
     * Validates that when serializing and deserializing a {@link GeneDiseaseCollection} through streaming,
     * an identical {@link GeneDiseaseCollection} is generated.
     * @throws IOException
     */
    @Test
    void testStreamSerializationWithStreamDeserialization() throws IOException {
        try(PipedInputStream in = new PipedInputStream()) {
            try(PipedOutputStream out = new PipedOutputStream(in)) {
                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    GeneDiseaseCollectionJsonConverter.writeJsonStream(out, expectedGeneDiseaseCollection);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                ).start();

                GeneDiseaseCollection returnedCollection = GeneDiseaseCollectionJsonConverter.readJsonStream(in);
                Assertions.assertTrue(returnedCollection.allFieldsEquals(expectedGeneDiseaseCollection));
            }
        }
    }

    @Test
    void testStreamSerializationWithMemoryDeserialization() throws IOException {
        try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            GeneDiseaseCollectionJsonConverter.writeJsonStream(outputStream, expectedGeneDiseaseCollection);
            GeneDiseaseCollection returnedCollection = GeneDiseaseCollectionJsonConverter.deserialize(outputStream.toString());

            Assertions.assertTrue(returnedCollection.allFieldsEquals(expectedGeneDiseaseCollection));
        }
    }

    @Test
    void testMemorySerializationWithStreamDeserialization() throws IOException {
        String jsonString = GeneDiseaseCollectionJsonConverter.serialize(expectedGeneDiseaseCollection);
        try(ByteArrayInputStream inputStream = new ByteArrayInputStream(jsonString.getBytes())) {
            GeneDiseaseCollection returnedCollection = GeneDiseaseCollectionJsonConverter.readJsonStream(inputStream);

            Assertions.assertTrue(returnedCollection.allFieldsEquals(expectedGeneDiseaseCollection));
        }
    }

    @Test
    void testSerializeUsingJsonWriterAndReader() {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);

        GeneDiseaseCollectionJsonConverter.serialize(expectedGeneDiseaseCollection, writer);

        StringReader stringReader = new StringReader(stringWriter.toString());
        JsonReader reader = new JsonReader(stringReader);

        GeneDiseaseCollection returnedCollection = GeneDiseaseCollectionJsonConverter.deserialize(reader);

        Assertions.assertTrue(returnedCollection.allFieldsEquals(expectedGeneDiseaseCollection));
    }
}
