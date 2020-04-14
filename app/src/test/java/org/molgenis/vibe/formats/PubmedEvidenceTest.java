package org.molgenis.vibe.formats;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.molgenis.vibe.exceptions.InvalidStringFormatException;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PubmedEvidenceTest {
    @Test
    public void useValidIdWithLowercasePrefix() {
        PubmedEvidence pubmedEvidence = new PubmedEvidence("pmid:1234", 2000);
        testIfValid(pubmedEvidence);
    }

    @Test
    public void useValidIdWithUppercasePrefix() {
        PubmedEvidence pubmedEvidence = new PubmedEvidence("PMID:1234", 2000);
        testIfValid(pubmedEvidence);
    }

    @Test
    public void useValidIdWithSingleUpperCasePrefix1() {
        Assertions.assertThrows(InvalidStringFormatException.class, () -> new PubmedEvidence("Pmid:1234", 2000) );
    }

    @Test
    public void useValidIdWithSingleUpperCasePrefix2() {
        Assertions.assertThrows(InvalidStringFormatException.class, () -> new PubmedEvidence("pMid:1234", 2000) );
    }

    @Test
    public void useValidIdWithInvalidPrefix() {
        Assertions.assertThrows(InvalidStringFormatException.class, () -> new PubmedEvidence("pm:1234", 2000) );
    }

    @Test
    public void useValidIdWithoutPrefix() {
        Assertions.assertThrows(InvalidStringFormatException.class, () -> new PubmedEvidence("1234", 2000) );
    }

    @Test
    public void useUriAsIdInput() {
        Assertions.assertThrows(InvalidStringFormatException.class, () -> new PubmedEvidence("http://identifiers.org/pubmed/1234", 2000) );
    }

    @Test
    public void useValidUri() {
        PubmedEvidence pubmedEvidence = new PubmedEvidence(URI.create("http://identifiers.org/pubmed/1234"), 2000);
        testIfValid(pubmedEvidence);
    }

    @Test
    public void useInvalidUri() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new PubmedEvidence(URI.create("http://identifiers.org/pm/1234"), 2000) );
    }

    @Test
    public void testSort() {
        List<PubmedEvidence> actualOrder = new ArrayList<>( Arrays.asList(
                new PubmedEvidence("pmid:20", 2002),
                new PubmedEvidence("pmid:3", 2004),
                new PubmedEvidence("pmid:8", 2001),
                new PubmedEvidence("pmid:1", 2003)
        ));

        List<PubmedEvidence> expectedOrder = new ArrayList<>( Arrays.asList(
                actualOrder.get(3),
                actualOrder.get(1),
                actualOrder.get(2),
                actualOrder.get(0)
        ));

        Collections.sort(actualOrder);
        Assertions.assertEquals(expectedOrder, actualOrder);
    }

    private void testIfValid(PubmedEvidence pubmedEvidence) {
        Assertions.assertAll(
                () -> Assertions.assertEquals("1234", pubmedEvidence.getId()),
                () -> Assertions.assertEquals("pmid:1234", pubmedEvidence.getFormattedId()),
                () -> Assertions.assertEquals(URI.create("http://identifiers.org/pubmed/1234"), pubmedEvidence.getUri())
        );
    }

    @Test
    public void testEqualsIdToEqualId() {
        Assertions.assertTrue(new PubmedEvidence("pmid:1234", 2000).equals(new PubmedEvidence("pmid:1234", 2000)));
    }

    @Test
    public void testEqualsUriToEqualUri() {
        Assertions.assertTrue(new PubmedEvidence(URI.create("http://identifiers.org/pubmed/1234"), 2000).equals(new PubmedEvidence(URI.create("http://identifiers.org/pubmed/1234"), 2000)));
    }

    @Test
    public void testEqualsIdToEqualUri() {
        Assertions.assertTrue(new PubmedEvidence("pmid:1234", 2000).equals(new PubmedEvidence(URI.create("http://identifiers.org/pubmed/1234"), 2000)));
    }

    @Test
    public void testEqualsIdToDifferentId() {
        Assertions.assertFalse(new PubmedEvidence("pmid:1234", 2000).equals(new PubmedEvidence("pmid:5678", 2000)));
    }
}