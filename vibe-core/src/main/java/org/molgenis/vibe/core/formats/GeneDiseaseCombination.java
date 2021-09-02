package org.molgenis.vibe.core.formats;

import static java.util.Objects.requireNonNull;

import java.net.URI;
import java.util.*;

/**
 * A combination of a {@link Gene} and a {@link Disease}.
 */
public class GeneDiseaseCombination extends BiologicalEntityCombination<Gene, Disease> {
    /**
     * The score belonging to the gene-disease combination from the DisGeNET database.
     */
    private Double disgenetScore;

    /**
     * A {@link Map} storing which {@link Source}{@code s} contains this combination and how often.
     */
    private Map<Source, Integer> sourcesCount = new HashMap<>();

    /**
     * A {@link Map} storing per {@link Source} the {@link URI}{@code s} to the evidence (if available).
     */
    private Map<Source, Set<PubmedEvidence>> pubmedEvidence = new HashMap<>();

    /**
     * @return the {@link Gene}
     * @see #getT1()
     */
    public Gene getGene() {
        return getT1();
    }

    /**
     * @return the {@link Disease}
     * @see #getT2()
     */
    public Disease getDisease() {
        return getT2();
    }

    public Double getDisgenetScore() {
        return disgenetScore;
    }

    /**
     * The number of occurrences for this gene-disease combination per {@link Source}.
     * @return an unmodifiable {@link Map}
     */
    public Map<Source, Integer> getSourcesCount() {
        return Collections.unmodifiableMap(sourcesCount);
    }

    /**
     * The count for the defined {@link Source}
     * @param source the {@link Source} to retrieve the count for
     * @return an {@code int} containing the frequency of this source found (if {@link Source} is not present returns a 0)
     */
    public int getCountForSource(Source source) {
        Integer count = sourcesCount.get(source);
        if(count == null) {
            count = 0;
        }
        return count;
    }

    /**
     * All {@link Source}{@code s} for this gene-disease combination that have {@link PubmedEvidence}.
     * @return an unmodifiable {@link Set} containing {@link Source}{@code s}
     */
    public Set<Source> getSourcesWithPubmedEvidence() {
        return Collections.unmodifiableSet(pubmedEvidence.keySet());
    }

    /**
     * The {@link PubmedEvidence} for the defined {@link Source}.
     * @param source
     * @return an unmodifiable {@link Set} containing {@link PubmedEvidence},
     * or {@code null} if {@link Source} does not have any evidence
     */
    public Set<PubmedEvidence> getPubmedEvidenceForSource(Source source) {
        Set<PubmedEvidence> evidence = pubmedEvidence.get(source);
        if(evidence != null) {
            evidence = Collections.unmodifiableSet(evidence);
        }

        return evidence;
    }

    /**
     * The {@link PubmedEvidence} for the defined {@link Source}.
     * @param source
     * @return a {@link List} ordered through {@link PubmedEvidence#RELEASE_YEAR_COMPARATOR} containing
     * {@link PubmedEvidence}, or {@code null} if {@link Source} does not have any evidence
     */
    public List<PubmedEvidence> getPubmedEvidenceForSourceSortedByReleaseDate(Source source) {
        List<PubmedEvidence> evidenceList = null;
        Set<PubmedEvidence> evidence = pubmedEvidence.get(source);

        if (evidence != null) {
            evidenceList = new ArrayList<>();
            evidenceList.addAll(evidence);
            Collections.sort(evidenceList, PubmedEvidence.RELEASE_YEAR_COMPARATOR);
        }
        return evidenceList;
    }

    /**
     * The {@link PubmedEvidence} of all {@link Source}{@code s} combined.
     * @return a {@link Set} containing all the {@link PubmedEvidence}
     */
    public Set<PubmedEvidence> getAllPubmedEvidence() {
        Set<PubmedEvidence> evidence = new HashSet<>();
        pubmedEvidence.values().forEach(evidence::addAll);
        return evidence;
    }

    /**
     * The {@link PubmedEvidence} of all {@link Source}{@code s} combined.
     * @return a {@link List} containing all the {@link PubmedEvidence} ordered through
     * {@link PubmedEvidence#RELEASE_YEAR_COMPARATOR}
     */
    public List<PubmedEvidence> getAllPubMedEvidenceSortedByYear() {
        List<PubmedEvidence> evidence = new ArrayList<>();
        pubmedEvidence.values().forEach(evidence::addAll);
        Collections.sort(evidence, PubmedEvidence.RELEASE_YEAR_COMPARATOR);
        return evidence;
    }

