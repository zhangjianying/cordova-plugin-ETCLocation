package com.talkweb.etc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class BootCompletedReceiver extends BroadcastReceiver {
    private final static String TAG = BootCompletedReceiver.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG,"收到广播:"+action);
        //这里只要收到广播就触发 检查服务是否存活的机制
        if(!Utils.isRunService(context,"com.talkweb.etc.GPSService")){
            //没有运行 就尝试拉起来
            Log.d(TAG, "尝试重新拉起 com.talkweb.etc.GPSService ");
            Intent intentService = new Intent(context,GPSService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intentService);
            } else {
                context.startService(intentService);
            }
        }else{
            Log.d(TAG, "com.talkweb.etc.GPSService 还存活");
        }
    }
}