package com.recipex.taskcallbacks;

import com.appspot.recipex_1281.recipexServerApi.model.MainUserInfoMessage;

public interface GetUserTC {
    void done(boolean res, MainUserInfoMessage response);
}
