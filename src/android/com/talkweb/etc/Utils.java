package com.talkweb.etc;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * 工具方法
 */
public class Utils {
    private static final String TAG = "Utils";
    private static final String FILE_ENCODE="UTF-8";

    public static final String FILE_EXTEND_PATH="extendData";
    public static final String EXTENDDATA_FILE="extendData.m";


    /**
     * 获取版本code
     * @param ctx
     * @return
     */
    public static int getVersionCode(Context ctx) {
        int localVersion = 0;
        try {
            PackageInfo packageInfo = ctx.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(ctx.getPackageName(), 0);
            localVersion = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return localVersion;
    }
    /**
     * 获取当前版本号
     * @return
     * @throws Exception
     */
    public static  String getVersionName(Context ctx)
    {
        // 获取packagemanager的实例
        PackageManager packageManager = ctx.getPackageManager();
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo = null;
        try {
            packInfo = packageManager.getPackageInfo(ctx.getPackageName(),0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String version = packInfo.versionName;
        return version;
    }

    /**
     * 读取离线文件
     * @param context
     * @param filePath 完整路径
     * @return
     */
    public static String readOffLineData(Context context,String filePath){
        if(TextUtils.isEmpty(filePath)){
            return null;
        }

        String RetVal = null;
        File file = null;
        FileInputStream input=null;
        try{
            file = new File(filePath);

            if(!file.exists()){
                Log.w(TAG,"文件:"+filePath+"  不存在");
                return null;
            }

            input = new FileInputStream(file);
            byte[] buf = new byte[1024];
            int length = 0;
            //循环读取文件内容，输入流中将最多buf.length个字节的数据读入一个buf数组中,返回类型是读取到的字节数。
            //当文件读取到结尾时返回 -1,循环结束。
            while((length = input.read(buf)) != -1){
                RetVal = new String(buf,0,length);
            }
            //最后记得，关闭流
            if(RetVal!=null){
                byte[] decode = Base64.decode(RetVal, Base64.DEFAULT);
                RetVal = new String(decode,FILE_ENCODE);
            }

        }catch(Exception ex){
            ex.printStackTrace();
            Log.e(TAG,ex.getMessage());
        }finally{
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return RetVal;
    }


    /**
     * 写离线文件.
     * @param context
     * @param taskType  类型
     * @param content   内容
     * @return  返回文件路径
     */
    public static String saveOffLineData(Context context,String taskType,String content){
        if(TextUtils.isEmpty(taskType)){
               return null;
        }
        if(TextUtils.isEmpty(content)){
            return null;
        }
        String extendDataFilePath = getFilePath(context, taskType);

        String RetVal = null;
        File file = null;
        FileOutputStream out = null;
        try {
            File dir = new File(extendDataFilePath);
            //临时文件名
            String tmpName =  System.currentTimeMillis() + ".d"; //临时文件名
            file = new File(dir, tmpName);
            if (file.exists()) {
                file.delete();
            }
            // 输入流
            out = new FileOutputStream(file);
            out.write(Base64.encode(content.getBytes(FILE_ENCODE), Base64.DEFAULT));
            out.flush();

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG,e.getMessage());
        }finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return file.getPath();
    }


    /**
     * 读取扩展信息
     * @param context
     * @return
     */
    public static String readExtendData(Context context){
        String extendDataFilePath = getFilePath(context, FILE_EXTEND_PATH);

        String RetVal = null;
        File file = null;
        FileInputStream input=null;
        try {
            File dir = new File(extendDataFilePath);

            file = new File(dir, EXTENDDATA_FILE);
            if (!file.exists()) {
                Log.w(TAG,"扩展信息文件不存在");
                return null;
            }

            input = new FileInputStream(file);
            byte[] buf = new byte[1024];
            int length = 0;
            //循环读取文件内容，输入流中将最多buf.length个字节的数据读入一个buf数组中,返回类型是读取到的字节数。
            //当文件读取到结尾时返回 -1,循环结束。
            while((length = input.read(buf)) != -1){
                RetVal = new String(buf,0,length);
            }
            //最后记得，关闭流
            if(RetVal!=null){
                byte[] decode = Base64.decode(RetVal, Base64.DEFAULT);
                RetVal = new String(decode,FILE_ENCODE);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG,e.getMessage());
        }finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return  RetVal;
    }


    /**
     * 删除文件
     * @param filePath
     */
    public static void deleteFile(String filePath){
        File file = new File(filePath);

        if(file.exists() && file.isFile()){
            file.delete();
        }

    }

    /**
     * 删除扩展信息
     * @param context
     */
    public static void  deleteExtendData(Context context){
        String extendDataFilePath = getFilePath(context, FILE_EXTEND_PATH);
        File dir = new File(extendDataFilePath);
        File file = new File(dir, EXTENDDATA_FILE);

        if (file.exists()) {
            file.delete();
        }
    }
    /**
     * 保存扩展信息
     */
    public static void saveExtendData(Context context,JSONObject josnObject){
        String extendDataFilePath = getFilePath(context, FILE_EXTEND_PATH);
        Log.i(TAG,"=============saveExtendData() ==============");
        File file = null;
        FileOutputStream out = null;
        try {
                File dir = new File(extendDataFilePath);
                // 目录不存在则创建
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                // 文件
                file = new File(dir, EXTENDDATA_FILE);
                Log.e(TAG,"保存位置:"+file.getPath());
                if (file.exists()) {
                    file.delete();
                }
                // 输入流
                out = new FileOutputStream(file);
                out.write(Base64.encode(josnObject.toString().getBytes(FILE_ENCODE), Base64.DEFAULT));
                out.flush();


        } catch (Exception e) {
            Log.e(TAG,e.getMessage());
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 文件根目录
     * Context.getExternalFilesDir("test"):  /storage/emulated/0/Android/data/com.learn.test/files/test
     * @param context
     * @return
     */
    public static String getFilePath(Context context,String fileDirType) {
        String versionName = "";
        try {
            PackageInfo pkg = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionName = pkg.versionName;
            // int versionCode = pkg.versionCode;
        } catch (Exception e) {
            Log.e(TAG,e.getMessage());
        }
        return context.getExternalFilesDir(fileDirType).getPath();
    }


    /**
     * 读取文件内容
     * @param readFile
     * @return
     */
    public static String readFile(File readFile){

      if(readFile==null){
          return null;
      }

      if(!readFile.exists()){
          return null;
      }
        FileInputStream input=null;
        String RetVal =null;

       try{
            input = new FileInputStream(readFile);
            byte[] buf = new byte[1024];
            int length = 0;
            //循环读取文件内容，输入流中将最多buf.length个字节的数据读入一个buf数组中,返回类型是读取到的字节数。
            //当文件读取到结尾时返回 -1,循环结束。
            while((length = input.read(buf)) != -1){
                RetVal = new String(buf,0,length);
            }
            //最后记得，关闭流
            if(RetVal!=null){
                byte[] decode = Base64.decode(RetVal, Base64.DEFAULT);
                RetVal = new String(decode,FILE_ENCODE);
            }
        }catch (Exception ex){
            Log.e(TAG,ex.getMessage());
        }finally {
            if(input!=null){
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return RetVal;

    }

    /**
     * 获取待上传的所有GPS文件
     * @return
     */
    public static File[] getGPSUPloadFiles(Context context){
        String saveGPSFilePath = Utils.getFilePath(context,GPSLocationListener.GPS_SAVE_DIR);
        return getFileList(saveGPSFilePath);
    }


    /*
     获取 taskType目录下所有文件列表
     */
    public static File[] getFileListByTaskType(Context context,String taskType){
        if(TextUtils.isEmpty(taskType)){
            return null;
        }
        String rootFilePath = getFilePath(context, taskType);
        return getFileList(rootFilePath);
    }


    /**
     * 获取路径下的所有文件
     * @param path
     * @return
     */
    public static File[] getFileList(String path){
        if(TextUtils.isEmpty(path)){
            return null;
        }

        File dirPath=new File(path);
        if(!dirPath.exists()){
            return null;
        }
        return dirPath.listFiles();
    }


    /**
     * 保存GPS文件
     * @param path
     * @param json
     */
    public static void saveFile(String path, String json) {
        Log.d(TAG,"=====saveFile() :"+json);
        try {
            File file = null;
            FileOutputStream out = null;
            try {
                File dir = new File(path);
                // 目录不存在则创建
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                String fileName =  System.currentTimeMillis() + ".tmp"; //临时文件名
                // 文件
                file = new File(dir, fileName);
                if (!file.exists()) {
                    file.createNewFile();
                }
                // 输入流
                out = new FileOutputStream(file);
                out.write(Base64.encode(json.getBytes(FILE_ENCODE), Base64.DEFAULT));
                out.flush();

                //写完文件以后 改名
                String newFileName = System.currentTimeMillis()+".g";
                file.renameTo(new File(path , newFileName));
            } finally {
                if (out != null) {
                    out.close();
                }
            }
        } catch (Exception e) {
            Log.e(TAG,e.getMessage());
        }
    }




    private static final String FINGER_PRINT = "fingerprint";

    /**
     * 获取设备指纹
     * 如果从SharedPreferences文件中拿不到，那么重新生成一个，
     * 并保存到SharedPreferences文件中。
     *
     * @param context
     * @return fingerprint 设备指纹
     */
    public static String getFingerprint(Context context) {
        String fingerprint = null;
        fingerprint = readFingerprintFromFile(context);
        if (TextUtils.isEmpty(fingerprint)) {
            fingerprint = createFingerprint(context);
        } else {
            Log.i(TAG, "从文件中获取设备指纹：" + fingerprint);
        }
        return fingerprint;
    }

    /**
     * 从SharedPreferences 文件获取设备指纹
     *
     * @return fingerprint 设备指纹
     */
    private static String readFingerprintFromFile(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(FINGER_PRINT, null);
    }

    /**
     * 生成一个设备指纹（耗时50毫秒以内）：
     * 1.IMEI + 设备硬件信息（主要）+ ANDROID_ID + WIFI MAC组合成的字符串
     * 2.用MessageDigest将以上字符串处理成32位的16进制字符串
     *
     * @param context
     * @return 设备指纹
     */
    public static String createFingerprint(Context context) {
        long startTime = System.currentTimeMillis();

        // 1.IMEI
        TelephonyManager TelephonyMgr = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        @SuppressLint("MissingPermission") final String imei = TelephonyMgr.getDeviceId();
        Log.i(TAG, "imei=" + imei);
        //2.android 设备信息（主要是硬件信息）
        final String hardwareInfo = Build.ID + Build.DISPLAY + Build.PRODUCT
                + Build.DEVICE + Build.BOARD /*+ Build.CPU_ABI*/
                + Build.MANUFACTURER + Build.BRAND + Build.MODEL
                + Build.BOOTLOADER + Build.HARDWARE /* + Build.SERIAL */
                + Build.TYPE + Build.TAGS + Build.FINGERPRINT + Build.HOST
                ;
        //Build.SERIAL => 需要API 9以上
        Log.i(TAG, "hardward info=" + hardwareInfo);

        /* 3. Android_id 刷机和恢复出厂会变
         * A 64-bit number (as a hex string) that is randomly
         * generated when the user first sets up the device and should remain
         * constant for the lifetime of the user's device. The value may
         * change if a factory reset is performed on the device.
         */
        final String androidId = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        Log.i(TAG, "android_id=" + androidId);

        // Combined Device ID
        final String deviceId = imei + hardwareInfo + androidId/* + wifiMAC + bt_MAC*/;
        Log.i(TAG, "deviceId=" + deviceId);

        // 创建一个 messageDigest 实例
        MessageDigest msgDigest = null;
        try {
            msgDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        //用 MessageDigest 将 deviceId 处理成32位的16进制字符串
        msgDigest.update(deviceId.getBytes(), 0, deviceId.length());
        // get md5 bytes
        byte md5ArrayData[] = msgDigest.digest();

        String deviceUniqueId = new String();
        for (int i = 0; i < md5ArrayData.length; i++) {
            int b = (0xFF & md5ArrayData[i]);
            if (b <= 0xF) deviceUniqueId += "0";
            deviceUniqueId += Integer.toHexString(b);
        } // hex string to uppercase
        deviceUniqueId = deviceUniqueId.toUpperCase();
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(FINGER_PRINT, deviceUniqueId).commit();
        return deviceUniqueId;
    }

    /**
     * 获取机型
     */
    public static String getPhoneModel() {
        String brand = android.os.Build.BRAND;//手机品牌
        String model = android.os.Build.MODEL;//手机型号
//        Log.d(TAG, "手机型号：" + brand + " " + model);
        return brand + " " + model;
    }

    /**
     * 获取操作系统
     *
     * @return
     */
    public static String getOS() {
//        Log.d(TAG, "操作系统:" + "Android" + android.os.Build.VERSION.RELEASE);
        return "Android" + android.os.Build.VERSION.RELEASE;
    }



    public static String combineJson(String srcJObjStr, String addJObjStr) throws JSONException {
        if(addJObjStr == null || addJObjStr.isEmpty()) {
            return srcJObjStr;
        }
        if(srcJObjStr == null || srcJObjStr.isEmpty()) {
            return addJObjStr;
        }

        JSONObject srcJObj = new JSONObject(srcJObjStr);
        JSONObject addJObj = new JSONObject(addJObjStr);

        combineJson(srcJObj, addJObj);

        return srcJObj.toString();
    }

    @SuppressWarnings("unchecked")
    public static JSONObject combineJson(JSONObject srcObj, JSONObject addObj) throws JSONException {

        Iterator<String> itKeys1 = addObj.keys();
        String key, value;
        while(itKeys1.hasNext()){
            key = itKeys1.next();
            value = addObj.optString(key);
            srcObj.put(key, value);
        }
        return srcObj;
    }


    /**
     * 判断服务是否在运行
     * @param context
     * @param serviceName
     * @return
     * 服务名称为全路径 例如com.ghost.WidgetUpdateService
     */
    public static boolean isRunService(Context context,String serviceName) {
        ActivityManager manager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceName.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    /**
     * 检查网络是否可用
     *
     * @param context
     * @return
     */
    public static boolean checkNet(Context context) {// 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
        try {
            ConnectivityManager connectivity = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                // 获取网络连接管理的对象
                NetworkInfo info = connectivity.getActiveNetworkInfo();
                if (info != null && info.isConnected()) {
                    // 判断当前网络是否已经连接
                    if (info.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }


    //文件拷贝
    public static boolean copyFile(String fromFile, String toFile) {
        InputStream fosfrom = null;
        OutputStream fosto = null;
        try {
            fosfrom = new FileInputStream(fromFile);
            fosto = new FileOutputStream(toFile);
            byte bt[] = new byte[1024];
            int c;
            while ((c = fosfrom.read(bt)) > 0) {
                fosto.write(bt, 0, c);
            }
            fosfrom.close();
            fosto.close();
            return true;
        } catch (Exception ex) {
            return false;
        }finally {
            if(fosfrom!=null){
                try {
                    fosfrom.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if(fosto!=null){
                try {
                    fosto.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

}
}
