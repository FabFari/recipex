package com.recipex.taskcallbacks;

import com.appspot.recipex_1281.recipexServerApi.model.MainUserListOfUsersMessage;

/**
 * callback from GetUsersAT
 */
public interface GetUsersTC {
    void done(boolean res, MainUserListOfUsersMessage response);
}
