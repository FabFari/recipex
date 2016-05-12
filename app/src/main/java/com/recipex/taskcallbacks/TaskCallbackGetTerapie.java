package com.recipex.taskcallbacks;

import com.appspot.recipex_1281.recipexServerApi.model.MainUserPrescriptionsMessage;

/**
 * Created by Sara on 08/05/2016.
 */
public interface TaskCallbackGetTerapie {
    void done(boolean b, MainUserPrescriptionsMessage m);
}
