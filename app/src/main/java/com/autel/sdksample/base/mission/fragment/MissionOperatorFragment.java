package com.autel.sdksample.base.mission.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.autel.common.CallbackWithNoParam;
import com.autel.common.CallbackWithOneParam;
import com.autel.common.CallbackWithOneParamProgress;
import com.autel.common.error.AutelError;
import com.autel.common.flycontroller.FlightErrorState;
import com.autel.common.mission.AutelMission;
import com.autel.common.mission.MissionExecuteState;
import com.autel.common.mission.RealTimeInfo;
import com.autel.common.mission.xstar.OrbitMission;
import com.autel.common.mission.xstar.Waypoint;
import com.autel.common.mission.xstar.WaypointMission;
import com.autel.lib.jniHelper.NativeHelper;
import com.autel.lib.jniHelper.PathPlanningResult;
import com.autel.sdk.mission.MissionManager;
import com.autel.sdk.product.BaseProduct;
import com.autel.sdksample.R;
import com.autel.sdksample.TestApplication;
import com.autel.sdksample.base.mission.MapActivity;
import com.autel.sdksample.base.util.FileUtils;
import com.autel.util.log.AutelLog;

import java.io.File;
import java.util.Arrays;
import java.util.List;


public class MissionOperatorFragment extends Fragment {
    Button missionPrepare;
    Button missionStart;
    Button missionPause;
    Button missionResume;
    Button missionCancel;
    Button missionDownload;
    Button writeMissionTestData;
    Button testWaypoint;
    Button testMapping;
    Button yawRestore;
    Button getCurrentMission;
    Button getMissionExecuteState;
    ProgressBar progressBarDownload;
    ProgressBar progressBarPrepare;

