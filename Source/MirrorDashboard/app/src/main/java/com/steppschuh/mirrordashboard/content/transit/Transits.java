package com.steppschuh.mirrordashboard.content.transit;

import com.steppschuh.mirrordashboard.content.Content;

import java.util.List;

public class Transits extends Content {

    public static final int TRANSITS_COUNT_DEFAULT = 10;

    private List<Transit> nextTransits;

    public Transits() {
        super(Content.TYPE_TRANSIT);
    }

    public Transits(List<Transit> nextTransits) {
        super(Content.TYPE_TRANSIT);
        this.nextTransits = nextTransits;
    }

    public void trimNextTransits() {
        trimNextTransits(TRANSITS_COUNT_DEFAULT);
    }

    public void trimNextTransits(int count) {
        while (nextTransits.size() > count) {
            nextTransits.remove(nextTransits.size() - 1);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder()
                .append(nextTransits.size())
                .append(" transit(s):");
        for (Transit transit : nextTransits) {
            sb.append("\n").append(transit);
        }
        return sb.toString();
    }

    public List<Transit> getNextTransits() {
        return nextTransits;
    }

    public void setNextTransits(List<Transit> nextTransits) {
        this.nextTransits = nextTransits;
    }

}
