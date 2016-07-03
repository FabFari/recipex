package com.recipex.taskcallbacks;

import com.appspot.recipex_1281.recipexServerApi.model.MainUserInfoMessage;

/**
 * callback from GetUserAT
 */
public interface GetUserTC {
    void done(boolean res, MainUserInfoMessage response);
}
