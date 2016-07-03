package com.recipex.taskcallbacks;

import com.appspot.recipex_1281.recipexServerApi.model.MainDefaultResponseMessage;

/**
 * callback from SendRequestAT
 */
public interface SendRequestTC {
    public void done(boolean res, MainDefaultResponseMessage response);
}
