package com.steppschuh.mirrordashboard.content;

public interface ContentUpdateListener {

    public void onContentUpdated(Content content);

    public void onContentUpdateFailed(ContentUpdater contentUpdater, ContentUpdateException exception);

}
