package com.steppschuh.mirrordashboard.pattern;

import java.util.ArrayList;
import java.util.List;

public class Pattern<T extends PatternItem> {

    public static final int MAXIMUM_PATTERN_LENGTH = 7;
    public static final int MINIMUM_PATTERN_LENGTH = 2;

    public static final int LOW = 0;
    public static final int HIGH = 1;

    private List<T> patternSequence;

    public Pattern(List<T> patternSequence) {
        this.patternSequence = patternSequence;
    }

    public boolean matches(Pattern otherPattern) {
        return matches(otherPattern.getPatternSequence());
    }

    public boolean matches(List<T> otherPatternSequence) {
        return matches(patternSequence, otherPatternSequence);
    }

    public static boolean matches(List<? extends PatternItem> patternSequence, List<? extends PatternItem> otherPatternSequence) {
        if (patternSequence.size() != otherPatternSequence.size()) {
            return false;
        }
        for (int index = 0; index < patternSequence.size(); index++) {
            PatternItem item = patternSequence.get(index);
            PatternItem otherItem = otherPatternSequence.get(index);
            if (item.getPatternValue() != otherItem.getPatternValue()) {
                return false;
            }
        }
        return true;
    }

    public static Pattern createPattern(int... values) {
        List<PatternItem> patternSequence = createPatternSequence(values);
        return new Pattern(patternSequence);
    }

    public static List<PatternItem> createPatternSequence(int... values) {
        List<PatternItem> patternSequence = new ArrayList<>();
        for (int value : values) {
            patternSequence.add(new GenericPatternItem(value));
        }
        return patternSequence;
    }

    /**
     * Extracts all valid patterns from a given list of {@link PatternItem}s.
     *
     * @param patternSequence
     * @return
     */
    public static List<Pattern> extractPatternsFromSequence(List<? extends PatternItem> patternSequence) {
        List<Pattern> patterns = new ArrayList<>();
        for (int patternLength = Pattern.MINIMUM_PATTERN_LENGTH; patternLength <= Pattern.MAXIMUM_PATTERN_LENGTH; patternLength++) {
            if (patternLength > patternSequence.size()) {
                break;
            }
            int startIndex = patternSequence.size() - patternLength;
            int endIndex = patternSequence.size();
            List<? extends PatternItem> patternItems = patternSequence.subList(startIndex, endIndex);
            Pattern<PatternItem> pattern = new Pattern<>(new ArrayList<>(patternItems));
            patterns.add(pattern);
        }
        return patterns;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + ": " + patternSequence.toString();
    }

    public List<T> getPatternSequence() {
        return patternSequence;
    }

    public void setPatternSequence(List<T> patternSequence) {
        this.patternSequence = patternSequence;
    }
}