    /**
     * Simple constructor allowing for easy comparison of collections.
     * @param gene
     * @param disease
     */
    public GeneDiseaseCombination(Gene gene, Disease disease) {
        super(gene, disease);
    }

    public GeneDiseaseCombination(Gene gene, Disease disease, double disgenetScore) {
        super(gene, disease);
        this.disgenetScore = requireNonNull(disgenetScore);
    }

    public void add(Source source, PubmedEvidence evidence) {
        // Increments counter for source.
        add(source);

        // Stores PubMed evidence.
        Set<PubmedEvidence> evidenceList = pubmedEvidence.computeIfAbsent(source, k -> new HashSet<>());
        evidenceList.add(evidence);
    }

    /**
     * Adds a {@link Source} to this gene-disease combination without an evidence {@link URI}.
     * @param source
     */
    public void add(Source source) {
        Integer count = sourcesCount.get(source);
        if(count == null) {
            sourcesCount.put(source, 1);
        } else {
            sourcesCount.put(source, count + 1);
        }
    }

    /**
     * Sets all values for a single {@link Source} that does have pubmed evidence.
     * @param source
     * @param count
     * @param pubmedEvidence
     * @throws IllegalArgumentException if {@code count < pubmedEvidence.size()}
     */
    public void set(Source source, int count, Set<PubmedEvidence> pubmedEvidence) {
        // Count should be equal or higher than number of pubmedEvidence present.
        if(count < pubmedEvidence.size()) {
            throw new IllegalArgumentException("count can not be lower than pubmedEvidence size");
        }
        // Sets the data for source.
        this.sourcesCount.put(source, count);
        this.pubmedEvidence.put(source, pubmedEvidence);
    }

    /**
     * Sets all values for a single {@link Source} that does not have a single pubmed evidence. Removes any evidence
     * for {@link Source} if present!
     * @param source
     * @param count
     */
    public void set(Source source, int count) {
        pubmedEvidence.remove(source);
        sourcesCount.put(source, count);
    }

    /**
     * Set {@code count} for a {@link Source}. To prevent wrongly setting information,
     * incrementing through {@link #add(Source)} is suggested instead.
     * @param source the {@link Source} for which the count should be set
     * @param count the new value for that {@link Source}
     * @deprecated use {@link #set(Source, int)} instead
     */
    @Deprecated
    void setSourceCount(Source source, int count) {
        sourcesCount.put(source, count);
    }

    /**
     * Set all {@link PubmedEvidence} for {@link Source}. Note that this method does NOT increment evidence count!
     * To prevent wrongly setting information, adding {@link PubmedEvidence} through
     * {@link #add(Source, PubmedEvidence)} is suggested instead.
     * @param source the {@link Source} for which the {@link PubmedEvidence} should be set
     * @param evidence a {@link Set} containing all {@link PubmedEvidence} for that {@link Source}
     * @deprecated use {@link #set(Source, int, Set)} instead
     */
    @Deprecated
    void setPubmedEvidenceForSource(Source source, Set<PubmedEvidence> evidence) {
        pubmedEvidence.put(source, evidence);
    }

    @Override
    public String toString() {
        return "GeneDiseaseCombination{" +
                "disgenetScore=" + disgenetScore +
                ", sourcesCount=" + sourcesCount +
                ", pubmedEvidence=" + pubmedEvidence +
                ' ' + super.toString() +
                '}';
    }

    @Override
    public boolean allFieldsEquals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GeneDiseaseCombination that = (GeneDiseaseCombination) o;

        if (!(
                super.allFieldsEquals(that) &&
                Objects.equals(disgenetScore, that.disgenetScore) &&
                Objects.equals(sourcesCount, that.sourcesCount) &&
                Objects.equals(pubmedEvidence, that.pubmedEvidence)
        )) {
            return false;
        }

        // Checks allFieldsEquals() for all items in pubmedEvidence.
        for (Map.Entry<Source, Set<PubmedEvidence>> sourcePubmedEvidence : pubmedEvidence.entrySet()) {
            List<PubmedEvidence> thisPubmedEvidences = new ArrayList<>(sourcePubmedEvidence.getValue());
            Collections.sort(thisPubmedEvidences);

            List<PubmedEvidence> thatPubmedEvidences = new ArrayList<>(that.pubmedEvidence.get(sourcePubmedEvidence.getKey()));
            Collections.sort(thatPubmedEvidences);

            for (int i = 0; i < thisPubmedEvidences.size(); i++) {
                if (!thisPubmedEvidences.get(i).allFieldsEquals(thatPubmedEvidences.get(i))) {
                    return false;
                }
            }
        }

        return true;
    }
}
