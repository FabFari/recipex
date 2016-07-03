package com.recipex.taskcallbacks;

import com.appspot.recipex_1281.recipexServerApi.model.MainUserRelationsMessage;

/**
 * callback from CheckUserRelationsAT
 */
public interface CheckUserRelationsTC {
    public void done(boolean resp, MainUserRelationsMessage response);
}
