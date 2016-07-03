package com.recipex.taskcallbacks;

import com.appspot.recipex_1281.recipexServerApi.model.MainDefaultResponseMessage;

/**
 * callback from DeleteMeasurementAT
 */
public interface DeleteMeasurementTC {
    void done(boolean res, String calId, MainDefaultResponseMessage response);
}
