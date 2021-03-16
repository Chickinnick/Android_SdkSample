package com.autel.sdksample.evo.mission.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;

import com.autel.common.mission.AutelCoordinate3D;
import com.autel.common.mission.AutelMission;
import com.autel.common.mission.MissionType;
import com.autel.common.mission.evo2.Evo2Waypoint;
import com.autel.common.mission.evo2.Evo2WaypointFinishedAction;
import com.autel.common.mission.evo2.Evo2WaypointMission;
import com.autel.common.mission.xstar.Waypoint;
import com.autel.sdksample.R;
import com.autel.sdksample.base.mission.AutelLatLng;
import com.autel.sdksample.base.mission.MapActivity;
import com.autel.sdksample.base.mission.MapOperator;
import com.autel.sdksample.base.mission.fragment.WaypointMissionFragment;
import com.autel.sdksample.evo.mission.adapter.EvoWaypointFinishActionAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 *
 */

public class EvoWaypointFragment extends WaypointMissionFragment {
    private Spinner finishActionSpinner;
    private EditText waypointSpeed;
    private EditText waypointReturnHeight;
    private EditText waypointHeight;
    private EvoWaypointFinishActionAdapter finishActionAdapter = null;
    private Evo2WaypointFinishedAction finishedAction = Evo2WaypointFinishedAction.UNKNOWN;
    List<Evo2Waypoint> wayPointList = new ArrayList<>();

    @SuppressLint("ValidFragment")
    public EvoWaypointFragment(MapOperator mMapOperator) {
        super(mMapOperator);
    }

    public EvoWaypointFragment() {
        super();
    }

    @Override
    protected Evo2Waypoint getWaypoint(int index) {
        if (index >= wayPointList.size()) {
            return null;
        }
        return wayPointList.get(index);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = createView(R.layout.fragment_mission_menu_waypoint);

        ((MapActivity) getActivity()).setWaypointHeightListener(new MapActivity.WaypointHeightListener() {
            @Override
            public int fetchHeight() {
                String valueHeight = waypointHeight.getText().toString();
                return isEmpty(valueHeight) ? 50 : Integer.valueOf(valueHeight);
            }
        });
        finishActionSpinner = (Spinner) view.findViewById(R.id.finishAction);
        finishActionAdapter = new EvoWaypointFinishActionAdapter(getContext());
        finishActionSpinner.setAdapter(finishActionAdapter);
        finishActionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                finishedAction = (Evo2WaypointFinishedAction) parent.getAdapter().getItem(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        waypointSpeed = (EditText) view.findViewById(R.id.waypointSpeed);
        waypointReturnHeight = (EditText) view.findViewById(R.id.waypointReturnHeight);
        waypointHeight = (EditText) view.findViewById(R.id.waypointHeight);

        return view;
    }

    @Override
    public AutelMission createAutelMission() {
        Evo2WaypointMission waypointMission = new Evo2WaypointMission();
        waypointMission.finishedAction = finishedAction;
//
           waypointMission.missionId = 2; //Mission id
        waypointMission.missionType = MissionType.Waypoint; //Mission type (Waypoint (waypoint), RECTANGLE (rectangle), POLYGON (polygon))
        waypointMission.totalFlyTime = 351; //Total flight time (unit s)
        waypointMission.totalDistance = 897; //Total flight distance (in m)
        waypointMission.VerticalFOV = 53.6f; //Read real-time heartbeat data of the camera
        waypointMission.HorizontalFOV = 68.0f; //Read real-time heartbeat data of the camera
        waypointMission.PhotoIntervalMin = 1020;
        waypointMission.MissionName = "Mission_1";
        waypointMission.GUID = UUID.randomUUID().toString().replace("-", "");

        waypointMission.missionAction = 1;

        String valueReturnHeight = waypointReturnHeight.getText().toString();
        waypointMission.finishReturnHeight = isEmpty(valueReturnHeight) ? 40 : Integer.valueOf(valueReturnHeight);
        waypointMission.wpList = wayPointList;
        return waypointMission;
    }


    @Override
    protected void waypointAdded(AutelLatLng latLng) {
        String wHeight = waypointHeight.getText().toString();
        Evo2Waypoint waypoint = new Evo2Waypoint(new AutelCoordinate3D(latLng.latitude, latLng.longitude, isEmpty(wHeight) ? 0 : Double.valueOf(wHeight)));
        wayPointList.add(waypoint);
        Log.i("QWE1EvoWaypointFragment", "waypointAdded: __");
    }

    public void hideBar() {
        if (getActivity() == null) {
            return;
        }
        WindowManager.LayoutParams params = getActivity().getWindow().getAttributes();
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        getActivity().getWindow().setAttributes(params);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        wayPointList = null;
    }
}
