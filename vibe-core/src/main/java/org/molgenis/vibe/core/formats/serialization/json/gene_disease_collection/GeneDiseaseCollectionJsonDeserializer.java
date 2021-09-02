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
     * Retrieve a {@link JsonPrimitive} and optionally force a {@link JsonParseException} if it is {@code null}
     * @param jsonObject from which to retrieve the {@link JsonPrimitive}
     * @param key the name of the {@link JsonPrimitive}
     * @param nullAllowed whether returned value is allowed to be {@code null}
     * @return the found {@link JsonPrimitive} within the given {@link JsonObject}
     */
    private static JsonPrimitive retrieveJsonPrimitive(JsonObject jsonObject, String key, boolean nullAllowed) {
        JsonPrimitive jsonPrimitive = jsonObject.getAsJsonPrimitive(key);
        return nullAllowed ? jsonPrimitive : (JsonPrimitive) ensureNonNull(jsonPrimitive, key);
    }

    /**
     * Retrieve a {@link JsonArray} and optionally force a {@link JsonParseException} if it is {@code null}
     * @param jsonObject from which to retrieve the {@link JsonArray}
     * @param key the name of the {@link JsonArray}
     * @param nullAllowed whether returned value is allowed to be {@code null}
     * @return the found {@link JsonArray} within the given {@link JsonObject}
     */
    private static JsonArray retrieveJsonArray(JsonObject jsonObject, String key, boolean nullAllowed) {
        JsonArray jsonArray = jsonObject.getAsJsonArray(key);
        return nullAllowed ? jsonArray : (JsonArray) ensureNonNull(jsonArray, key);
    }

    /**
     * Checks whether a {@link JsonElement} is null. If so, throws a new {@link JsonParseException}. If not, returns
     * the {@link JsonElement}.
     * @param element the {@link JsonElement} which needs to be checked whether it's {@code null}
     * @param key the key used to retrieve the {@link JsonElement} (used in the {@link JsonParseException} message
     * @return if {@code element != null}, returns the {@code element}
     */
    private static JsonElement ensureNonNull(JsonElement element, String key) {
        if(element == null) {
            throw new JsonParseException("Expected field is missing: " + key);
        } else {
            return element;
        }
    }

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
        GeneDiseaseCombination geneDiseaseCombination =
                generateGeneDiseaseCombination(combinationObject, genesMap, diseasesMap);
        addSourcesToGeneDiseaseCombination(combinationObject, geneDiseaseCombination, sourcesMap);
        return geneDiseaseCombination;
    }

    private static GeneDiseaseCombination generateGeneDiseaseCombination(JsonObject combinationObject,
                                                                         Map<String, Gene> genesMap,
                                                                         Map<String, Disease> diseasesMap) {
        // Try retrieving gene/disease:
        // retrieveJsonPrimitive with nullAllowed == false ensures the combinations JsonObject contains the field.
        // Validating if Gene/Disease == null ensures the genesMap/diseasesMap contained a key for that Gene/Disease.
        String geneString = retrieveJsonPrimitive(combinationObject, COMBINATION_GENE_KEY, false).getAsString();
        String diseaseString = retrieveJsonPrimitive(combinationObject, COMBINATION_DISEASE_KEY, false).getAsString();
        Gene gene = genesMap.get(geneString);
        Disease disease = diseasesMap.get(diseaseString);
        if(gene == null && disease == null) {
            throw new JsonParseException("Missing gene and disease data belonging to a GeneDiseaseCombination: " + geneString + " & " + diseaseString);
        } else if(gene == null) {
            throw new JsonParseException("Missing a gene data belonging to a GeneDiseaseCombination: " + geneString);
        } else if(disease == null) {
            throw new JsonParseException("Missing a disease data belonging to a GeneDiseaseCombination: " + diseaseString);
        }
        // Retrieve score primitive.
        JsonPrimitive jsonDisgenetScore = retrieveJsonPrimitive(combinationObject, COMBINATION_DISGENET_SCORE_KEY, true);

        // Generate GeneDiseaseCombination with or without score (depending if jsonDisgenetScore primitive == null).
        if(jsonDisgenetScore != null) {
            return new GeneDiseaseCombination(gene, disease, jsonDisgenetScore.getAsDouble());
        } else {
            return new GeneDiseaseCombination(gene, disease);
        }
    }

    private static void addSourcesToGeneDiseaseCombination(JsonObject combinationObject,
                                                      GeneDiseaseCombination geneDiseaseCombination,
                                                      Map<String, Source> sourcesMap) {
        // Check if there are any sources.
        JsonArray combinationSourcesArray = retrieveJsonArray(combinationObject, COMBINATION_SOURCES_KEY, true);
        if(combinationSourcesArray != null) {
            // Process all sources.
            Iterator<JsonElement> combinationSourcesIterator = combinationSourcesArray.iterator();
            while (combinationSourcesIterator.hasNext()) {
                JsonObject sourceObject = combinationSourcesIterator.next().getAsJsonObject();

                // Try retrieving source:
                // retrieveJsonPrimitive with nullAllowed == false ensures the combinations JsonObject contains the field.
                // Validating if Source == null ensures the sourcesMap contained a key for that Source.
                String sourceName = retrieveJsonPrimitive(sourceObject, COMBINATION_SOURCE_NAME_KEY, false).getAsString();
                Source source = sourcesMap.get(sourceName);
                if(source == null) {
                    throw new JsonParseException("Missing source data belonging to a GeneDiseaseCombination: " + sourceName);
                }

                // Retrieve count belonging to source.
                int sourceCount = retrieveJsonPrimitive(sourceObject, COMBINATION_SOURCE_COUNT_KEY, false).getAsInt();

                // Storage for pubmeds.
                Set<PubmedEvidence> pubmedEvidenceForSource = new HashSet<>();

                // Retrieves each individual pubmed evidence item. Requires array, but is allowed to be empty.
                Iterator<JsonElement> pubmedsIterator = retrieveJsonArray(sourceObject, COMBINATION_SOURCE_PUBMEDS_KEY, false).iterator();
                while (pubmedsIterator.hasNext()) {
                    JsonObject pubmedObject = pubmedsIterator.next().getAsJsonObject();
                    URI pubmedUri = URI.create(retrieveJsonPrimitive(pubmedObject, COMBINATION_SOURCE_PUBMED_URI_KEY, false).getAsString());
                    int pubmedYear = retrieveJsonPrimitive(pubmedObject, COMBINATION_SOURCE_PUBMED_YEAR_KEY, false).getAsInt();
                    pubmedEvidenceForSource.add(new PubmedEvidence(pubmedUri, pubmedYear));
                }

                if (pubmedEvidenceForSource.isEmpty()) {
                    geneDiseaseCombination.set(source, sourceCount);
                } else {
                    geneDiseaseCombination.set(source, sourceCount, pubmedEvidenceForSource);
                }
            }
        }
    }

    static Gene deserializeGene(JsonObject jsonObject, String geneId) {
        String fullGeneId = Gene.ID_PREFIX + ":" + geneId;
        String fullGeneSymbolId = GeneSymbol.ID_PREFIX + ":" + retrieveJsonPrimitive(jsonObject, GENE_SYMBOL_KEY, false).getAsString();
        return new Gene(fullGeneId, new GeneSymbol(fullGeneSymbolId));
    }

    static Disease deserializeDisease(JsonObject jsonObject, String diseaseId) {
        String fullDiseaseId = Disease.ID_PREFIX + ":" + diseaseId;
        String diseaseName = retrieveJsonPrimitive(jsonObject, DISEASE_NAME_KEY, false).getAsString();
        return new Disease(fullDiseaseId, diseaseName);

    }

    static Source deserializeSource(JsonObject jsonObject, String sourceName) {
        URI sourceUri = URI.create(retrieveJsonPrimitive(jsonObject, SOURCE_URI_KEY, false).getAsString());
        String sourceFullName = retrieveJsonPrimitive(jsonObject, SOURCE_FULL_NAME_KEY, false).getAsString();
        String levelString = retrieveJsonPrimitive(jsonObject, SOURCE_LEVEL_KEY, false).getAsString();
        Source source = new Source(sourceUri, sourceFullName, levelString);

        // Validates if sourceName is equal to the name from the fullName.
        if(!source.getName().equals(sourceName)) {
            throw new JsonParseException("The source name is not equal to the one that is derived from the fullName.");
        }

        return source;
    }
}
