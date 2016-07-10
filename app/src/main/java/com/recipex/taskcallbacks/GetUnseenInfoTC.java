package com.recipex.taskcallbacks;

/**
 * Created by Sara on 06/07/2016.
 */

import com.appspot.recipex_1281.recipexServerApi.model.MainUserUnseenInfoMessage;

/**
 * callback for GetUnseenInfoAT
 */
public interface GetUnseenInfoTC {
    void done(MainUserUnseenInfoMessage m);
}
