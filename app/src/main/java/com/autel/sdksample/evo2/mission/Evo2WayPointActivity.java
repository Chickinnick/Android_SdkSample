package com.autel.sdksample.evo2.mission;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.autel.common.CallbackWithNoParam;
import com.autel.common.CallbackWithOneParam;
import com.autel.common.CallbackWithOneParamProgress;
import com.autel.common.battery.evo.EvoBatteryInfo;
import com.autel.common.error.AutelError;
import com.autel.common.flycontroller.ARMWarning;
import com.autel.common.flycontroller.evo.EvoFlyControllerInfo;
import com.autel.common.mission.AutelCoordinate3D;
import com.autel.common.mission.AutelMission;
import com.autel.common.mission.MissionType;
import com.autel.common.mission.RealTimeInfo;
import com.autel.common.mission.evo.MissionActionType;
import com.autel.common.mission.evo.WaypointAction;
import com.autel.common.mission.evo.WaypointHeadingMode;
import com.autel.common.mission.evo.WaypointType;
import com.autel.common.mission.evo2.Evo2Waypoint;
import com.autel.common.mission.evo2.Evo2WaypointFinishedAction;
import com.autel.common.mission.evo2.Evo2WaypointMission;
import com.autel.common.mission.evo2.Poi;
import com.autel.common.product.AutelProductType;
import com.autel.common.remotecontroller.RemoteControllerInfo;
import com.autel.internal.sdk.mission.evo2.Evo2WaypointRealTimeInfoImpl;
import com.autel.sdk.battery.EvoBattery;
import com.autel.sdk.flycontroller.Evo2FlyController;
import com.autel.sdk.mission.MissionManager;
import com.autel.sdk.product.BaseProduct;
import com.autel.sdk.remotecontroller.AutelRemoteController;
import com.autel.sdksample.R;
import com.autel.sdksample.TestApplication;
import com.autel.util.log.AutelLog;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class Evo2WayPointActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "Nikc";
    private Evo2WaypointMission autelMission;
    private Evo2FlyController mEvoFlyController;
    private EvoBattery battery;
    private AutelRemoteController remoteController;
    private MissionManager missionManager;
    private float lowBatteryPercent = 15f;
    private boolean isBatteryOk = true; //当前电量是否合适
    private boolean isCompassOk = false; //当前指南针状态是否OK
    private boolean isImuOk = false; //当前IMU是否OK
    private boolean isGpsOk = false; //当前gps是否OK
    private boolean isImageTransOk = false; //当前图传信号是否OK
    private boolean isCanTakeOff = false; //是否能起飞

    enum FlyState {
        Prepare, Start, Pause, None
    }

    private FlyState flyState = FlyState.None;

    private int id = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTitle("WayPoint");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evo2_waypoint);

        BaseProduct product = ((TestApplication) getApplicationContext()).getCurrentProduct();
        if (null != product && product.getType() == AutelProductType.EVO_2) {
            missionManager = product.getMissionManager();
            missionManager.setRealTimeInfoListener(new CallbackWithOneParam<RealTimeInfo>() {
                @Override
                public void onSuccess(RealTimeInfo realTimeInfo) {
                    Evo2WaypointRealTimeInfoImpl info = (Evo2WaypointRealTimeInfoImpl) realTimeInfo;
                    Log.i("Nikc" ,"MissionRunning"+ "timeStamp:" + info.timeStamp + ",speed:" + info.speed + ",isArrived:" + info.isArrived +
                            ",isDirecting:" + info.isDirecting + ",waypointSequence:" + info.waypointSequence + ",actionSequence:" + info.actionSequence +
                            ",photoCount:" + info.photoCount + ",MissionExecuteState:" + info.executeState + ",remainFlyTime:" + info.remainFlyTime);
                }

                @Override
                public void onFailure(AutelError autelError) {

                }
            });

            battery = (EvoBattery) product.getBattery();
            battery.getLowBatteryNotifyThreshold(new CallbackWithOneParam<Float>() {
                @Override
                public void onSuccess(Float aFloat) {
                    lowBatteryPercent = aFloat;
                }

                @Override
                public void onFailure(AutelError autelError) {

                }
            });
            battery.setBatteryStateListener(new CallbackWithOneParam<EvoBatteryInfo>() {
                @Override
                public void onSuccess(EvoBatteryInfo batteryState) {
                    Log.d("Nikc" ," batteryState "+batteryState.getRemainingPercent());
//                    isBatteryOk = batteryState.getRemainingPercent() > lowBatteryPercent;
                }

                @Override
                public void onFailure(AutelError autelError) {

                }
            });

            mEvoFlyController = (Evo2FlyController) product.getFlyController();
            mEvoFlyController.setFlyControllerInfoListener(new CallbackWithOneParam<EvoFlyControllerInfo>() {
                @Override
                public void onSuccess(EvoFlyControllerInfo evoFlyControllerInfo) {
                    isCompassOk = evoFlyControllerInfo.getFlyControllerStatus().isCompassValid();
                    isCanTakeOff = evoFlyControllerInfo.getFlyControllerStatus().isTakeOffValid();

                    isImuOk = evoFlyControllerInfo.getFlyControllerStatus().getArmErrorCode() != ARMWarning.IMU_LOSS
                            && evoFlyControllerInfo.getFlyControllerStatus().getArmErrorCode() != ARMWarning.DISARM_IMU_LOSS;

                    isGpsOk = evoFlyControllerInfo.getFlyControllerStatus().isGpsValid();
                }

                @Override
                public void onFailure(AutelError autelError) {

                }
            });

            remoteController = product.getRemoteController();
            remoteController.setInfoDataListener(new CallbackWithOneParam<RemoteControllerInfo>() {
                @Override
                public void onSuccess(RemoteControllerInfo remoteControllerInfo) {
                    isImageTransOk = remoteControllerInfo.getDSPPercentage() >= 30;
                }

                @Override
                public void onFailure(AutelError autelError) {

                }
            });
        }
        Log.d("Nikc" ,"init missionManager" + missionManager);
        initView();
        initData();
    }

    private void initView() {
        findViewById(R.id.prepare).setOnClickListener(this);
        findViewById(R.id.start).setOnClickListener(this);
        findViewById(R.id.pause).setOnClickListener(this);
        findViewById(R.id.resume).setOnClickListener(this);
        findViewById(R.id.cancel).setOnClickListener(this);
        findViewById(R.id.download).setOnClickListener(this);
    }

    private void initData() {
        autelMission = new Evo2WaypointMission();

        autelMission.missionId = id++; //Mission id
        autelMission.missionType = MissionType.Waypoint; //Mission type (Waypoint (waypoint), RECTANGLE (rectangle), POLYGON (polygon))
        autelMission.totalFlyTime = 351; //Total flight time (unit s)
        autelMission.totalDistance = 897; //Total flight distance (in m)
        autelMission.VerticalFOV = 53.6f; //Read real-time heartbeat data of the camera
        autelMission.HorizontalFOV = 68.0f; //Read real-time heartbeat data of the camera
        autelMission.PhotoIntervalMin = 1020;
        autelMission.MissionName = "Mission_1";
        autelMission.GUID = UUID.randomUUID().toString().replace("-", "");

        autelMission.missionAction = 1;//1-The aircraft flies in a straight line with the current waypoint and the next waypoint without decelerating, 0-normal flight
        List<Evo2Waypoint> wpList = new ArrayList<>();

        //Waypoint 1 (action: fly over)
        Evo2Waypoint cruiserWaypoint1 = new Evo2Waypoint(new AutelCoordinate3D(22.5966492303896, 113.99885752564695, 60)); //Latitude, longitude, flight altitude
        cruiserWaypoint1.wSpeed = 5; //speed (unit m/s)
        cruiserWaypoint1.poiIndex = -1; //Associated point of interest id
        cruiserWaypoint1.flyTime = 0; //Flight time (unit: s)
        cruiserWaypoint1.hoverTime = 10; //0 when flying over, the specific time is passed in when hovering (unit s)
        cruiserWaypoint1.flyDistance = 0;//Flying distance (in m)
        cruiserWaypoint1.headingMode = WaypointHeadingMode.CUSTOM_DIRECTION; //Heading along the route
        cruiserWaypoint1.waypointType = WaypointType.HOVER; //Waypoint type: STANDARD or HOVER
        //Waypoint 1 is flying over, you can add 0 or 1 camera action
        List<WaypointAction> list1 = new ArrayList<>();
        //Add camera action
        WaypointAction action1 = new WaypointAction();
        action1.actionType = MissionActionType.START_RECORD; //Start recording
        action1.parameters = new int[]{45, 0, 0, 0, 0, 0, 0, 20, 0, 0}; //Set recording parameters (parameter 1: PTZ pitch angle parameter 2: head angle angle The remaining parameters vary according to different camera actions)
        list1.add(action1);
        cruiserWaypoint1.actions = list1;
        wpList.add(cruiserWaypoint1);

        //Waypoint 2 (action: hover)
        Evo2Waypoint cruiserWaypoint2 = new Evo2Waypoint(new AutelCoordinate3D(22.59628621670881, 113.99741950976092, 60));
        cruiserWaypoint2.wSpeed =5;
// cruiserWaypoint2.poiIndex = 1; //The id of the associated point of interest, that is, the second point of interest is associated
        cruiserWaypoint2.poiIndex = -1;
        cruiserWaypoint2.hoverTime = 5;
        cruiserWaypoint2.flyDistance = 153;
        cruiserWaypoint2.headingMode = WaypointHeadingMode.CUSTOM_DIRECTION;
        cruiserWaypoint2.waypointType = WaypointType.HOVER;
        //Waypoint 2 is the hovering point, 0 or more camera actions can be added
        List<WaypointAction> list2 = new ArrayList<>();
        //Add camera action 1 fixed real shot (time interval is 2s, fixed real time is 40s)
        WaypointAction point2Action1 = new WaypointAction();
        point2Action1.actionType = MissionActionType.START_TIME_LAPSE_SHOOT;
        point2Action1.parameters = new int[]{-45, 90, 2, 40, 0, 0, 0, 20, 0, 0};
        list2.add(point2Action1);
        wpList.add(cruiserWaypoint2);
        //Add camera action 2 to start recording (recording duration is 60s)
        WaypointAction point2Action2 = new WaypointAction();
        point2Action2.actionType = MissionActionType.START_RECORD;
        point2Action2.parameters = new int[]{-23, 90, 0, 0, 0, 60, 0, 20, 0, 0};
        list2.add(point2Action2);

        cruiserWaypoint2.actions = list2;
// wpList.add(cruiserWaypoint2);

        //Waypoint 3 (action: leap)
        Evo2Waypoint cruiserWaypoint3 = new Evo2Waypoint(new AutelCoordinate3D(22.59563928164338, 113.99866562877735, 60));
        cruiserWaypoint3.wSpeed = 5;
        cruiserWaypoint3.poiIndex = -1;
        cruiserWaypoint3.flyTime = 190;
        cruiserWaypoint3.hoverTime = 0;
        cruiserWaypoint3.flyDistance = 300;
        cruiserWaypoint3.headingMode = WaypointHeadingMode.CUSTOM_DIRECTION;
        cruiserWaypoint3.waypointType = WaypointType.STANDARD;
        List<WaypointAction> list3 = new ArrayList<>();
        WaypointAction point3Action1 = new WaypointAction();
        point3Action1.actionType = MissionActionType.TAKE_PHOTO; //Photo
        point3Action1.parameters = new int[]{0, 90, 0, 0, 0, 0, 0, 20, 0, 0};
        list3.add(point3Action1);
        cruiserWaypoint3.actions = list3;
        wpList.add(cruiserWaypoint3);

        //Waypoint 4 (action: leap)
        Evo2Waypoint cruiserWaypoint4 = new Evo2Waypoint(new AutelCoordinate3D(22.595273074299133, 113.9969537182374, 60));
        cruiserWaypoint4.wSpeed=5;
        cruiserWaypoint4.poiIndex = -1;
        cruiserWaypoint4.flyTime = 234;
        cruiserWaypoint4.hoverTime = 0;
        cruiserWaypoint4.flyDistance = 481;
        cruiserWaypoint4.headingMode = WaypointHeadingMode.CUSTOM_DIRECTION;
        cruiserWaypoint4.waypointType = WaypointType.STANDARD;
        List<WaypointAction> list4 = new ArrayList<>();
        WaypointAction point4Action1 = new WaypointAction();
        point4Action1.actionType = MissionActionType.START_TIME_LAPSE_SHOOT; //Timed photo (2s interval)
        point4Action1.parameters = new int[]{0, 90, 2, 0, 0, 0, 0, 20, 0, 0};
        list4.add(point4Action1);
        cruiserWaypoint4.actions = list4;
        wpList.add(cruiserWaypoint4);

        //Waypoint 5 (action: leap)
        Evo2Waypoint cruiserWaypoint5 = new Evo2Waypoint(new AutelCoordinate3D(22.595157667753398, 113.99928502161195, 60));
        cruiserWaypoint5.wSpeed = 5;
        cruiserWaypoint5.poiIndex = -1;
        cruiserWaypoint5.flyTime = 295;
        cruiserWaypoint5.hoverTime = 0;
        cruiserWaypoint5.flyDistance = 722;
        cruiserWaypoint5.headingMode = WaypointHeadingMode.CUSTOM_DIRECTION;
        cruiserWaypoint5.waypointType = WaypointType.STANDARD;
        List<WaypointAction> list5 = new ArrayList<>();
        WaypointAction point5Action1 = new WaypointAction();
        point5Action1.actionType = MissionActionType.START_DISTANCE_SHOOT; //定距拍照(10m间隔)
        point5Action1.parameters = new int[]{0, 90, 0, 0, 10, 0, 0, 20, 0, 0};
        list5.add(point5Action1);
        cruiserWaypoint5.actions = list5;
        wpList.add(cruiserWaypoint5);

        //航点6（动作：飞跃）
        Evo2Waypoint cruiserWaypoint6 = new Evo2Waypoint(new AutelCoordinate3D(22.59583649616868, 22.59583649616868, 60));
        cruiserWaypoint6.wSpeed = 5;
        cruiserWaypoint6.poiIndex = -1;
        cruiserWaypoint6.flyTime = 326;
        cruiserWaypoint6.hoverTime = 0;
        cruiserWaypoint6.flyDistance = 825;
        cruiserWaypoint6.headingMode = WaypointHeadingMode.CUSTOM_DIRECTION;
        cruiserWaypoint6.waypointType = WaypointType.STANDARD;
        List<WaypointAction> list6 = new ArrayList<>();
        WaypointAction point6Action1 = new WaypointAction();
        point6Action1.actionType = MissionActionType.START_RECORD; //开始录像
        point6Action1.parameters = new int[]{0, 90, 0, 0, 0, 0, 0, 20, 0, 0};
        list6.add(point6Action1);
        cruiserWaypoint6.actions = list6;
//        wpList.add(cruiserWaypoint6);

        //航点7（动作：飞跃）
        Evo2Waypoint cruiserWaypoint7 = new Evo2Waypoint(new AutelCoordinate3D(22.59583649616868, 22.59583649616868, 60));
        cruiserWaypoint7.wSpeed = 5;
        cruiserWaypoint7.poiIndex = -1;
        cruiserWaypoint7.flyTime = 351;
        cruiserWaypoint7.hoverTime = 0;
        cruiserWaypoint7.flyDistance = 897;
        cruiserWaypoint7.headingMode = WaypointHeadingMode.CUSTOM_DIRECTION;
        cruiserWaypoint7.waypointType = WaypointType.STANDARD;
        List<WaypointAction> list7 = new ArrayList<>();
        WaypointAction point7Action1 = new WaypointAction();
        point7Action1.actionType = MissionActionType.STOP_RECORD; //结束录像
        point7Action1.parameters = new int[]{30, 90, 0, 0, 0, 0, 0, 20, 0, 0};
        list7.add(point7Action1);
        cruiserWaypoint7.actions = list7;
//        wpList.add(cruiserWaypoint7);


        //添加兴趣点（可添加0个或多个兴趣点）
        List<Poi> poiList = new ArrayList<>();
        Poi cruiserPoi1 = new Poi();
        cruiserPoi1.id = 0;
        cruiserPoi1.coordinate3D = new AutelCoordinate3D(22.59594093275901, 113.99941807396686, 60);
        poiList.add(cruiserPoi1);

        Poi cruiserPoi2 = new Poi();
        cruiserPoi2.id = 1;
        cruiserPoi2.coordinate3D = new AutelCoordinate3D(22.595821147877796, 113.99901208495906, 60);
        poiList.add(cruiserPoi2);

//        autelMission.wpoiList = poiList;
        autelMission.wpList = wpList;
        autelMission.finishedAction = Evo2WaypointFinishedAction.RETURN_HOME;
    }

    @Override
    public void onClick(View view) {
        final int id = view.getId();
        switch (id) {
            case R.id.prepare: {
                //飞行之前，必须进行必要的飞行检查
                if (!flyCheck()) {
                    return;
                }

                if (flyState != FlyState.None) {
                    Log.i("TAG", "onClick: flyState${ " + flyState.name());
                    Toast.makeText(Evo2WayPointActivity.this, "Current status " +flyState.name()+ ", cannot be executed", Toast.LENGTH_LONG).show();
                    return;
                }
                if (null != missionManager) {
                    missionManager.prepareMission(autelMission, new CallbackWithOneParamProgress<Boolean>() {
                        @Override
                        public void onProgress(float v) {
                            Log.d(TAG ,"prepareMission onProgress " +v  );

                            Toast.makeText(Evo2WayPointActivity.this, "prepare success", Toast.LENGTH_LONG).show();

                        }

                        @Override
                        public void onSuccess(Boolean aBoolean) {
                            flyState = FlyState.Prepare;
                            Log.d(TAG ,"prepareMission success");
                            Toast.makeText(Evo2WayPointActivity.this, "prepare success", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onFailure(AutelError autelError) {
                            Log.d(TAG ,"prepareMission onFailure");
                            Toast.makeText(Evo2WayPointActivity.this, "prepare failed", Toast.LENGTH_LONG).show();

                        }
                    });
                }
            }
            break;

            case R.id.start: {
                if (flyState != FlyState.Prepare) {
                    Toast.makeText(Evo2WayPointActivity.this, "当前状态，不能执行", Toast.LENGTH_LONG).show();
                    return;
                }
                if (null != missionManager) {
                    missionManager.startMission(new CallbackWithNoParam() {
                        @Override
                        public void onSuccess() {
                            flyState = FlyState.Start;
                            Toast.makeText(Evo2WayPointActivity.this, "start success", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onFailure(AutelError autelError) {
                            Log.e(TAG, "onFailure: startMission error: " + autelError.getDescription() + "error name/code: " +autelError.getErrCode().name() + "/" + autelError.getErrCode().getCode());
                        }
                    });
                }
            }
            break;

            case R.id.pause: {
                if (flyState != FlyState.Start) {
                    Toast.makeText(Evo2WayPointActivity.this, "当前状态，不能执行", Toast.LENGTH_LONG).show();
                    return;
                }
                if (null != missionManager) {
                    missionManager.pauseMission(new CallbackWithNoParam() {
                        @Override
                        public void onSuccess() {
                            flyState = FlyState.Pause;
                            Toast.makeText(Evo2WayPointActivity.this, "pause success", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onFailure(AutelError autelError) {
                            Log.e(TAG, "onFailure: pause" + autelError.getDescription() );

                        }
                    });
                }
            }
            break;

            case R.id.resume: {
                if (flyState != FlyState.Pause) {
                    Toast.makeText(Evo2WayPointActivity.this, "当前状态，不能执行", Toast.LENGTH_LONG).show();
                    return;
                }
                if (null != missionManager) {
                    missionManager.resumeMission(new CallbackWithNoParam() {
                        @Override
                        public void onSuccess() {
                            flyState = FlyState.Start;
                            Toast.makeText(Evo2WayPointActivity.this, "continue success", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onFailure(AutelError autelError) {
                            Log.e(TAG, "onFailure: resume" + autelError.getDescription() );

                        }
                    });
                }
            }
            break;

            case R.id.cancel: {
                if (flyState == FlyState.None) {
                    Toast.makeText(Evo2WayPointActivity.this, "当前状态，不能执行", Toast.LENGTH_LONG).show();
                    return;
                }
                if (null != missionManager) {
                    missionManager.cancelMission(new CallbackWithNoParam() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(Evo2WayPointActivity.this, "cancel success", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onFailure(AutelError autelError) {
                            Log.e(TAG, "onFailure: cancel" + autelError.getDescription() );

                        }
                    });
                }
            }
            break;

            case R.id.download: {
                if (flyState == FlyState.None) {
                    Toast.makeText(Evo2WayPointActivity.this, "当前状态，不能执行", Toast.LENGTH_LONG).show();
                    return;
                }
                if (null != missionManager) {
                    missionManager.downloadMission(new CallbackWithOneParamProgress<AutelMission>() {
                        @Override
                        public void onProgress(float v) {

                        }

                        @Override
                        public void onSuccess(AutelMission autelMission) {
                            Toast.makeText(Evo2WayPointActivity.this, "download success", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onFailure(AutelError autelError) {
                            Log.e(TAG, "onFailure: dld" + autelError.getDescription() );

                        }
                    });
                }
            }
            break;
        }
    }

    private boolean flyCheck() {
//        if (!isBatteryOk) {
//            Toast.makeText(Evo2WayPointActivity.this, "!isBatteryOk", Toast.LENGTH_LONG).show();
//            return false;
//        }
//
//        if (!isImuOk) {
//            Toast.makeText(Evo2WayPointActivity.this, "!isImuOk", Toast.LENGTH_LONG).show();
//            return false;
//        }
//
//        if (!isGpsOk) {
//            Toast.makeText(Evo2WayPointActivity.this, "!isGpsOk", Toast.LENGTH_LONG).show();
//            return false;
//        }
//
//        if (!isCompassOk) {
//            Toast.makeText(Evo2WayPointActivity.this, "!isCompassOk", Toast.LENGTH_LONG).show();
//            return false;
//        }
//
//        if (!isImageTransOk) {
//            Toast.makeText(Evo2WayPointActivity.this, "!isImageTransOk", Toast.LENGTH_LONG).show();
//            return false;
//        }
//        if (!isCanTakeOff) {
//            Toast.makeText(Evo2WayPointActivity.this, "!isCanTakeOff", Toast.LENGTH_LONG).show();
//            return false;
//        }

        return true;
    }
}
