package com.recipex.taskcallbacks;

import com.appspot.recipex_1281.recipexServerApi.model.MainUserMeasurementsMessage;

/**
 * Created by Sara on 11/05/2016.
 */
public interface TaskCallbackGetMeasurements {
    void done(MainUserMeasurementsMessage m);
}
