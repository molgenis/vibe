package org.molgenis.vibe.core.formats.serialization.json.gene_disease_collection;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import org.molgenis.vibe.core.exceptions.JsonIOParseException;
import org.molgenis.vibe.core.formats.Disease;
import org.molgenis.vibe.core.formats.Gene;
import org.molgenis.vibe.core.formats.GeneDiseaseCollection;
import org.molgenis.vibe.core.formats.GeneDiseaseCombination;
import org.molgenis.vibe.core.formats.Source;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class GeneDiseaseCollectionJsonStreamDeserializer extends GeneDiseaseCollectionJsonDeserializer
        implements Closeable {
    private JsonReader reader;

    /**
     * Retrieve next name and validates if it equal to the expected next name. If the next name does not adhere to the
     * {@code expectedName}, throws an {@link JsonParseException} instead.
     * @param reader the reader from which to retrieve the next name
     * @param expectedName the expected next name
     * @return the next name
     * @throws IOException
     * @throws JsonParseException if {@code !reader.nextName().equals(expectedName)}
     */
    private static String retrieveValidatedNextName(JsonReader reader, String expectedName) throws IOException {
        // .nextName() throws IllegalStateException if next JsonElement is not a name.
        String actualName = reader.nextName();
        if(!actualName.equals(expectedName)) {
            throw new JsonParseException("JsonToken does not match the expected name. Expected \"" + expectedName + "\" but was \"" + actualName + "\"");
        }
        return actualName;
    }

    public GeneDiseaseCollectionJsonStreamDeserializer(InputStream inputStream) {
        this.reader = new JsonReader(new InputStreamReader(requireNonNull(inputStream), StandardCharsets.UTF_8));
    }

    public GeneDiseaseCollection readJsonStream() throws IOException {
        try {
            return readCollectionStream();
        } catch (JsonParseException | IllegalStateException e) {
            throw new JsonIOParseException(e);
        }
    }

    private GeneDiseaseCollection readCollectionStream() throws IOException {
        GeneDiseaseCollection gdc = new GeneDiseaseCollection();

        // Digests JSON.
        reader.beginObject();
        retrieveValidatedNextName(reader, GENES_KEY);
        Map<String,Gene> genesMap = readGenesStream();
        retrieveValidatedNextName(reader, DISEASES_KEY);
        Map<String,Disease> diseasesMap = readDiseasesStream();
        retrieveValidatedNextName(reader, SOURCES_KEY);
        Map<String,Source> sourcesMap = readSourcesStream();
        retrieveValidatedNextName(reader, COMBINATIONS_KEY);
        readCombinationsStream(gdc, genesMap, diseasesMap, sourcesMap);
        reader.endObject();

        return gdc;
    }

    /**
     * Generic code for retrieval of data that uses a similar format for storing information (except the fields storing
     * the actual data for the objects) and which needs to be turned into a {@link Map} for allowing the
     * {@link GeneDiseaseCombination}{@code s} to be deserialized.
     *
     * {
     *     "value1" : {
     *         "field2" : "value2",
     *         "field3" : "value3"
     *     }
     * }
     *
     * Note that "value1" here, which is the actual ID from the object. However, it is stored as key for the object to
     * allow for easier data retrieval in json format (and not included again as another field to reduce the file size).
     *
     * This code then passes the following part to {@link #digestObject(Map, JsonObject, String)} for data-specific
     * deserialization (in combination with "value1" which is given through a separate argument):
     * {
     *      "field2" : "value2",
     *      "field3" : "value3"
     * }
     *
     * @param <T>
     */
    private abstract class JsonMapCollector<T> {
        JsonReader reader;

        public JsonMapCollector(JsonReader reader) {
            this.reader = requireNonNull(reader);
        }

        protected Map<String, T> readStream() throws IOException {
            Map<String,T> entityMap = new HashMap<>();

            reader.beginObject();
            while(reader.hasNext()) {
                // .nextName() throws IllegalStateException if next JsonElement is not a name.
                String name = reader.nextName();
                // .getAsJsonObject() throws IllegalStateException if next JsonElement is not a JsonObject.
                JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
                // Digests object.
                digestObject(entityMap, jsonObject, name);
            }
            reader.endObject();

            return entityMap;
        }

        /**
         * An object specific implementation for deserialization.
         * @param entityMap a {@link Map} to store deserialized data in
         * @param jsonObject the current object being processed
         * @param objectId the id belonging to the object being processed
         * @see JsonMapCollector
         */
        protected abstract void digestObject(Map<String,T> entityMap, JsonObject jsonObject, String objectId);
    }

    private Map<String,Gene> readGenesStream() throws IOException {
        JsonMapCollector<Gene> digester = new JsonMapCollector<Gene>(reader) {
            @Override
            protected void digestObject(Map entityMap, JsonObject jsonObject, String objectId) {
                entityMap.put(objectId, deserializeGene(jsonObject, objectId));
            }
        };

        return digester.readStream();
    }

    private Map<String,Disease> readDiseasesStream() throws IOException {
        JsonMapCollector<Disease> digester = new JsonMapCollector<Disease>(reader) {
            @Override
            protected void digestObject(Map<String, Disease> entityMap, JsonObject jsonObject, String objectId) {
                entityMap.put(objectId, deserializeDisease(jsonObject, objectId));
            }
        };

        return digester.readStream();
    }

    private Map<String,Source> readSourcesStream() throws IOException {
        JsonMapCollector<Source> digester = new JsonMapCollector<Source>(reader) {
            @Override
            protected void digestObject(Map<String, Source> entityMap, JsonObject jsonObject, String objectId) {
                entityMap.put(objectId, deserializeSource(jsonObject, objectId));
            }
        };

        return digester.readStream();
    }

    private void readCombinationsStream(GeneDiseaseCollection gdc, Map<String,Gene> genesMap,
                                        Map<String,Disease> diseasesMap, Map<String,Source> sourcesMap)
            throws IOException {
        reader.beginArray();
        while(reader.hasNext()) {
            // .getAsJsonObject() throws IllegalStateException if next JsonElement is not a JsonObject.
            gdc.add(deserializeGeneDiseaseCombination(
                    JsonParser.parseReader(reader).getAsJsonObject(), genesMap, diseasesMap, sourcesMap)
            );
        }
        reader.endArray();
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}