package com.steppschuh.mirrordashboard.pattern;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class PatternTest {

    @Test
    public void extractPatternsFromSequence_longSequence_validPatterns() throws Exception {
        List<PatternItem> patternSequence = Pattern.createPatternSequence(
                Pattern.HIGH,
                Pattern.LOW,
                Pattern.HIGH,
                Pattern.LOW,
                Pattern.HIGH,
                Pattern.LOW,
                Pattern.HIGH,
                Pattern.LOW,
                Pattern.HIGH,
                Pattern.LOW,
                Pattern.HIGH,
                Pattern.LOW
        );

        List<Pattern> patterns = Pattern.extractPatternsFromSequence(patternSequence);

        PatternItem expectedLastItem = patternSequence.get(patternSequence.size() - 1);
        PatternItem actualLastItem;
        for (Pattern<PatternItem> pattern : patterns) {
            actualLastItem = pattern.getPatternSequence().get(pattern.getPatternSequence().size() - 1);
            assertEquals(expectedLastItem, actualLastItem);
        }

        int expectedPatterns = Pattern.MAXIMUM_PATTERN_LENGTH - Pattern.MINIMUM_PATTERN_LENGTH + 1;
        assertEquals(expectedPatterns, patterns.size());
    }

    @Test
    public void extractPatternsFromSequence_shortSequence_validPatterns() throws Exception {
        List<PatternItem> patternSequence = Pattern.createPatternSequence(
                Pattern.HIGH,
                Pattern.LOW,
                Pattern.HIGH,
                Pattern.LOW,
                Pattern.HIGH
        );

        List<Pattern> patterns = Pattern.extractPatternsFromSequence(patternSequence);

        PatternItem expectedLastItem = patternSequence.get(patternSequence.size() - 1);
        PatternItem actualLastItem;
        for (Pattern<PatternItem> pattern : patterns) {
            actualLastItem = pattern.getPatternSequence().get(pattern.getPatternSequence().size() - 1);
            assertEquals(expectedLastItem, actualLastItem);
        }

        int expectedPatterns = patternSequence.size() - Pattern.MINIMUM_PATTERN_LENGTH + 1;
        assertEquals(expectedPatterns, patterns.size());
    }

}