    MissionManager missionManager;
    private String filePath = FileUtils.getMissionFilePath() + "mission.aut";


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = createView(R.layout.fragment_mission_menu);
        return view;
    }

    protected MissionManager getMissionManager() {
        BaseProduct product = ((TestApplication) getActivity().getApplicationContext()).getCurrentProduct();
        if (null != product) {
            switch (product.getType()) {
//                case X_STAR:
//                    return ((XStarAircraft) product).getMissionManager();
//                case PREMIUM:
//                    return ((XStarPremiumAircraft) product).getMissionManager();
                case DRAGONFISH:
                    return product.getMissionManager();
            }

        }
        return null;
    }

    private String TAG = "Mission";

    protected View createView(@LayoutRes int resource) {
        View view = View.inflate(getContext(), resource, null);
        initUi(view);
        return view;
    }

    private void initUi(final View view) {
        if (getActivity() != null) {
            ((MapActivity) getActivity()).updateMissionInfo("Mission state : ");
            ((MapActivity) getActivity()).updateLogInfo("RealTimeInfo : ");
        }
        missionManager = getMissionManager();

        view.findViewById(R.id.setRealTimeInfoListener).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != missionManager) {
                    missionManager.setRealTimeInfoListener(new CallbackWithOneParam<RealTimeInfo>() {
                        @Override
                        public void onSuccess(RealTimeInfo realTimeInfo) {
                            if (getActivity() != null) {
                                ((MapActivity) getActivity()).updateLogInfo("RealTimeInfo : " + realTimeInfo);
                            }
                        }

                        @Override
                        public void onFailure(AutelError autelError) {
                            if (getActivity() != null)
                                ((MapActivity) getActivity()).updateMissionInfo("Mission state : " + autelError.getDescription());
                        }
                    });
                }
            }
        });

        view.findViewById(R.id.resetRealTimeInfoListener).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != missionManager) {
                    missionManager.setRealTimeInfoListener(null);
                }
            }
        });

        final Context applicationContext = getActivity().getApplicationContext();
        progressBarDownload = (ProgressBar) view.findViewById(R.id.progressBarDownload);
        progressBarPrepare = (ProgressBar) view.findViewById(R.id.progressBarPrepare);

        missionPrepare = (Button) view.findViewById(R.id.missionPrepare);
        missionPrepare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != missionManager) {
                    progressBarPrepare.setVisibility(View.VISIBLE);
                    missionManager.prepareMission(((MapActivity) getActivity()).createMission(), filePath, new CallbackWithOneParamProgress<Boolean>() {
                        @Override
                        public void onProgress(float v) {
                            AutelLog.d(TAG, " prepareMission onProgress " + v);
                        }

                        @Override
                        public void onSuccess(Boolean aBoolean) {
                            AutelLog.d(TAG, " prepareMission " + aBoolean);
                            toastView(R.string.mission_prepare_notify);
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    progressBarPrepare.setVisibility(View.GONE);
                                }
                            });

                        }

                        @Override
                        public void onFailure(AutelError autelError) {
                            AutelLog.d(TAG, " onFailure " + autelError.getDescription());
                            toastView(autelError);
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    progressBarPrepare.setVisibility(View.GONE);
                                }
                            });
                        }
                    });
                }
            }
        });

        missionStart = (Button) view.findViewById(R.id.missionStart);
        missionStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != missionManager) {
                    missionManager.startMission(new CallbackWithOneParam<Pair<Boolean, FlightErrorState>>() {
                        @Override
                        public void onSuccess(Pair<Boolean, FlightErrorState> booleanFlightErrorStatePair) {
                            toastView(R.string.mission_start_notify);
                        }

                        @Override
                        public void onFailure(AutelError autelError) {
                            toastView(autelError);
                        }
                    });
                }
            }
        });

        missionPause = (Button) view.findViewById(R.id.missionPause);
        missionPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != missionManager) {
                    missionManager.pauseMission(new CallbackWithNoParam() {
                        @Override
                        public void onSuccess() {
                            toastView(R.string.mission_pause_notify);
                        }

                        @Override
                        public void onFailure(AutelError autelError) {
                            toastView(autelError);
                        }
                    });
                }
            }
        });

        missionResume = (Button) view.findViewById(R.id.missionResume);
        missionResume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != missionManager) {
                    missionManager.resumeMission(new CallbackWithNoParam() {
                        @Override
                        public void onSuccess() {
                            toastView(R.string.mission_resume_notify);
                        }

                        @Override
                        public void onFailure(AutelError autelError) {
                            toastView(autelError);
                        }
                    });
                }
            }
        });

        missionCancel = (Button) view.findViewById(R.id.missionCancel);
        missionCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != missionManager) {
                    missionManager.cancelMission(new CallbackWithNoParam() {
                        @Override
                        public void onSuccess() {
                            toastView(R.string.mission_cancel_notify);
                        }

                        @Override
                        public void onFailure(AutelError autelError) {
                            toastView(autelError);
                        }
                    });
                }
            }
        });

        missionDownload = (Button) view.findViewById(R.id.missionDownload);
        writeMissionTestData = (Button) view.findViewById(R.id.writeMissionTestData);
        testWaypoint = (Button) view.findViewById(R.id.testWaypoint);
        testMapping = (Button) view.findViewById(R.id.testMapping);
        missionDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != missionManager) {
                    progressBarDownload.setVisibility(View.VISIBLE);
                    missionManager.downloadMission(new CallbackWithOneParamProgress<AutelMission>() {
                        @Override
                        public void onProgress(float v) {

                        }

                        @Override
                        public void onSuccess(AutelMission autelMission) {
                            toastView(R.string.mission_download_notify);
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    progressBarDownload.setVisibility(View.GONE);
                                }
                            });

                            if (autelMission instanceof WaypointMission) {

                                List<Waypoint> wpList = ((WaypointMission) autelMission).wpList;
                            } else if (autelMission instanceof OrbitMission) {

                            }
                            showDownloadMission(autelMission.toString());
                        }

                        @Override
                        public void onFailure(AutelError autelError) {
                            toastView(autelError);
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    progressBarDownload.setVisibility(View.GONE);
                                }
                            });
                        }
                    });
                }
            }
        });
        writeMissionTestData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                File myDir = new File(FileUtils.getMissionFilePath());
                if (!myDir.exists()) {
                    myDir.mkdirs();
                }
                double missionType = 1;
                double[] droneLocation = new double[]{};
                double[] homeLocation = new double[]{};//MissionSaveUtils.getHomeLocation(task);
                double[] launchLocation = new double[]{};//MissionSaveUtils.getLaunchLocation(task);
                double[] landingLocation = new double[]{};//MissionSaveUtils.getLandingLocation(task);
                double[] avoidPosition = new double[]{};//MissionSaveUtils.getAvoidPosition(task);
                //put waypoint head
                double UAVTurnRad = 0;
                double UAVFlyVel = 0;
                double UserFPKIsDef = 0;
                double UserFlyPathA = 0;
                double WidthSid = 0;
                double OverlapSid = 0;
                double WidthHead = 0;
                double OverlapHead = 0;
                double UAVFlyAlt = 0;
                char waypointLen = 0;
                int poiPointLen = 0;
                double[] waypointParamList = new double[]{};//MissionSaveUtils.getWaypointParamList(task);
                double[] poiParamList = new double[]{};//MissionSaveUtils.getPoiPointParamList(task);
                int[] linkPoints = new int[]{};//MissionSaveUtils.getPoiLinkPointList(task);
                boolean isEnableTopographyFollow = true;
                int res = NativeHelper.writeMissionFile(filePath, missionType,
                        droneLocation, homeLocation,
                        launchLocation, landingLocation,
                        avoidPosition, UAVTurnRad,
                        UAVFlyVel, UserFPKIsDef,
                        UserFlyPathA, WidthSid,
                        OverlapSid, WidthHead,
                        OverlapHead, UAVFlyAlt,
                        waypointLen, waypointParamList,
                        poiPointLen, poiParamList, linkPoints, isEnableTopographyFollow ? 1 : 0);
                AutelLog.d("NativeHelper", " writeMissionFile result -> " + res);
            }
        });

        testWaypoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //飞机当前位置
                double[] drone = new double[]{22.59651, 113.9972969, 0};//经纬高
                //返航点位置
                double[] homePoint = new double[]{22.59651, 113.9972969, 100.0};//经纬高
                //上升盘旋点
                double[] upHomePoint = new double[]{22.59651, 113.99434723115584, 100.0, 120.0};//经、纬、高、盘旋半径
                //下降盘旋点
                double[] downHomePoint = new double[]{22.59651, 114.00024656884415, 100, 120.0};//经、纬、高、盘旋半径


                /**
                 * waypointParams：航点参数每16个值为一组，以下是以两个航点为例子；
                 参数说明：航点定义根据接口协议有16个变量，分别为：
                 航点定义根据接口协议有16个变量，分别为：
                 变量 0：当前航点标识（目前等于航点在当前任务中的序号）
                 变量 1：当前航点类型，其中：0–普通航点/飞越;1-兴趣点Orbit;4–起飞航点;5–按时间盘旋航点;6-按圈数盘旋航点;7–降落航点
                 变量 2：航点坐标，纬度
                 变量 3：航点坐标，经度
                 变量 4：航点坐标，高度
                 变量 5：航点飞行速度，单位米/秒
                 变量 6：盘旋时间或盘旋圈数，只针对航点类型为盘旋有用
                 变量 7：盘旋半径，单位：米
                 变量 8：盘旋方向：0-顺时针;1-逆时针盘旋
                 变量 9：兴趣点起始角度 1-360度
                 变量10：兴趣点水平角度 1-360度
                 变量11：相机动作类型: 0-无，1-拍照，2-定时拍照，3-定距拍照，4-录像
                 变量12：相机动作参数，定时(单位s)和定距（单位m）的参数
                 变量13：相机动作参数，云台俯仰角（-120 -- 0）
                 变量14-15：未定义
                 */
                double[] waypointParams = new double[]{1.0, 0.0, 22.59794923247847, 113.9946704742452, 100.0, 17.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                        2.0, 0.0, 22.593907884795755, 113.99646218984662, 100.0, 17.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
                PathPlanningResult waypointMissionPath = NativeHelper.getWaypointMissionPath(drone, homePoint, upHomePoint, downHomePoint, waypointParams);
                AutelLog.debug_i("NativeHelper:", "flyTime = " + waypointMissionPath.getFlyTime()
                        + ", flyLength = " + waypointMissionPath.getFlyLength() + ", picNum = " + waypointMissionPath.getPictNum()
                        + ",errorCode = " + waypointMissionPath.getErrorCode() + ", listSize = " + waypointMissionPath.getLatLngList().size());

            }
        });
        testMapping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //飞机当前位置
                double[] drone = new double[]{22.59651, 113.9972969, 0};//经纬高
                //返航点位置
                double[] homePoint = new double[]{22.59651, 113.9972969, 100.0};//经纬高
                //上升盘旋点
                double[] upHomePoint = new double[]{22.59651, 113.99434723115584, 100.0, 120.0};//经、纬、高、盘旋半径
                //下降盘旋点
                double[] downHomePoint = new double[]{22.59651, 114.00024656884415, 100, 120.0};//经、纬、高、盘旋半径
                //途经点1 （上升盘旋点到任务之间添加）
                double[] startAvoid= new double[]{22.595300191562032, 113.98885025388489, 100, 1};//经、纬、高、是否有效（0-无效，1-有效）
                //途经点2
                double[] endAvoid = new double[]{22.592050563109837, 113.99623427307421, 100, 1};//经、纬、高、是否有效（0-无效，1-有效）

                double[] avoidPoints = Arrays.copyOf(startAvoid, startAvoid.length + endAvoid.length);
                //将b数组添加到已经含有a数组的c数组中去
                System.arraycopy(endAvoid, 0, avoidPoints, startAvoid.length, endAvoid.length);
                //矩形或多边形顶点坐标(经、纬、高)
                double[] vertexs =new double[]{22.598363332625695, 113.99316746476211,100};
                //航线高度
                float height = 100f;
                //航线速度
                float speed = 17.0f;
                //旁向重叠率
                double sideRate = 0.8f;
                //主航线重叠率
                float courseRate = 0.7f;
                //主航线角度 0:自动，1：用户自定义航向角度
                int userDefineAngle = 0 ;
                //当userDefineAngle为1时有效
                int courseAngle = 30;
                //飞机转弯半径，默认要设置120
                int turningRadius = 120;
                //旁向扫描宽度
                double sideScanWidth = 140.56235f;//2*height*tan(HFOV/2)需要自行计算得出
                //航向扫描宽度
                double courseScanWidth = 78.98377f;//2*height*tan(VFOV/2)


                PathPlanningResult result = NativeHelper.getMappingMissionPath(drone, homePoint, upHomePoint, downHomePoint,
                        vertexs, avoidPoints, height, speed, sideRate, courseRate
                        , userDefineAngle, courseAngle, turningRadius
                        , sideScanWidth, courseScanWidth);
                AutelLog.d(TAG," result "+result.getArea()+" "+result.getErrorCode());

            }
        });

        view.findViewById(R.id.yawRestore).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != missionManager) {
                    missionManager.yawRestore(new CallbackWithNoParam() {
                        @Override
                        public void onSuccess() {
                            toastView(R.string.mission_yaw_restore_notify);
                        }

                        @Override
                        public void onFailure(AutelError autelError) {
                            toastView(autelError);
                        }
                    });
                }
            }
        });

        view.findViewById(R.id.getCurrentMission).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != missionManager) {
                    AutelMission mission = missionManager.getCurrentMission();
                    ((MapActivity) getActivity()).updateLogInfo(null != mission ? mission.toString() : "null");
                }
            }
        });

        view.findViewById(R.id.getMissionExecuteState).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != missionManager) {
                    MissionExecuteState state = missionManager.getMissionExecuteState();
                    ((MapActivity) getActivity()).updateLogInfo(null != state ? state.toString() : "UNKNOWN");
                }
            }
        });
        final TextView layoutShowState = (TextView) view.findViewById(R.id.layoutShowState);
        layoutShowState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int distance = view.findViewById(R.id.operatorScroll).getWidth();
                Log.v("showhide", "x  : " + view.getX());
                boolean toShow = view.getX() < 0;
                if (toShow) {
                    view.setX(0);
                    layoutShowState.setText("HIDE");
                    ((MapActivity) getActivity()).setMissionContainerVisible(true);
                } else {
                    view.setX(-distance);
                    layoutShowState.setText("SHOW");
                    ((MapActivity) getActivity()).setMissionContainerVisible(false);
                }
            }
        });
    }

    private void showDownloadMission(String info) {
        ((MapActivity) getActivity()).updateLogInfo(info);
    }

    protected boolean isEmpty(String value) {
        return null == value || "".equals(value);
    }

    public void onDestroy() {
        super.onDestroy();
        if (null != missionManager) {
            missionManager.setRealTimeInfoListener(null);
        }
    }

    Handler mHandler = new Handler(Looper.getMainLooper());

    private void toastView(final AutelError autelError) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity().getApplicationContext(), autelError.getDescription(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void toastView(final int log) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity().getApplicationContext(), getString(log), Toast.LENGTH_LONG).show();
            }
        });
    }
}
