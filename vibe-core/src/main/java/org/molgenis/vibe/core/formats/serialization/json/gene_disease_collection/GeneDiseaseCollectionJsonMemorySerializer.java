package org.molgenis.vibe.core.formats.serialization.json.gene_disease_collection;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.molgenis.vibe.core.formats.Disease;
import org.molgenis.vibe.core.formats.Gene;
import org.molgenis.vibe.core.formats.GeneDiseaseCollection;
import org.molgenis.vibe.core.formats.GeneDiseaseCombination;
import org.molgenis.vibe.core.formats.Source;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

/**
 * Serializers a {@link GeneDiseaseCollection} to {@code json}.
 */
public class GeneDiseaseCollectionJsonMemorySerializer extends GeneDiseaseCollectionJsonSerializer
        implements JsonSerializer<GeneDiseaseCollection> {
    @Override
    public JsonElement serialize(GeneDiseaseCollection src, Type typeOfSrc, JsonSerializationContext context) {
        Set<Source> sources = new HashSet<>();

        // Object to store all data into.
        JsonObject collectionObject = new JsonObject();
        collectionObject.add(GENES_KEY, serializeGenes(src));
        collectionObject.add(DISEASES_KEY, serializeDiseases(src));
        JsonArray geneDiseaseCombinations = serializeGeneDiseaseCombinations(src, sources);
        collectionObject.add(SOURCES_KEY, serializeSources(sources));
        collectionObject.add(COMBINATIONS_KEY, geneDiseaseCombinations); // enforces identical format to streaming
        return collectionObject;
    }

    /**
     * Generates the {@link JsonArray} for the gene-disease combinations.
     * @param src the {@link GeneDiseaseCollection} that should be processed
     * @param foundSources a {@link Set} to store all unique {@link Source} instances in found in the gene-disease
     *                   combinations
     * @return the {@link GeneDiseaseCombination}{@code s} present in the {@link GeneDiseaseCollection} as {@link JsonArray}
     */
    private static JsonArray serializeGeneDiseaseCombinations(GeneDiseaseCollection src, Set<Source> foundSources) {
        // Array to store all gene-disease combinations in.
        JsonArray combinationsArray = new JsonArray();

        // Goes through all gene-disease combinations.
        for(GeneDiseaseCombination gdc : src.getGeneDiseaseCombinationsOrdered()) {
            // Adds a single gene-disease combination to the array.
            combinationsArray.add(serializeGeneDiseaseCombination(gdc, foundSources));
        }
        return combinationsArray;
    }

    /**
     * Generates the {@link JsonObject} for the {@link Gene}{@code s} present in the {@link GeneDiseaseCollection}.
     * @param src the {@link GeneDiseaseCollection} that should be processed
     * @return the {@link Gene}{@code s} present in the {@link GeneDiseaseCollection} as {@link JsonObject}
     */
    private static JsonObject serializeGenes(GeneDiseaseCollection src) {
        JsonObject genesObject = new JsonObject();

        // Processes items into JsonObject.
        for(Gene gene : src.getGenes()) {
            genesObject.add(gene.getId(), serializeGene(gene));
        }

        return genesObject;
    }

    /**
     * Generates the {@link JsonObject} for the {@link Disease}{@code s} present in the {@link GeneDiseaseCollection}.
     * @param src the {@link GeneDiseaseCollection} that should be processed
     * @return the {@link Disease}{@code s} present in the {@link GeneDiseaseCollection} as {@link JsonObject}
     */
    private static JsonObject serializeDiseases(GeneDiseaseCollection src) {
        JsonObject diseasesObject = new JsonObject();

        // Processes items into JsonObject.
        for(Disease disease : src.getDiseases()) {
            diseasesObject.add(disease.getId(), serializeDisease(disease));
        }

        return diseasesObject;
    }

    /**
     * Generates the {@link JsonObject} for the {@link Source}{@code s} present in the {@link GeneDiseaseCollection}.
     * @param sourcesSet the {@link Set} of all unique {@link Source}{@code s} (after it has been filled using
     * {@link #serializeGeneDiseaseCombinations(GeneDiseaseCollection, Set)})
     * @return the {@link Source}{@code s} present in the {@link GeneDiseaseCollection} as {@link JsonObject}
     */
    private static JsonObject serializeSources(Set<Source> sourcesSet) {
        JsonObject sourcesObject = new JsonObject();

        // Processes items into JsonObject.
        for(Source source : sourcesSet) {
            sourcesObject.add(source.getName(), serializeSource(source));
        }

        return sourcesObject;
    }
}
