package com.talkweb.etc;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.util.concurrent.TimeUnit;


/**
 * 5.x 以上使用 JobService 实现守护进程，这个守护进程要做的工作很简单，就是
 * 检查服务是否存活. 不存活的时候拉起来
 */
@SuppressLint("NewApi")
public class VMDaemonJobService extends JobService {

    private final static String TAG = VMDaemonJobService.class.getSimpleName();

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "onStartJob");
        doing();
        doService();
        return false;
    }

    //不能做长时间任务
    private void doing(){
        Log.d(TAG, "onStartJob");
        // 判断服务是否已经在运行
        if(!Utils.isRunService(this,"com.talkweb.etc.GPSService")){
            //没有运行 就尝试拉起来
            Log.d(TAG, "尝试重新拉起 com.talkweb.etc.GPSService ");
            Intent intent = new Intent(this,GPSService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
        }else{
            Log.d(TAG, "com.talkweb.etc.GPSService 还存活");
        }

    }

    // 自己调用自己
    // 有些厂商禁用 周期任务
    private void doService() {
        JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        JobInfo.Builder builder = new JobInfo.Builder(1, new ComponentName(this, VMDaemonJobService.class));  //指定哪个JobService执行操作
        builder.setMinimumLatency(TimeUnit.MINUTES.toMillis(5)); //执行的最小延迟时间
        builder.setOverrideDeadline(TimeUnit.MINUTES.toMillis(15));  //执行的最长延时时间
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);  //非漫游网络状态
        builder.setBackoffCriteria(TimeUnit.MINUTES.toMillis(10), JobInfo.BACKOFF_POLICY_LINEAR);  //线性重试方案
        builder.setRequiresCharging(false); // 未充电状态
        jobScheduler.schedule(builder.build());
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "onStopJob");
        return false;
    }
}