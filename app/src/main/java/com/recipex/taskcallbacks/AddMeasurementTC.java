package com.recipex.taskcallbacks;

import com.appspot.recipex_1281.recipexServerApi.model.MainDefaultResponseMessage;

/**
 * callback from AddMeasurementAT
 */
public interface AddMeasurementTC {
    public void done(boolean resp, MainDefaultResponseMessage response);
}
