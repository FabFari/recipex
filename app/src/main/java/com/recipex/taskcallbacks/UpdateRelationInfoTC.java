package com.recipex.taskcallbacks;

import com.appspot.recipex_1281.recipexServerApi.model.MainDefaultResponseMessage;

/**
 * Created by Fabrizio on 26/05/2016.
 */

/**
 * callback from UpdateRelationInfoAT
 */
public interface UpdateRelationInfoTC {
    public void done(boolean resp, MainDefaultResponseMessage response);
}
