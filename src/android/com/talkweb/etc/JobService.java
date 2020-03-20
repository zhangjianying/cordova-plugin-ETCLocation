package com.talkweb.etc;

import android.annotation.SuppressLint;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.util.concurrent.TimeUnit;

/**
 * 周期触发任务
 */
@SuppressLint("NewApi")
public class JobService  extends android.app.job.JobService {
    private final static String TAG = JobService.class.getSimpleName();

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "JobService onStartJob()");
        doing();
        doService();
        return false;
    }

    //不能做长时间任务
    private void doing(){
        Log.d(TAG, "JobService doing()");

        new HeartbeatTask("reportGPS",this.getApplicationContext()).run();
    }

    // 自己调用自己
    // 有些厂商禁用 周期任务
    private void doService() {
        JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        JobInfo.Builder builder = new JobInfo.Builder(1, new ComponentName(this, JobService.class));  //指定哪个JobService执行操作
        builder.setMinimumLatency(TimeUnit.MINUTES.toMillis(3)); //执行的最小延迟时间
        builder.setOverrideDeadline(TimeUnit.MINUTES.toMillis(5));  //执行的最长延时时间
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE);  //非漫游网络状态
        builder.setBackoffCriteria(TimeUnit.MINUTES.toMillis(5), JobInfo.BACKOFF_POLICY_LINEAR);  //线性重试方案
        builder.setRequiresCharging(false); // 未充电状态
        jobScheduler.schedule(builder.build());
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "JobService onStopJob()");
        return false;
    }
}
