package com.recipex.taskcallbacks;

import com.appspot.recipex_1281.recipexServerApi.model.MainDefaultResponseMessage;

import java.util.ArrayList;
import java.util.LinkedList;

public interface RimuoviTerapiaTC {
    void done(boolean res, ArrayList<String> ids, MainDefaultResponseMessage response);
}
