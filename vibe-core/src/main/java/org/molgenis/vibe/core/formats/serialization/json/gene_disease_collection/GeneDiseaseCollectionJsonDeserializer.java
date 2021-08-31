package org.molgenis.vibe.core.formats.serialization.json.gene_disease_collection;

import com.google.gson.*;
import org.molgenis.vibe.core.formats.Disease;
import org.molgenis.vibe.core.formats.Gene;
import org.molgenis.vibe.core.formats.GeneDiseaseCollection;
import org.molgenis.vibe.core.formats.GeneDiseaseCombination;
import org.molgenis.vibe.core.formats.GeneSymbol;
import org.molgenis.vibe.core.formats.PubmedEvidence;
import org.molgenis.vibe.core.formats.Source;

import java.net.URI;
import java.util.*;

/**
 * Contains all code for deserializing individual elements within a {@link GeneDiseaseCollection}.
 */
abstract class GeneDiseaseCollectionJsonDeserializer extends GeneDiseaseCollectionJsonSerialization {
    /**
     * Deserializes a {@link GeneDiseaseCombination}. To ensure no duplicate objects are generated, requires
     * pre-generated {@link Map}{@code s} for the {@link Gene}{@code s}, {@link Disease}{@code s} and
     * {@link Source}{@code s}.
     * @param combinationObject the {@link GeneDiseaseCombination} that needs to be deserialized
     * @param genesMap all possible {@link Gene}{@code s} present in the {@link GeneDiseaseCollection}
     * @param diseasesMap all possible {@link Disease}{@code s} present in the {@link GeneDiseaseCollection}
     * @param sourcesMap all possible {@link Source}{@code s} present in the {@link GeneDiseaseCollection}
     * @return the {@link GeneDiseaseCollection} generated from the {@link JsonObject}
     */
    static GeneDiseaseCombination deserializeGeneDiseaseCombination(JsonObject combinationObject,
                                                                    Map<String, Gene> genesMap,
                                                                    Map<String, Disease> diseasesMap,
                                                                    Map<String, Source> sourcesMap) {
        // Try retrieving fields for new GeneDiseaseCombination.
        String geneString;
        String diseaseString;
        try {
            geneString = combinationObject.getAsJsonPrimitive(COMBINATION_GENE_KEY).getAsString();
            diseaseString = combinationObject.getAsJsonPrimitive(COMBINATION_DISEASE_KEY).getAsString();
        } catch (NullPointerException e) {
            throw new JsonParseException("Expected field is missing.");
        }
        // Try finding matching gene/disease.
        Gene gene = genesMap.get(geneString);
        Disease disease = diseasesMap.get(diseaseString);
        if(gene == null || disease == null) {
            throw new JsonParseException("Missing a gene/disease belonging to a GeneDiseaseCombination.");
        }
        // Retrieve score primitive.
        JsonPrimitive jsonDisgenetScore = combinationObject.getAsJsonPrimitive(COMBINATION_DISGENET_SCORE_KEY);

        // Generate GeneDiseaseCombination with or without score (depending if primitive == null).
        GeneDiseaseCombination geneDiseaseCombination;
        if(jsonDisgenetScore != null) {
            geneDiseaseCombination = new GeneDiseaseCombination(gene, disease, jsonDisgenetScore.getAsDouble());
        } else {
            geneDiseaseCombination = new GeneDiseaseCombination(gene, disease);
        }

        // Check if there are any sources.
        JsonArray combinationSourcesArray = combinationObject.getAsJsonArray(COMBINATION_SOURCES_KEY);
        if(combinationSourcesArray != null) {
            // Process all sources.
            Iterator<JsonElement> combinationSourcesIterator = combinationSourcesArray.iterator();
            while (combinationSourcesIterator.hasNext()) {
                // Define variables.
                String sourceString;
                int sourceCount;
                Iterator<JsonElement> pubmedsIterator;
                // Retrieve fields.
                try {
                    // The source.
                    JsonObject sourceObject = combinationSourcesIterator.next().getAsJsonObject();
                    sourceString = sourceObject.getAsJsonPrimitive(COMBINATION_SOURCE_NAME_KEY).getAsString();

                    // Retrieves source counts.
                    sourceCount = sourceObject.getAsJsonPrimitive(COMBINATION_SOURCE_COUNT_KEY).getAsInt();

                    // Prepares for iterating through pubmed evidence.
                    pubmedsIterator = sourceObject.getAsJsonArray(COMBINATION_SOURCE_PUBMEDS_KEY).iterator();
                } catch (NullPointerException e) {
                    throw new JsonParseException("Expected field is missing.");
                }

                // Prepare for iterating through pubmeds.
                Set<PubmedEvidence> pubmedEvidenceForSource = new HashSet<>();

                // Retrieves each individual pubmed evidence item.
                while (pubmedsIterator.hasNext()) {
                    URI pubmedUri;
                    int pubmedYear;
                    try {
                        JsonObject pubmedObject = pubmedsIterator.next().getAsJsonObject();
                        pubmedUri = URI.create(pubmedObject.getAsJsonPrimitive(COMBINATION_SOURCE_PUBMED_URI_KEY).getAsString());
                        pubmedYear = pubmedObject.getAsJsonPrimitive(COMBINATION_SOURCE_PUBMED_YEAR_KEY).getAsInt();
                    } catch (NullPointerException e) {
                        throw new JsonParseException("Expected field is missing.");
                    }
                    pubmedEvidenceForSource.add(new PubmedEvidence(pubmedUri, pubmedYear));
                }

                // Sets data for source.
                Source source = sourcesMap.get(sourceString);
                if (pubmedEvidenceForSource.isEmpty()) {
                    geneDiseaseCombination.set(source, sourceCount);
                } else {
                    geneDiseaseCombination.set(source, sourceCount, pubmedEvidenceForSource);
                }
            }
        }

        return geneDiseaseCombination;
    }

