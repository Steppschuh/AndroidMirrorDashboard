package com.steppschuh.mirrordashboard.pattern;

public class GenericPatternItem implements PatternItem {

    int value;

    public GenericPatternItem(int value) {
        this.value = value;
    }

    @Override
    public int getPatternValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(getPatternValue());
    }

}
