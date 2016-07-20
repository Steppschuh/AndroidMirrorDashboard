package com.steppschuh.mirrordashboard.content;

import com.steppschuh.mirrordashboard.content.Content;

public interface ContentUpdateListener {

    public void onContentUpdated(Content content);

    public void onContentUpdateFailed(Exception exception);

}
