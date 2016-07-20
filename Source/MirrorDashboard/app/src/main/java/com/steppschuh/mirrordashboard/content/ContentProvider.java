package com.steppschuh.mirrordashboard.content;

public class ContentProvider {

    private int type;

    public ContentProvider(int type) {
        this.type = type;
    }

    public Content fetchContent() throws Exception {
        throw new Exception("Fetch method not implemented");
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

}
