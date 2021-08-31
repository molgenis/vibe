package org.molgenis.vibe.core.formats.serialization.json.gene_disease_collection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.molgenis.vibe.core.formats.GeneDiseaseCollection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class GeneDiseaseCollectionJsonConverter {
    private static final Gson gson;

    static {
        GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting();
        // TypeAdapters for ElementSerializers.
        gsonBuilder.registerTypeAdapter(
                GeneDiseaseCollection.class, new GeneDiseaseCollectionJsonMemorySerializer());
        gsonBuilder.registerTypeAdapter(
                GeneDiseaseCollection.class, new GeneDiseaseCollectionJsonMemoryDeserializer());
        gson = gsonBuilder.create();
    }

    public static String serialize(GeneDiseaseCollection geneDiseaseCollection) {
        return gson.toJson(geneDiseaseCollection);
    }

    public static void serialize(GeneDiseaseCollection geneDiseaseCollection, JsonWriter writer) {
        gson.toJson(geneDiseaseCollection, GeneDiseaseCollection.class, writer);
    }

    public static GeneDiseaseCollection deserialize(String geneDiseaseCollectionJson) {
        return gson.fromJson(geneDiseaseCollectionJson, GeneDiseaseCollection.class);
    }

    public static GeneDiseaseCollection deserialize(JsonReader geneDiseaseCollectionJson) {
        return gson.fromJson(geneDiseaseCollectionJson, GeneDiseaseCollection.class);
    }

    public static void writeJsonStream(OutputStream outputStream, GeneDiseaseCollection collection) throws IOException {
        try(GeneDiseaseCollectionJsonStreamSerializer serializer =
                    new GeneDiseaseCollectionJsonStreamSerializer(gson, outputStream)) {
            serializer.writeJsonStream(collection);
        }
    }

    public static GeneDiseaseCollection readJsonStream(InputStream inputStream) throws IOException {
        try(GeneDiseaseCollectionJsonStreamDeserializer deserializer =
                    new GeneDiseaseCollectionJsonStreamDeserializer(inputStream)) {
            return deserializer.readJsonStream();
        }
    }
}
