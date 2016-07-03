package com.recipex.taskcallbacks;

import com.appspot.recipex_1281.recipexServerApi.model.MainDefaultResponseMessage;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * callback from DeletePrescriptionAT
 */
public interface DeletePrescriptionTC {
    void done(boolean res, ArrayList<String> ids, MainDefaultResponseMessage response);
}
