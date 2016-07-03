package com.recipex.taskcallbacks;

import com.appspot.recipex_1281.recipexServerApi.model.MainActiveIngredientsMessage;

/**
 * Created by Sara on 08/05/2016.
 */

/**
 * callback from GetMainIngredientsAT
 */
public interface ActiveIngredientsTC {
    void done(MainActiveIngredientsMessage m);
}
