package org.molgenis.vibe.core.formats.serialization.json.gene_disease_collection;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import org.molgenis.vibe.core.formats.Disease;
import org.molgenis.vibe.core.formats.Gene;
import org.molgenis.vibe.core.formats.GeneDiseaseCollection;
import org.molgenis.vibe.core.formats.GeneDiseaseCombination;
import org.molgenis.vibe.core.formats.Source;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class GeneDiseaseCollectionJsonStreamSerializer extends GeneDiseaseCollectionJsonSerializer implements Closeable {
    private Gson gson;
    private JsonWriter writer;

    public GeneDiseaseCollectionJsonStreamSerializer(Gson gson, OutputStream outputStream) throws IOException {
        this.gson = requireNonNull(gson);
        this.writer = this.gson.newJsonWriter(new OutputStreamWriter(requireNonNull(outputStream), StandardCharsets.UTF_8));
    }


    public void writeJsonStream(GeneDiseaseCollection collection) throws IOException {
        writer.beginObject();
        writeGenesStream(collection);
        writeDiseasesStream(collection);
        writeSourcesStream(collection);
        writeCombinationsStream(collection);
        writer.endObject();
    }

    private void writeGenesStream(GeneDiseaseCollection collection) throws IOException {
        writer.name(GENES_KEY);
        writer.beginObject();
        for(Gene gene: collection.getGenes()) {
            writer.name(gene.getId());
            gson.toJson(serializeGene(gene), writer);
        }
        writer.endObject();
    }

    private void writeDiseasesStream(GeneDiseaseCollection collection) throws IOException {
        writer.name(DISEASES_KEY);
        writer.beginObject();
        for(Disease disease: collection.getDiseases()) {
            writer.name(disease.getId());
            gson.toJson(serializeDisease(disease), writer);
        }
        writer.endObject();
    }

    private void writeSourcesStream(GeneDiseaseCollection collection) throws IOException {
        // Stores unique sources.
        Set<Source> foundSources = new HashSet<>();

        // Writes sources.
        writer.name(SOURCES_KEY);
        writer.beginObject();
        for(GeneDiseaseCombination gdc : collection.getGeneDiseaseCombinationsOrdered()) {
            for(Source source : gdc.getSourcesCount().keySet()) {
                if(!foundSources.contains(source)) {
                    foundSources.add(source);
                    writer.name(source.getName());
                    gson.toJson(serializeSource(source), writer);
                }
            }
        }
        writer.endObject();
    }

    private void writeCombinationsStream(GeneDiseaseCollection collection) throws IOException {
        writer.name(COMBINATIONS_KEY);
        writer.beginArray();
        for(GeneDiseaseCombination gdc : collection.getGeneDiseaseCombinationsOrdered()) {
            // Adds a single gene-disease combination to the array.
            gson.toJson(serializeGeneDiseaseCombination(gdc), writer);
        }
        writer.endArray();
    }

    @Override
    public void close() throws IOException {
        writer.flush();
        writer.close();
    }
}
