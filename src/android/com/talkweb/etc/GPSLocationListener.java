package com.talkweb.etc;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GPSLocationListener extends BDAbstractLocationListener {
    private static final String TAG = "GPSLocationListener";
    private Context mContext;
    public static final String GPS_SAVE_DIR ="GPSS";

    private String saveGPSFilePath =null; // 保存GPS文件的路径

    public GPSLocationListener(Context context){
        this.mContext = context;

        this.saveGPSFilePath = Utils.getFilePath(this.mContext,GPS_SAVE_DIR);
    }

    /**
     * 获取设备信息
     * @return
     */
    public JSONObject getDeviceInfoJsonObject() throws JSONException {
        JSONObject retVal = new JSONObject();
        //当前设备的指纹信息
        String fingerprint = Utils.getFingerprint(this.mContext);
        Log.i(TAG,"当前指纹信息:"+fingerprint);
        if(!TextUtils.isEmpty(fingerprint)){
            retVal.put("deviceId",fingerprint);
        }
        //补充设备信息
        retVal.put("deviceModel",Utils.getPhoneModel());
        retVal.put("OS",Utils.getOS());
        retVal.put("versionName",Utils.getVersionName(this.mContext));
        retVal.put("versionCode",Utils.getVersionCode(this.mContext));
        return retVal;
    }

    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
        Log.i(TAG,"--------------GPSLocationListener onReceiveLocation()----------------");
        double latitude = bdLocation.getLatitude();    //获取纬度信息
        double longitude = bdLocation.getLongitude();    //获取经度信息
        float radius = bdLocation.getRadius();    //获取定位精度，默认值为0.0f

        String coorType = bdLocation.getCoorType();
        //获取经纬度坐标类型，以LocationClientOption中设置过的坐标类型为准

        String time = bdLocation.getTime();
        Log.i(TAG,String.format("time:%s  latitude:%s  longitude:%s  radius:%s  coorType:%s",time,latitude,longitude,radius,coorType));

        //获取定位信息后.写文件由另外的线程负责统一上传
        //这里必须将坐标与扩展信息一起保存
        JSONObject gpsJson = null;
        try {
            //读取扩展信息
            String extendData = Utils.readExtendData(this.mContext );
            Log.d(TAG,"读取出来的扩展信息:" + extendData);


            gpsJson = getGPSJsonObject(bdLocation);
            Log.d(TAG,"上传GPS对象信息:" + gpsJson.toString());
            Log.d(TAG,"this.saveGPSFilePath = "+this.saveGPSFilePath);

            //设备基本信息
            JSONObject deviceInfoJsonObject = getDeviceInfoJsonObject();
            Log.d(TAG,"设备基本信息:" + deviceInfoJsonObject.toString());



            //将所有json 合并后保存
            JSONObject writeObj = new JSONObject();
            if(!TextUtils.isEmpty(extendData)){
                JSONObject  extendJsonObject = new JSONObject(extendData);
                writeObj = Utils.combineJson(writeObj,extendJsonObject);
            }

            writeObj = Utils.combineJson(writeObj,gpsJson);
            writeObj = Utils.combineJson(writeObj,deviceInfoJsonObject);

            Utils.saveFile(this.saveGPSFilePath,writeObj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG,e.getMessage());
        }

    }


    private static JSONObject getGPSJsonObject(BDLocation bdLocation) throws JSONException {
        JSONObject gpsJsonObj = new JSONObject();
        gpsJsonObj.put("latitude",bdLocation.getLatitude());
        gpsJsonObj.put("longitude",bdLocation.getLongitude());
        gpsJsonObj.put("radius",bdLocation.getRadius());
        gpsJsonObj.put("GPSTime",bdLocation.getTime());
        gpsJsonObj.put("coorType",bdLocation.getCoorType());

        SimpleDateFormat myFmt=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        gpsJsonObj.put("createTime",myFmt.format(new Date()));
        return gpsJsonObj;
    }
}
