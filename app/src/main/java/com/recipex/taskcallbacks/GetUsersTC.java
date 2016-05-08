package com.recipex.taskcallbacks;

import com.appspot.recipex_1281.recipexServerApi.model.MainUserListOfUsersMessage;

public interface GetUsersTC {
    void done(boolean res, MainUserListOfUsersMessage response);
}
