package org.molgenis.vibe.core.formats.serialization.json.gene_disease_collection;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.molgenis.vibe.core.formats.Disease;
import org.molgenis.vibe.core.formats.Gene;
import org.molgenis.vibe.core.formats.GeneDiseaseCollection;
import org.molgenis.vibe.core.formats.Source;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class GeneDiseaseCollectionJsonMemoryDeserializer extends GeneDiseaseCollectionJsonDeserializer
        implements JsonDeserializer<GeneDiseaseCollection> {
    @Override
    public GeneDiseaseCollection deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject collectionObject = json.getAsJsonObject();
        return deserializeGeneDiseaseCollection(collectionObject.getAsJsonArray(COMBINATIONS_KEY),
                deserializeGenes(collectionObject.getAsJsonObject(GENES_KEY)),
                deserializeDiseases(collectionObject.getAsJsonObject(DISEASES_KEY)),
                deserializeSources(collectionObject.getAsJsonObject(SOURCES_KEY))
        );
    }

    private static GeneDiseaseCollection deserializeGeneDiseaseCollection(JsonArray combinationsArray,
                                                                          Map<String, Gene> genesMap,
                                                                          Map<String, Disease> diseasesMap,
                                                                          Map<String, Source> sourcesMap) {
        GeneDiseaseCollection collection = new GeneDiseaseCollection();

        // Tries to retrieve iterator for GeneDiseaseCombinations.
        Iterator<JsonElement> combinationsIterator;
        try {
            combinationsIterator = combinationsArray.iterator();
        } catch (NullPointerException e) {
            throw new JsonParseException("Missing GeneDiseaseCombinations.");
        }

        // Iterates through all combinations.
        while(combinationsIterator.hasNext()) {
            // Create basic GeneDiseaseCombination.
            collection.add(deserializeGeneDiseaseCombination(combinationsIterator.next().getAsJsonObject(),
                    genesMap, diseasesMap, sourcesMap));
        }

        return collection;
    }

    private static Map<String,Gene> deserializeGenes(JsonObject jsonObject) {
        Map<String,Gene> genesMap = new HashMap<>();

        if(jsonObject != null) {
            for (String geneId : jsonObject.keySet()) {
                genesMap.put(geneId, deserializeGene(jsonObject.getAsJsonObject(geneId), geneId));
            }
        }

        return genesMap;
    }

    private static Map<String,Disease> deserializeDiseases(JsonObject jsonObject)  {
        Map<String,Disease> diseasesMap = new HashMap<>();
        if(jsonObject != null) {
            for (String diseaseId : jsonObject.keySet()) {
                diseasesMap.put(diseaseId, deserializeDisease(jsonObject.getAsJsonObject(diseaseId), diseaseId));
            }
        }

        return diseasesMap;
    }

    private static Map<String,Source> deserializeSources(JsonObject jsonObject) {
        Map<String,Source> sourcesMap = new HashMap<>();

        if(jsonObject != null) {
            for (String sourceName : jsonObject.keySet()) {
                sourcesMap.put(sourceName, deserializeSource(jsonObject.getAsJsonObject(sourceName), sourceName));
            }
        }

        return sourcesMap;
    }
}
