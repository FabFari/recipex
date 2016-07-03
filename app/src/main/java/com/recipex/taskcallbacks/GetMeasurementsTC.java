package com.recipex.taskcallbacks;

import com.appspot.recipex_1281.recipexServerApi.model.MainUserMeasurementsMessage;

/**
 * Created by Sara on 11/05/2016.
 */

/**
 * callback from GetMeasurementsUserAT
 */
public interface GetMeasurementsTC {
    void done(MainUserMeasurementsMessage m);
}
