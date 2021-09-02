package org.molgenis.vibe.core.formats.serialization.json.gene_disease_collection;

import com.google.gson.*;
import org.molgenis.vibe.core.formats.Disease;
import org.molgenis.vibe.core.formats.Gene;
import org.molgenis.vibe.core.formats.GeneDiseaseCollection;
import org.molgenis.vibe.core.formats.GeneDiseaseCombination;
import org.molgenis.vibe.core.formats.PubmedEvidence;
import org.molgenis.vibe.core.formats.Source;

import java.util.*;

/**
 * Contains all code for serializing individual elements within a {@link GeneDiseaseCollection}.
 * <br /><br />
 * <b>IMPORTANT:</b> Currently the {@link Source#getUri()} is not used as key. This increases the readability of the
 * json file and reduces file size. In practice this should not cause any issues within a single
 * {@link GeneDiseaseCollection}. However, if multiple {@link Source}{@code s} have the same {@link Source#getName()},
 * this would lead to a corrupt {@code json} output. This could for example be caused when a single
 * {@link GeneDiseaseCollection} contains data from multiple {@link Source} objects that indicate different versions of
 * the same {@link Source#getFullName()} (which under normal circumstances should not happen).
 */
abstract class GeneDiseaseCollectionJsonSerializer extends GeneDiseaseCollectionJsonSerialization {
    /**
     * Wrapper for {@link #serializeGeneDiseaseCombination(GeneDiseaseCombination, Set)} where the {@code foundSources}
     * are written to a {@link HashSet} that is thrown away.
     * @param gdc
     * @return
     */
    static JsonObject serializeGeneDiseaseCombination(GeneDiseaseCombination gdc) {
        return serializeGeneDiseaseCombination(gdc, new HashSet<>());
    }

    /**
     * Converts a {@link GeneDiseaseCombination} to a {@link JsonObject}.
     *
     * @param gdc the {@link GeneDiseaseCombination} to be converted to a {@link JsonObject}
     * @param foundSources a {@link Set} to store all unique found {@link Source}{@code s} in
     * @return the {@code gdc} as {@link JsonObject}
     */
    static JsonObject serializeGeneDiseaseCombination(GeneDiseaseCombination gdc, Set<Source> foundSources) {
        // Creates a new gene-disease combinations.
        JsonObject combinationObject = new JsonObject();

        // Adds gene.
        combinationObject.addProperty(COMBINATION_GENE_KEY, gdc.getGene().getId());

        // Adds disease.
        combinationObject.addProperty(COMBINATION_DISEASE_KEY, gdc.getDisease().getId());

        // Adds score.
        if(gdc.getDisgenetScore() != null) {
            combinationObject.addProperty(COMBINATION_DISGENET_SCORE_KEY, gdc.getDisgenetScore());
        }

        // Adds sources.
        JsonArray evidenceArray = new JsonArray();

        // sources: generate sorted list of sources.
        List<Source> orderedSources = new ArrayList<>();
        orderedSources.addAll(gdc.getSourcesCount().keySet());
        Collections.sort(orderedSources);

        // sources: Goes through all ordered sources.
        for(Source source : orderedSources) {
            // sources: Add source Object itself to set containing all used sources.
            foundSources.add(source);

            // sources: Digest source basic data.
            JsonObject evidenceItemObject = new JsonObject();
            evidenceItemObject.addProperty(COMBINATION_SOURCE_NAME_KEY, source.getName());
            evidenceItemObject.addProperty(COMBINATION_SOURCE_COUNT_KEY, gdc.getCountForSource(source));

            // sources: Adds pubmed evidence (empty array if no pubmed evidence).
            JsonArray pubmedEvidenceArray = new JsonArray();

            List<PubmedEvidence> evidenceList = gdc.getPubmedEvidenceForSourceSortedByReleaseDate(source);
            if(evidenceList != null) {
                for (PubmedEvidence pubmedEvidence : evidenceList) {
                    JsonObject pubmedEvidenceObject = new JsonObject();
                    pubmedEvidenceObject.addProperty(COMBINATION_SOURCE_PUBMED_URI_KEY, pubmedEvidence.getUri().toString());
                    pubmedEvidenceObject.addProperty(COMBINATION_SOURCE_PUBMED_YEAR_KEY, pubmedEvidence.getReleaseYear());
                    pubmedEvidenceArray.add(pubmedEvidenceObject);
                }
            }
            evidenceItemObject.add(COMBINATION_SOURCE_PUBMEDS_KEY, pubmedEvidenceArray);

            // sources: Adds evidence from single source to array.
            evidenceArray.add(evidenceItemObject);
        }

        // Evidence: Adds array of evidence to combination.
        combinationObject.add(COMBINATION_SOURCES_KEY, evidenceArray);

        return combinationObject;
    }

    /**
     * Converts a {@link Gene} to a {@link JsonObject}.
     * <br /><b>IMPORTANT:</b> It is assumed this data is stored using a {@link JsonObject} using the ID as key, not as
     * a {@link JsonArray}. Therefore, the ID is left out to reduce duplicate information!
     *
     * @param gene the {@link Gene} to be converted to a {@link JsonObject}
     * @return the {@code gene} as {@link JsonObject}
     */
    static JsonObject serializeGene(Gene gene) {
        JsonObject singleGeneObject = new JsonObject();
        singleGeneObject.addProperty(GENE_SYMBOL_KEY, gene.getSymbol().getId());
        return singleGeneObject;
    }

    /**
     * Converts a {@link Disease} to a {@link JsonObject}.
     * <br /><b>IMPORTANT:</b> It is assumed this data is stored using a {@link JsonObject} using the ID as key, not as
     * a {@link JsonArray}. Therefore, the ID is left out to reduce duplicate information!
     *
     * @param disease the {@link Disease} to be converted to a {@link JsonObject}
     * @return the {@code disease} as {@link JsonObject}
     */
    static JsonObject serializeDisease(Disease disease) {
        JsonObject singleDiseaseObject = new JsonObject();
        singleDiseaseObject.addProperty(DISEASE_NAME_KEY, disease.getName());
        return singleDiseaseObject;
    }

    /**
     * Converts a {@link Source} to a {@link JsonObject}.
     * <br /><b>IMPORTANT:</b> It is assumed this data is stored using a {@link JsonObject} using the name as key, not
     * as a {@link JsonArray}. Therefore, the name is left out to reduce duplicate information!
     *
     * @param source the {@link Source} to be converted to a {@link JsonObject}
     * @return the {@code source} as {@link JsonObject}
     */
    static JsonObject serializeSource(Source source) {
        JsonObject singleSourceObject = new JsonObject();
        singleSourceObject.addProperty(SOURCE_FULL_NAME_KEY, source.getFullName());
        singleSourceObject.addProperty(SOURCE_URI_KEY, source.getUri().toString());
        singleSourceObject.addProperty(SOURCE_LEVEL_KEY, source.getLevel().getReadableString());
        return singleSourceObject;
    }
}
