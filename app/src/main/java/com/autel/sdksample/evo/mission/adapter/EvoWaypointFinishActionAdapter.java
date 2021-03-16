package com.autel.sdksample.evo.mission.adapter;

import android.content.Context;

import com.autel.common.mission.evo.EvoWaypointFinishedAction;
import com.autel.common.mission.evo2.Evo2WaypointFinishedAction;
import com.autel.sdksample.base.adapter.SelectorAdapter;

public class EvoWaypointFinishActionAdapter extends SelectorAdapter<Evo2WaypointFinishedAction> {

    public EvoWaypointFinishActionAdapter(Context context) {
        super(context);
        elementList.add(Evo2WaypointFinishedAction.KEEP_ON_LAST_POINT);
        elementList.add(Evo2WaypointFinishedAction.LAND_ON_LAST_POINT);
        elementList.add(Evo2WaypointFinishedAction.RETURN_HOME);
        elementList.add(Evo2WaypointFinishedAction.RETURN_TO_FIRST_POINT);
        elementList.add(Evo2WaypointFinishedAction.STOP_ON_LAST_POINT);
    }
}
