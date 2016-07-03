package com.recipex.taskcallbacks;

import com.appspot.recipex_1281.recipexServerApi.model.MainUserPrescriptionsMessage;

/**
 * Created by Sara on 08/05/2016.
 */

/**
 * callback from GetPrescriptionsUserAT
 */
public interface GetPrescriptionsTC {
    void done(boolean b, MainUserPrescriptionsMessage m);
}
