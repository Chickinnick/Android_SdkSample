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
                double missionType = 1;//任务类型，1-航点任务，6-矩形/多边形任务
                //长度/高度单位均为米

                //长度为3，飞机的纬度、经度、起飞高度
                double[] droneLocation = new double[]{22.59638835580453, 113.99613850526757, 40.0};
                //长度为3，返航点的纬度、经度、返航高度
                double[] homeLocation = new double[]{22.59638835580453, 113.99613850526757, 50.0};
                //长度为4，上升盘旋点的纬度、经度、高度、盘旋半径
                double[] launchLocation = new double[]{22.59638835580453, 113.99318883642341, 100.0, 120.0};
                //长度为4，下降盘旋点的纬度、经度、高度、盘旋半径
                double[] landingLocation = new double[]{22.59291695879857, 113.99787910849454, 100.0, 120.0};
                //长度为8（两个点），如果没有可以全设为0，只用于矩形和多边形，矩形/多边形与上升下降盘旋点之间的点的纬度、经度、高度、是否使用该航点(0-使用，1-不使用)
                double[] avoidPosition = new double[]{22.598295333564423, 113.99354868480384, 100.0, 1.0,
                        22.598772827314363, 113.99867325644607, 100.0, 1.0};

                char waypointLen = 2;//航点的个数/矩形多边形是顶点的个数
                int poiPointLen = 2;//观察点的个数

                //以下参数针对矩形、多边形任务,航点任务时全置为 0 就可以了
                double UAVTurnRad = 120;//飞机转弯半径，默认 120 米
                double UAVFlyVel = 17;//飞行速度(单位m/s)
                double UserFPKIsDef = 1;//是否用户自定义主航线角度，0-自动，1-手动
                double UserFlyPathA = 0;//用户自定义主航线角度，UserFPKIsDef为1时生效
                double WidthSid =140.56;//旁向扫描宽度
                double OverlapSid = 0.7;//旁向重叠率（0-1）
                double WidthHead = 78.984;//航向扫描宽度
                double OverlapHead = 0.8;//航向重叠率（0-1）
                double UAVFlyAlt = 100;//飞行高度

                /*
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
                    变量12：相机动作参数，定时和定距的参数
                    变量13：相机动作参数，云台俯仰角（-120 -- 0）
                    变量14-15：未定义
                */
                //航点任务
                double[] waypointParamList = new double[]{1.0, 0.0, 22.597737289727164, 113.9974874391902, 100.0, 17.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -90.0, 0.0, 0.0,
                        2.0, 0.0, 22.59897542587946, 114.00336684129968, 100.0, 17.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -90.0, 0.0, 0.0};
                //矩形任务
//                double[] waypointParamList = new double[]{1.0, 0.0, 22.59808119092429, 113.9951432761672, 100.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -90.0, 0.0, 0.0,
//                        2.0, 0.0, 22.59808119092429, 113.9971040869537, 100.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -90.0, 0.0, 0.0,
//                        3.0, 0.0, 22.596611380444926, 113.9971040869537, 100.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -90.0, 0.0, 0.0,
//                        4.0, 0.0, 22.596611380444926, 113.9951432761672, 100.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -90.0, 0.0, 0.0};

                /*
                    航点定义根据接口协议有17个变量，分别为：
                    变量 0：纬度
                    变量 1：经度
                    变量 2：高度
                    变量 3：半径
                    变量 4：IP_Type，默认 11
                    变量 5：关联航点个数
                */
                double[] poiParamList = new double[]{22.601550713371807, 113.99913365283817, 0.0, 120.0, 11.0, 1.0,
                        22.600490797193245, 113.99435713952568, 20.0, 120.0, 11.0, 0.0};

                //关联航点序号列表，每个观察点最多关联五个航点，数组个数为观察点个数*5
                int[] linkPoints = new int[]{2, 0, 0, 0, 0, 0, 0, 0, 0, 0};

                //是否使用地形跟随
                boolean isEnableTopographyFollow = true;

                //返回0表示成功，返回非0表示失败
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
                double[] drone = new double[]{22.123112, 113.232123, 1000};
                double[] homePoint = new double[]{22.123112, 113.232123, 1000};
                double[] upHomePoint = new double[]{22.2342412, 113.3232123, 1000};
                double[] downHomePoint = new double[]{22.125112, 113.232523, 1000};
                double[] waypointParams = new double[]{22.123112, 113.232123, 1000, 22.123312, 113.232423, 1000};
                PathPlanningResult waypointMissionPath = NativeHelper.getWaypointMissionPath(drone, homePoint, upHomePoint, downHomePoint, waypointParams);
                AutelLog.debug_i("NativeHelper:", "flyTime = " + waypointMissionPath.getFlyTime()
                        + ", flyLength = " + waypointMissionPath.getFlyLength() + ", picNum = " + waypointMissionPath.getPictNum()
                        + ",errorCode = " + waypointMissionPath.getErrorCode() + ", listSize = " + waypointMissionPath.getLatLngList().size());

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
