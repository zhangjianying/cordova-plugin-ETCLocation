package com.talkweb.etc;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.LOG;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.time.format.TextStyle;
import java.util.concurrent.TimeUnit;


/**
 * ETC 高速项目 后台服务启动插件
 */
public class ETCLocationPlugin extends CordovaPlugin {
	private static final String TAG = "ETCLocationPlugin";

	//读写权限验证
	private static String[] PERMISSIONS = {
			Manifest.permission.READ_EXTERNAL_STORAGE,
			Manifest.permission.WRITE_EXTERNAL_STORAGE,
			Manifest.permission.ACCESS_COARSE_LOCATION,
			Manifest.permission.ACCESS_FINE_LOCATION,
			Manifest.permission.ACCESS_WIFI_STATE,
			Manifest.permission.ACCESS_NETWORK_STATE,
			Manifest.permission.READ_PHONE_STATE
	};
	//请求状态码
	private static int REQUEST_PERMISSION_CODE = 46481;
	private CallbackContext callback;
	private String action;
	private JSONArray dataObj;

	@Override
	public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
		super.onRequestPermissionResult(requestCode, permissions, grantResults);
		if (requestCode == REQUEST_PERMISSION_CODE) {
			Log.i(TAG, "权限获取回调");
			if (this.cordova.hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
				&& this.cordova.hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
					&& this.cordova.hasPermission(Manifest.permission.READ_PHONE_STATE)	) {
				Log.i(TAG, "获取权限成功");
				withAction();
			} else {
				Log.i(TAG, "获取权限失败");
				callBackError(-10, "用户未给予权限");
			}
		}

	}

	/**
	 * 插件主入口
	 */
	@Override
	public boolean execute(String action, final JSONArray args, CallbackContext callbackContext) throws JSONException {
		Log.d(TAG, "execute method starting");

		this.callback = callbackContext;
		this.action = action;
		this.dataObj = args;

		//循环申请字符串数组里面所有的权限
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
			if (!this.cordova.hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
			||!this.cordova.hasPermission(Manifest.permission.READ_PHONE_STATE)) {
				//去获取权限
				this.cordova.requestPermissions(this, REQUEST_PERMISSION_CODE, PERMISSIONS);
				return true;
			}
			Log.i(TAG, "有权限");
		}

		//根据不同的action做不同的处理
		return withAction();
	}

	/**
	 * 根据不同的action做不同的处理
	 *
	 * @return
	 * @throws JSONException
	 */
	private boolean withAction() throws JSONException {

		//启动后台GPS保存 service
		if ("startBackgroundLocation".equalsIgnoreCase(action)) {
			Log.i(TAG, "startBackgroundLocation()");
			JSONObject params = this.dataObj.getJSONObject(0);
//			final int delayTime = params.getInt("delayTime"); // GPS获取间隔
//			final String serverURL = params.getString("serverURL"); // 服务器提交接口地址 http://192.168.146.166:8888/kite/
			String extendParams = params.getString("extendParams");// 扩展参数 json格式

//			LOG.d(TAG, String.format("delayTime=%s  serverURL=%s   extendParams=%s", delayTime, serverURL,extendParams));
			this.cordova.getThreadPool().execute(new Runnable() {
				@Override
				public void run() {
					startBackgroundLocation();
				}
			});

			return true;
		}

		// 保存扩展信息
		if ("saveExtendData".equalsIgnoreCase(action)) {
			final CordovaInterface cordovaInterface = this.cordova;
			final JSONObject params = this.dataObj.getJSONObject(0);
			this.cordova.getThreadPool().execute(new Runnable() {
				@Override
				public void run() {
					Log.i(TAG, "保存的扩展信息:"+params);
					Utils.saveExtendData(cordovaInterface.getActivity(),params);
				}
			});
			return true;
		}

		//删除保存的扩展信息
		if ("deleteExtendData".equalsIgnoreCase(action)) {
			final CordovaInterface cordovaInterface = this.cordova;
			this.cordova.getThreadPool().execute(new Runnable() {
				@Override
				public void run() {
					Log.i(TAG, "删除扩展信息:");
					Utils.deleteExtendData(cordovaInterface.getActivity());
				}
			});
			return true;
		}

		//删除保存的扩展信息
		if ("getDeviceId".equalsIgnoreCase(action)) {
			final CordovaInterface cordovaInterface = this.cordova;
			final CallbackContext _callBack = this.callback;
			this.callback.success(Utils.getFingerprint(cordovaInterface.getActivity()));
			return true;
		}

		//保存离线文件
		if("saveOffLineData".equalsIgnoreCase(action)){
			final CordovaInterface cordovaInterface = this.cordova;
			final CallbackContext _callBack = this.callback;

			final JSONObject params = this.dataObj.getJSONObject(0);
			this.cordova.getThreadPool().execute(new Runnable() {
					@Override
					public void run() {
						try {
							String taskType = params.getString("taskType");
							String content = params.getString("content");
							String fileName = Utils.saveOffLineData(cordovaInterface.getActivity(), taskType, content);
							_callBack.success(fileName);
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
			});
			return true;
		}

		// 读取离线文件
		if("readOffLineData".equalsIgnoreCase(action)){
			final CordovaInterface cordovaInterface = this.cordova;
			final CallbackContext _callBack = this.callback;

			final JSONObject params = this.dataObj.getJSONObject(0);
			this.cordova.getThreadPool().execute(new Runnable() {
				@Override
				public void run() {
					try {
						String filePath = params.getString("filePath");
						String fileContent = Utils.readOffLineData(cordovaInterface.getActivity(), filePath);
						_callBack.success(fileContent);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			});
			return true;
		}

		//根据TaskType获取文件列表
        if("getFileListByTaskType".equalsIgnoreCase(action)){
            final CordovaInterface cordovaInterface = this.cordova;
            final CallbackContext _callBack = this.callback;
            final JSONObject params = this.dataObj.getJSONObject(0);

            this.cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        String taskType = params.getString("taskType");
                        File[] fileListByTaskType = Utils.getFileListByTaskType(cordovaInterface.getActivity(), taskType);

                        JSONArray retList = new JSONArray();
                        for(File file : fileListByTaskType){
                            retList.put(file.getPath());
                        }

                        _callBack.success(retList);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            return true;
        }


		//复制文件
		if("copyFile".equalsIgnoreCase(action)){
			final CordovaInterface cordovaInterface = this.cordova;
			final CallbackContext _callBack = this.callback;
			final JSONObject params = this.dataObj.getJSONObject(0);

			this.cordova.getThreadPool().execute(new Runnable() {
				@Override
				public void run() {
					try {
						String fromFilePath = params.getString("fromFilePath");
						String toFilePath = params.getString("toFilePath");

						if(TextUtils.isEmpty(toFilePath)){
							_callBack.error("toFilePath is null");
							return;
						}
						if(TextUtils.isEmpty(fromFilePath)){
							_callBack.error("fromFilePath is null");
							return;
						}

						if(!new File(fromFilePath).exists()){
							_callBack.error("fromFilePath not exists");
							return;
						}

						Utils.copyFile(fromFilePath,toFilePath);
						_callBack.success(Boolean.toString(new File(toFilePath).exists()));
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			});

			return true;
		}

		//获取保存root 路径值
		if("getSavePathByTaskType".equalsIgnoreCase(action)){
			final CordovaInterface cordovaInterface = this.cordova;
			final CallbackContext _callBack = this.callback;
			final JSONObject params = this.dataObj.getJSONObject(0);

			this.cordova.getThreadPool().execute(new Runnable() {
				@Override
				public void run() {
					try {
						String taskType = params.getString("taskType");
						String filePath = Utils.getFilePath(cordovaInterface.getActivity(), taskType);
						_callBack.success(filePath);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			});
			return true;
		}

		//删除文件  deleteFile
		if("deleteFile".equalsIgnoreCase(action)){
			final CordovaInterface cordovaInterface = this.cordova;
			final CallbackContext _callBack = this.callback;
			final JSONObject params = this.dataObj.getJSONObject(0);

			this.cordova.getThreadPool().execute(new Runnable() {
				@Override
				public void run() {
					try {
						String filePath = params.getString("filePath");

						if(TextUtils.isEmpty(filePath)){
							_callBack.error("filePath is null");
							return;
						}

						Utils.deleteFile(filePath);
						_callBack.success(Boolean.toString(Boolean.TRUE));
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			});
			return true;
		}

		return false;
	}

	public void startBackgroundLocation() {
		LOG.d(TAG,"准备启动服务");
		Intent intent = new Intent(this.cordova.getActivity(),GPSService.class);
		if(!Utils.isRunService(this.cordova.getActivity(),"com.talkweb.etc.GPSService")){
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
				this.cordova.getActivity().startForegroundService(intent);
			} else {
				this.cordova.getActivity().startService(intent);
			}
		}else{
			Log.d(TAG, "com.talkweb.etc.GPSService 还存活");
		}

		//开启 jobScheduler 守护
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			startJobScheduler();
		}

	}

	/**
	 * 5.x以上系统启用 JobScheduler API 进行实现守护进程的唤醒操作
	 */
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private void startJobScheduler() {
		int jobId = 1;
		JobInfo.Builder jobInfo = new JobInfo.Builder(jobId, new ComponentName(this.cordova.getActivity(), VMDaemonJobService.class));
		jobInfo.setMinimumLatency(TimeUnit.MINUTES.toMillis(5)); //执行的最小延迟时间
		jobInfo.setOverrideDeadline(TimeUnit.MINUTES.toMillis(15));  //执行的最长延时时间
		jobInfo.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);  //非漫游网络状态
		jobInfo.setBackoffCriteria(TimeUnit.MINUTES.toMillis(10), JobInfo.BACKOFF_POLICY_LINEAR);  //线性重试方案
		JobScheduler jobScheduler = (JobScheduler) this.cordova.getActivity().getSystemService(Context.JOB_SCHEDULER_SERVICE);
		jobScheduler.schedule(jobInfo.build());
	}

	private void callBackError(int code, String msg) {
		try {
			JSONObject retVal = new JSONObject();
			retVal.put("code", code);
			retVal.put("desc", msg);
			callback.error(retVal);
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

}