    static Gene deserializeGene(JsonObject jsonObject, String geneId) {
        String fullGeneId = Gene.ID_PREFIX + ":" + geneId;
        String fullGeneSymbolId;
        try {
            fullGeneSymbolId = GeneSymbol.ID_PREFIX + ":" + jsonObject.getAsJsonPrimitive(GENE_SYMBOL_KEY).getAsString();
        } catch (NullPointerException e) {
            throw new JsonParseException("Expected field is missing.");
        }
        return new Gene(fullGeneId, new GeneSymbol(fullGeneSymbolId));
    }

    static Disease deserializeDisease(JsonObject jsonObject, String diseaseId) {
        String fullDiseaseId = Disease.ID_PREFIX + ":" + diseaseId;
        String diseaseName;
        try {
            diseaseName = jsonObject.getAsJsonPrimitive(DISEASE_NAME_KEY).getAsString();
        } catch (NullPointerException e) {
            throw new JsonParseException("Expected field is missing.");
        }

        return new Disease(fullDiseaseId, diseaseName);

    }

    static Source deserializeSource(JsonObject jsonObject, String sourceName) {
        URI sourceUri;
        String sourceFullName;
        String levelString;
        // Tries to retrieve all fields.
        try {
            sourceUri = URI.create(jsonObject.getAsJsonPrimitive(SOURCE_URI_KEY).getAsString());
            sourceFullName = jsonObject.getAsJsonPrimitive(SOURCE_FULL_NAME_KEY).getAsString();
            levelString = jsonObject.getAsJsonPrimitive(SOURCE_LEVEL_KEY).getAsString();
        } catch (NullPointerException e) {
            throw new JsonParseException("Expected field is missing.");
        }
        Source source = new Source(sourceUri, sourceFullName, levelString);

        // Validates if sourceName is equal to the name from the fullName.
        if(!source.getName().equals(sourceName)) {
            throw new JsonParseException("The source name is not equal to the one that is derived from the fullName.");
        }

        return source;
    }
}
