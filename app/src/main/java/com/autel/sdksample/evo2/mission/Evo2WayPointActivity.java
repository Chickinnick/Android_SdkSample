package com.autel.sdksample.evo2.mission;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;
import com.autel.common.CallbackWithNoParam;
import com.autel.common.CallbackWithOneParam;
import com.autel.common.CallbackWithOneParamProgress;
import com.autel.common.error.AutelError;
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
import com.autel.internal.sdk.mission.evo2.Evo2WaypointRealTimeInfoImpl;
import com.autel.sdk.mission.MissionManager;
import com.autel.sdk.product.BaseProduct;
import com.autel.sdksample.R;
import com.autel.sdksample.TestApplication;
import com.autel.util.log.AutelLog;

import java.util.ArrayList;
import java.util.List;


public class Evo2WayPointActivity extends AppCompatActivity implements View.OnClickListener{

    private Evo2WaypointMission autelMission;
    private MissionManager missionManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTitle("WayPoint");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evo2_waypoint);

        BaseProduct product = ((TestApplication) getApplicationContext()).getCurrentProduct();
        if(null != product && product.getType() == AutelProductType.EVO_2){
            missionManager = product.getMissionManager();
        }

        initView();
        initData();
        initListener();
    }

    private void initView(){
        findViewById(R.id.prepare).setOnClickListener(this);
        findViewById(R.id.start).setOnClickListener(this);
        findViewById(R.id.pause).setOnClickListener(this);
        findViewById(R.id.cancel).setOnClickListener(this);
        findViewById(R.id.download).setOnClickListener(this);
    }

    private void initData(){
        autelMission = new Evo2WaypointMission();
        autelMission.missionId = 1; //任务id
        autelMission.missionType = MissionType.Waypoint; //任务类型(Waypoint(航点)、RECTANGLE(矩形)、POLYGON(多边形))
        autelMission.totalFlyTime = 1000; //总飞行时间(单位s)
        autelMission.totalDistance = 10000; //总飞行距离(单位m)
        autelMission.VerticalFOV = 1.0f; //相机实时心跳数据读取
        autelMission.HorizontalFOV = 1.0f; //相机实时心跳数据读取
        autelMission.PhotoIntervalMin = 2;

        List<Evo2Waypoint> wpList = new ArrayList<>();

        //航点1（动作：悬停）
        Evo2Waypoint cruiserWaypoint1 = new Evo2Waypoint(new AutelCoordinate3D(23.1234, 123.12345, 60));
        cruiserWaypoint1.wSpeed = 2; //单位m/s
        cruiserWaypoint1.poiIndex = 1; //关联的兴趣点id
        cruiserWaypoint1.flyTime = 60;
        cruiserWaypoint1.flyDistance = 200;
        cruiserWaypoint1.headingMode = WaypointHeadingMode.FORWARD_TO_NEXT_POINT;
        //普通航点动作只有WaypointType.HOVER和 WaypointType.STANDARD
        WaypointType waypointType1 = WaypointType.HOVER;
        cruiserWaypoint1.waypointType = waypointType1; //悬停
        if(waypointType1 == WaypointType.HOVER) {
            cruiserWaypoint1.hoverTime = 60; //航点动作是悬停，则添加悬停时间
        }
        //航点1为悬停航点，可添加0个或多个相机动作
        List<WaypointAction> list = new ArrayList<>();
        //相机动作1 拍照
        WaypointAction action1 = new WaypointAction();
        action1.actionType = MissionActionType.TAKE_PHOTO;
        if(waypointType1 == WaypointType.STANDARD) {
            //参数1：云台pitch角度，参数2：云台yaw角度
            action1.parameters = new int[]{-20, 30, 0, 0, 0, 0, 0};
        }else {
            //hover不能添加单拍照动作，只能定时拍
            action1.parameters = new int[]{};
        }
        list.add(action1);
        //相机动作2 定实拍
        WaypointAction action2 = new WaypointAction();
        action2.actionType = MissionActionType.START_TIME_LAPSE_SHOOT;
        if(waypointType1 == WaypointType.STANDARD) {
            //参数1：云台pitch角度，参数2：云台yaw角度，参数3：定实拍间隔
            action2.parameters = new int[]{-20, 30, 2, 0, 0, 0, 0};
        }else {
            //参数1：云台pitch角度，参数2：云台yaw角度，参数3：定实拍间隔，参数4：定实拍持续时间
            action2.parameters = new int[]{-20, 30, 2, 10, 0, 0, 0};
        }
        list.add(action2);
        //相机动作3 定距拍
        WaypointAction action3 = new WaypointAction();
        action3.actionType = MissionActionType.START_DISTANCE_SHOOT;
        if(waypointType1 == WaypointType.STANDARD) {
            //参数1：云台pitch角度，参数2：云台yaw角度，参数5：定距拍间隔（单位m）
            action3.parameters = new int[]{-20, 30, 0, 0, 100, 0, 0};
        }else {
            //hover不能添加定距拍动作
            action3.parameters = new int[]{};
        }
        list.add(action3);
        //相机动作4 开始录像
        WaypointAction action4 = new WaypointAction();
        action4.actionType = MissionActionType.START_RECORD;
        if(waypointType1 == WaypointType.STANDARD) {
            action4.parameters = new int[]{-20, 30, 0, 0, 0, 0, 0};
        }else {
            //参数1：云台pitch角度，参数2：云台yaw角度，参数6：录像持续时间
            action4.parameters = new int[]{-20, 30, 0, 0, 0, 60, 0};
        }
        list.add(action4);
        //相机动作5 结束录像
        WaypointAction action5 = new WaypointAction();
        action5.actionType = MissionActionType.STOP_RECORD;
        if(waypointType1 == WaypointType.STANDARD) {
            action5.parameters = new int[]{-20, 30, 0, 0, 0, 0, 0};
        }else {
            action5.parameters = new int[]{-20, 30, 0, 0, 0, 0, 0};
        }
        list.add(action5);
        cruiserWaypoint1.actions = list;
        wpList.add(cruiserWaypoint1);

        //航点2（动作：飞跃）
        Evo2Waypoint cruiserWaypoint2 = new Evo2Waypoint(new AutelCoordinate3D(23.1244, 123.12445, 60));
        cruiserWaypoint2.wSpeed = 2; //单位m/s
        cruiserWaypoint2.poiIndex = 1; //关联的兴趣点id
        cruiserWaypoint2.flyTime = 60;
        cruiserWaypoint2.flyDistance = 200;
        cruiserWaypoint2.headingMode = WaypointHeadingMode.FORWARD_TO_NEXT_POINT;
        //普通航点动作只有WaypointType.HOVER和 WaypointType.STANDARD
        cruiserWaypoint2.waypointType = WaypointType.STANDARD; //飞跃
        //航点1为飞跃航点，可添加0个或1个相机动作
        List<WaypointAction> list2 = new ArrayList<>();
        //添加相机动作1 拍照
        WaypointAction point2Action1 = new WaypointAction();
        point2Action1.actionType = MissionActionType.TAKE_PHOTO;
        point2Action1.parameters = new int[]{-20, 30, 0, 0, 0, 0, 0};
        list2.add(point2Action1);
        cruiserWaypoint2.actions = list2;
        wpList.add(cruiserWaypoint2);

        //航点3（动作：飞跃）
        Evo2Waypoint cruiserWaypoint3 = new Evo2Waypoint(new AutelCoordinate3D(23.1244, 123.12445, 60));
        cruiserWaypoint3.wSpeed = 2; //单位m/s
        cruiserWaypoint3.poiIndex = 1; //关联的兴趣点id
        cruiserWaypoint3.flyTime = 60;
        cruiserWaypoint3.flyDistance = 200;
        cruiserWaypoint3.headingMode = WaypointHeadingMode.FORWARD_TO_NEXT_POINT;
        //普通航点动作只有WaypointType.HOVER和 WaypointType.STANDARD
        cruiserWaypoint3.waypointType = WaypointType.STANDARD; //飞跃
        //航点1为飞跃航点，可添加0个或1个相机动作
        List<WaypointAction> list3 = new ArrayList<>();
        //添加相机动作1 开始录像
        WaypointAction point3Action1 = new WaypointAction();
        point3Action1.actionType = MissionActionType.START_RECORD;
        point3Action1.parameters = new int[]{-20, 30, 0, 0, 0, 0, 0};
        list3.add(point3Action1);
        cruiserWaypoint3.actions = list3;
        wpList.add(cruiserWaypoint3);

        //航点4（动作：飞跃）
        Evo2Waypoint cruiserWaypoint4 = new Evo2Waypoint(new AutelCoordinate3D(23.1244, 123.12445, 60));
        cruiserWaypoint4.wSpeed = 2; //单位m/s
        cruiserWaypoint4.poiIndex = 1; //关联的兴趣点id
        cruiserWaypoint4.flyTime = 60;
        cruiserWaypoint4.flyDistance = 200;
        cruiserWaypoint4.headingMode = WaypointHeadingMode.FORWARD_TO_NEXT_POINT;
        //普通航点动作只有WaypointType.HOVER和 WaypointType.STANDARD
        cruiserWaypoint4.waypointType = WaypointType.STANDARD; //飞跃
        //航点1为飞跃航点，可添加0个或1个相机动作
        List<WaypointAction> list4 = new ArrayList<>();
        //添加相机动作1 停止录像
        WaypointAction point4Action1 = new WaypointAction();
        point4Action1.actionType = MissionActionType.STOP_RECORD;
        point4Action1.parameters = new int[]{-20, 30, 0, 0, 0, 0, 0};
        list4.add(point4Action1);
        cruiserWaypoint4.actions = list4;
        wpList.add(cruiserWaypoint4);

        //航点5（动作：飞跃）
        Evo2Waypoint cruiserWaypoint5 = new Evo2Waypoint(new AutelCoordinate3D(23.1244, 123.12445, 60));
        cruiserWaypoint5.wSpeed = 2; //单位m/s
        cruiserWaypoint5.poiIndex = 1; //关联的兴趣点id
        cruiserWaypoint5.flyTime = 60;
        cruiserWaypoint5.flyDistance = 200;
        cruiserWaypoint5.headingMode = WaypointHeadingMode.FORWARD_TO_NEXT_POINT;
        //普通航点动作只有WaypointType.HOVER和 WaypointType.STANDARD
        cruiserWaypoint5.waypointType = WaypointType.STANDARD; //飞跃
        //航点1为飞跃航点，可添加0个或1个相机动作
        List<WaypointAction> list5 = new ArrayList<>();
        //不添加相机动作
        WaypointAction point5Action1 = new WaypointAction();
        point5Action1.actionType = MissionActionType.INVALID;
        point5Action1.parameters = new int[]{-20, 30, 0, 0, 0, 0, 0};
        list5.add(point5Action1);
        cruiserWaypoint5.actions = list5;
        wpList.add(cruiserWaypoint5);

        //添加兴趣点（可添加0个或多个兴趣点）
        List<Poi> poiList = new ArrayList<>();
        Poi cruiserPoi1 = new Poi();
        cruiserPoi1.id = 1;
        cruiserPoi1.coordinate3D = new AutelCoordinate3D(23.1344, 123.13445, 60);
        poiList.add(cruiserPoi1);

        Poi cruiserPoi2 = new Poi();
        cruiserPoi2.id = 1;
        cruiserPoi2.coordinate3D = new AutelCoordinate3D(23.1354, 123.13545, 60);
        poiList.add(cruiserPoi2);

        autelMission.wpoiList = poiList;
        autelMission.wpList = wpList;
        autelMission.finishedAction = Evo2WaypointFinishedAction.RETURN_TO_FIRST_POINT;
    }

    @Override
    public void onClick(View view) {
        final int id = view.getId();
        switch (id){
            case R.id.prepare:{
                if(null != missionManager){
                    missionManager.prepareMission(autelMission, new CallbackWithOneParamProgress<Boolean>() {
                        @Override
                        public void onProgress(float v) {

                        }

                        @Override
                        public void onSuccess(Boolean aBoolean) {
                            Toast.makeText(Evo2WayPointActivity.this, "prepare success", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onFailure(AutelError autelError) {

                        }
                    });
                }
            }
            break;

            case R.id.start:{
                if(null != missionManager) {
                    missionManager.startMission(new CallbackWithNoParam() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(Evo2WayPointActivity.this, "start success", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onFailure(AutelError autelError) {

                        }
                    });
                }
            }
            break;

            case R.id.pause:{
                if(null != missionManager) {
                    missionManager.pauseMission(new CallbackWithNoParam() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(Evo2WayPointActivity.this, "pause success", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onFailure(AutelError autelError) {

                        }
                    });
                }
            }
            break;

            case R.id.cancel:{
                if(null != missionManager) {
                    missionManager.cancelMission(new CallbackWithNoParam() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(Evo2WayPointActivity.this, "cancel success", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onFailure(AutelError autelError) {

                        }
                    });
                }
            }
            break;

            case R.id.download:{
                if(null != missionManager) {
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

                        }
                    });
                }
            }
            break;
        }
    }

    private void initListener(){
        if(null == missionManager) return;
        missionManager.setRealTimeInfoListener(new CallbackWithOneParam<RealTimeInfo>() {
            @Override
            public void onSuccess(RealTimeInfo realTimeInfo) {
                Evo2WaypointRealTimeInfoImpl info = (Evo2WaypointRealTimeInfoImpl) realTimeInfo;
                AutelLog.d("MissionRunning", "timeStamp:" + info.timeStamp + ",speed:" + info.speed + ",isArrived:" + info.isArrived +
                        ",isDirecting:"+ info.isDirecting + ",waypointSequence:" + info.waypointSequence + ",actionSequence:" + info.actionSequence +
                        ",photoCount:" + info.photoCount + ",MissionExecuteState:" + info.executeState+ ",remainFlyTime:" + info.remainFlyTime);
            }

            @Override
            public void onFailure(AutelError autelError) {

            }
        });
    }
}
