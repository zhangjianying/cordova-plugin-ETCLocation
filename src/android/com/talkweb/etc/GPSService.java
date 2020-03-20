package com.talkweb.etc;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.talkweb.tollmanage.R;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GPSService extends Service {
    private static final String TAG = "GPSService";

    // 守护进程 Service ID
    private final static int DAEMON_SERVICE_ID = -5121;

    /** 百度定位客户端 */
    private LocationClient mLocationClient =null;
    /** 百度监听回调 **/
    private GPSLocationListener mGPSLocationListener = null;

    private BootCompletedReceiver bootCompletedReceiver=null;


    // 周期任务调度
    static ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {
        Log.i(TAG,"--------------GPSService onCreate()----------------");
        //注册前台服务
        initFrontService();
        //注册广播
        initRegisterReceiver();

        //触发周期任务
        initTask();
    }

    /**
     * 周期性任务 注册
     */
    private void initTask(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startJobScheduler();
        }else{
            executor.scheduleWithFixedDelay(new HeartbeatTask("reportGPS",this.getApplicationContext()), 2, 5, TimeUnit.SECONDS);
        }

    }

    /**
     * 5.x以上系统启用 JobScheduler API 进行实现守护进程的唤醒操作
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void startJobScheduler() {
        int jobId = 1;
        JobInfo.Builder jobInfo = new JobInfo.Builder(jobId, new ComponentName(this, JobService.class));
        jobInfo.setMinimumLatency(TimeUnit.MINUTES.toMillis(3)); //执行的最小延迟时间
        jobInfo.setOverrideDeadline(TimeUnit.MINUTES.toMillis(5));  //执行的最长延时时间
        jobInfo.setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE);  //非漫游网络状态
//        jobInfo.setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE)
        jobInfo.setBackoffCriteria(TimeUnit.MINUTES.toMillis(5), JobInfo.BACKOFF_POLICY_LINEAR);  //线性重试方案
        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(jobInfo.build());
    }

    /**
     * 注册动态广播
     */
    private void initRegisterReceiver(){
        //注册动态广播
        //有些广播只能动态注册,且只能在service里面注册. 这就是为什么要这个service
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(Intent.ACTION_TIME_TICK); //每分钟变化的action
        mFilter.addAction(Intent.ACTION_TIME_CHANGED); //设置了系统时间的action
        mFilter.addAction(Intent.ACTION_SCREEN_ON); // 亮屏幕
        mFilter.addAction(Intent.ACTION_SCREEN_OFF); // 关屏幕
        mFilter.addAction(Intent.ACTION_SCREEN_OFF); // 亮屏幕
        mFilter.addAction(Intent.ACTION_BATTERY_CHANGED); // 电量变化
        mFilter.addAction(Intent.ACTION_CONFIGURATION_CHANGED); //
        bootCompletedReceiver = new BootCompletedReceiver();
        registerReceiver(bootCompletedReceiver, mFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG,"--------------GPSService onStartCommand()----------------");

        int delayTime = 5; // 默认值 15分钟
        if (intent != null) {
            delayTime = intent.getIntExtra("delayTime", delayTime);
        }
        // 初始化定位服务，配置相应参数
        Log.i(TAG,"GPSService 刷新时间间隔: "+delayTime);
        initLocationService(delayTime);

        mLocationClient.start();
        return Service.START_STICKY;
    }


    /**
     * 初始化定位服务，配置相应参数
     */
    private void initLocationService(int delayTime) {
        //声明LocationClient类
        mLocationClient = new LocationClient(this.getApplicationContext());
        mGPSLocationListener = new GPSLocationListener(this.getApplicationContext());
        mLocationClient.registerLocationListener(mGPSLocationListener);

        LocationClientOption option = new LocationClientOption();
        // 可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        // 可选，默认gcj02，设置返回的定位结果坐标系
        option.setCoorType("gcj02"); //bd09ll
        // 可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        // option.setScanSpan(0);
        // 可选，设置是否需要地址信息，默认不需要
        option.setIsNeedAddress(false);
        // 可选，默认false,设置是否使用gps
        option.setOpenGps(true);
        // 可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setLocationNotify(false);
        option.disableCache(true);
        //可选，默认0，即仅定位一次，设置发起连续定位请求的间隔需要大于等于5000ms才是有效的
        option.setScanSpan(delayTime*1000*60);
        option.setIgnoreKillProcess(false);
        option.SetIgnoreCacheException(true);
        mLocationClient.setLocOption(option);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG,"---------------GPSService onDestroy()----------------");
        if (mLocationClient != null && mLocationClient.isStarted()) {
            mLocationClient.stop();
            if (mGPSLocationListener != null) {
                mLocationClient.unRegisterLocationListener(mGPSLocationListener);
            }
        }

        if(bootCompletedReceiver!=null){
            unregisterReceiver(bootCompletedReceiver);
        }

        stopSelf();

    }

    /**
     * 设置前台服务
     */
    private void initFrontService(){
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        Notification builder = new Notification.Builder(this).setTicker("").setSmallIcon(R.drawable.screen).build();
        Notification builder = new Notification.Builder(this).setAutoCancel(true).setContentTitle("收费运营")
                .setContentText("正在运行!!").setSmallIcon(R.drawable.screen)
                .setWhen(System.currentTimeMillis()).setOngoing(true).build();

        // 利用 Android 漏洞提高进程优先级，
        startForeground(DAEMON_SERVICE_ID, builder);
        // 当 SDk 版本大于18时，需要通过内部 Service 类启动同样 id 的 Service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Intent innerIntent = new Intent(this, DaemonInnerService.class);
            startService(innerIntent);
        }
    }


    /**
     * 实现一个内部的 Service，实现让后台服务的优先级提高到前台服务，这里利用了 android 系统的漏洞，
     * 不保证所有系统可用，测试在7.1.1 之前大部分系统都是可以的，不排除个别厂商优化限制
     */
    public static class DaemonInnerService extends Service {

        @Override public void onCreate() {
            super.onCreate();
        }

        @Override public int onStartCommand(Intent intent, int flags, int startId) {
            startForeground(DAEMON_SERVICE_ID, new Notification());
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }

        @Override public IBinder onBind(Intent intent) {
            throw new UnsupportedOperationException("onBind 未实现");
        }

        @Override public void onDestroy() {
            super.onDestroy();

            stopSelf();
        }
    }

}
