package com.talkweb.etc;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 周期任务
 * 高速项目心跳
 */
public class HeartbeatTask implements Runnable {
    private final static String TAG = HeartbeatTask.class.getSimpleName();
    private String mTaskName = null;
    private Context mContext;

    //提交线程
    private static ExecutorService scheduledThreadPool = Executors.newCachedThreadPool();


    public HeartbeatTask(String taskName, Context context){
        this.mTaskName = taskName;
        this.mContext = context;
    }

    @Override
    public void run() {
        Log.d(TAG,"=========== HeartbeatTask run() ==============");
        try {
            //检查是否有网. 有网络 才触发上传
            if(!Utils.checkNet(this.mContext)){
                Log.w(TAG,"HeartbeatTask, 没有网络不触发上传");
                return;
            }
        //查找是否有文件需要上传
        //这里要防止因为"离线"原因堆积了大量待上传信息.
        //所以需要一个线程池来控制排队上传
        File[] gpsuPloadFiles = Utils.getGPSUPloadFiles(this.mContext);
            Log.i(TAG,"======== 扫描到 "+ gpsuPloadFiles.length +" 个文件待上传处理");
            for(File file : gpsuPloadFiles){
                    scheduledThreadPool.execute(new UPloadFileRunable(file));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {

        }
    }


    /**
     * 提交任务
     */
    class UPloadFileRunable implements  Runnable{
        private final String TAG = UPloadFileRunable.class.getSimpleName();
        private File mfile;

        public UPloadFileRunable(File file){
            this.mfile = file;
        }
        @Override
        public void run() {
            Log.d(TAG,"=========== UPloadFileRunable() ===========");
            try {
                String fileContent = Utils.readFile(this.mfile);
                JSONObject postData = new JSONObject(fileContent);

                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("p", postData.toString()));
                String retVal = this.doPost(params, "https://service.hngsetc.com/appportal/heartbeat");

                if(TextUtils.isEmpty(retVal)){
                    Log.w(TAG,"服务器接口返回出错(null)");
                    return;
                }

                JSONObject retObj = new JSONObject(retVal);
                if(retObj.getJSONObject("code")==null){
                    Log.w(TAG,"服务器接口返回格式异常");
                    return;
                }

                if("0".equalsIgnoreCase(retObj.getJSONObject("code").getString("code"))){
                    Log.i(TAG,"文件内容:"+postData.toString()+" 上传成功!");
                    this.mfile.delete();
                }
//                Thread.sleep(10*1000L);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG,e.getMessage());
            }finally {
                this.mfile=null;
            }
        }

        public String doPost(List<NameValuePair> params, String url) throws Exception{
            String result = null;
            HttpPost httpPost=null;
            try {
                // 新建HttpPost对象
                httpPost = new HttpPost(url);
                // 设置字符集
                HttpEntity entity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
                // 设置参数实体
                httpPost.setEntity(entity);
                // 获取HttpClient对象
                HttpClient httpClient = new DefaultHttpClient();
                //连接超时
                httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 30*1000);
                //请求超时
                httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 30*1000);

                // 获取HttpResponse实例
                HttpResponse httpResp = httpClient.execute(httpPost);
                // 判断是够请求成功
                if (httpResp.getStatusLine().getStatusCode() == 200) {
                    // 获取返回的数据
                    result = EntityUtils.toString(httpResp.getEntity(), "UTF-8");
                    Log.i("HttpPost", "HttpPost方式请求成功，返回数据如下：");
                    Log.i("result", result);
                } else {
                    Log.i("HttpPost", "HttpPost方式请求失败");
                }
            } catch (ConnectTimeoutException e){
                result = null;
                if(httpPost!=null){
                    httpPost.abort();
                }
                Log.e(TAG,e.getMessage());
            }finally {
                if(httpPost!=null){
                    httpPost.abort();
                }
            }
            return result;
        }



    }

}
