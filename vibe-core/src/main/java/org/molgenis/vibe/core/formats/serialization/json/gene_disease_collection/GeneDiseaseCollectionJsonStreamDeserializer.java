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

    public GeneDiseaseCollectionJsonStreamDeserializer(InputStream inputStream) {
        this.reader = new JsonReader(new InputStreamReader(requireNonNull(inputStream), StandardCharsets.UTF_8));
    }

    public GeneDiseaseCollection readJsonStream() throws IOException {
        try {
            return readCollectionStream();
        } catch (JsonParseException e) {
            throw new JsonIOParseException(e);
        }
    }

    private GeneDiseaseCollection readCollectionStream() throws IOException {
        GeneDiseaseCollection gdc = new GeneDiseaseCollection();
        boolean combinationDigested = false;

        // Stores maps with all genes/diseases/sources.
        Map<String,Gene> genesMap = null;
        Map<String,Disease> diseasesMap = null;
        Map<String,Source> sourcesMap = null;

        // Digests JSON.
        reader.beginObject();
        while (reader.hasNext()) {
            switch(reader.peek()) {
                case NAME:
                    String name = reader.nextName();
                    if(name.equals(GENES_KEY)) {
                        genesMap = readGenesStream();
                    } else if(name.equals(DISEASES_KEY)) {
                        diseasesMap = readDiseasesStream();
                    } else if(name.equals(SOURCES_KEY)) {
                        sourcesMap = readSourcesStream();
                    } else if(name.equals(COMBINATIONS_KEY)) {
                        if(genesMap == null || diseasesMap == null || sourcesMap == null) {
                            throw new JsonParseException("Invalid inputStream: combinations were found before all genes/diseases/sources could be collected.");
                        } else {
                            readCombinationsStream(gdc, genesMap, diseasesMap, sourcesMap);
                            combinationDigested = true;
                        }
                    }
                    break;
                default:
                    throw new JsonParseException("an unexpected JsonToken was found:" + reader.peek().toString());
            }
        }
        reader.endObject();

        // Checks whether readCombinationsStream() was triggered.
        if(!combinationDigested) {
            throw new JsonParseException("No combinations were digested.");
        }
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
                switch(reader.peek()) {
                    case NAME:
                        String objectId = reader.nextName();
                        digestObject(entityMap, JsonParser.parseReader(reader).getAsJsonObject(), objectId);
                        break;
                    default:
                        throw new JsonParseException("an unexpected JsonToken was found:" + reader.peek().toString());
                }
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
        JsonMapCollector digester = new JsonMapCollector<Gene>(reader) {
            @Override
            protected void digestObject(Map entityMap, JsonObject jsonObject, String objectId) {
                entityMap.put(objectId, deserializeGene(jsonObject, objectId));
            }
        };

        return digester.readStream();
    }

    private Map<String,Disease> readDiseasesStream() throws IOException {
        JsonMapCollector digester = new JsonMapCollector<Disease>(reader) {
            @Override
            protected void digestObject(Map<String, Disease> entityMap, JsonObject jsonObject, String objectId) {
                entityMap.put(objectId, deserializeDisease(jsonObject, objectId));
            }
        };

        return digester.readStream();
    }

    private Map<String,Source> readSourcesStream() throws IOException {
        JsonMapCollector digester = new JsonMapCollector<Source>(reader) {
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
            switch(reader.peek()) {
                case BEGIN_OBJECT:
                    gdc.add(deserializeGeneDiseaseCombination(
                            JsonParser.parseReader(reader).getAsJsonObject(), genesMap, diseasesMap, sourcesMap)
                    );
                    break;
                default:
                    throw new JsonParseException("an unexpected JsonToken was found:" + reader.peek().toString());
            }
        }
        reader.endArray();
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}