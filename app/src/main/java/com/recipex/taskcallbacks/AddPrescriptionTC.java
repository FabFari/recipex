package com.recipex.taskcallbacks;

import com.appspot.recipex_1281.recipexServerApi.model.MainDefaultResponseMessage;

/**
 * Created by Sara on 04/05/2016.
 */

/**
 * callback from AddPrescriptionAT
 */
public interface AddPrescriptionTC {
    void done(boolean b, MainDefaultResponseMessage m);
}
