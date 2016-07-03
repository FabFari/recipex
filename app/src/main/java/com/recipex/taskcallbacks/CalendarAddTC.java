package com.recipex.taskcallbacks;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Sara on 26/05/2016.
 */

/**
 * callback from AggiungiMisurazioneCalendar and AggiungiTerapiaCalendar
 */
public interface CalendarAddTC {
    void done(boolean b, ArrayList<String> l);
}
