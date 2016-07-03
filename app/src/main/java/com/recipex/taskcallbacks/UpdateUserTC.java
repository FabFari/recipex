package com.recipex.taskcallbacks;

import com.appspot.recipex_1281.recipexServerApi.model.MainDefaultResponseMessage;

/**
 * callback from UpdateUserAT
 */
public interface UpdateUserTC {
    void done(boolean res, MainDefaultResponseMessage response);
}
