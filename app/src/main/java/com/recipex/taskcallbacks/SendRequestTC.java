package com.recipex.taskcallbacks;

import com.appspot.recipex_1281.recipexServerApi.model.MainDefaultResponseMessage;

public interface SendRequestTC {
    public void done(boolean res, MainDefaultResponseMessage response);
}
