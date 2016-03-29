package com.home.mark.getcitydemo.Util;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.home.mark.getcitydemo.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by mark on 16/3/18.
 */
public class GetCityNameByLocation {

    private final static String GET_GOOGLE_MAP_XML_URL = "http://maps.google.cn/maps/api/geocode/xml";
    private final static String GET_GOOGLE_MAP_JSON_URL = "http://maps.google.cn/maps/api/geocode/json";
    private final static int REQUEST_CODE_ASK_PERMISSIONS = 123;

    public enum LocErrorType {
        netWorkError, cityError, permissionError, failError
    }

    public interface CallBack {

        void onGetLocaltionSuccess(String cityName);
        /**
         * LocErrorType
         * @param type
         */
        void onGetLocaltionFail(LocErrorType type);
    }

    /**
     * 通过网络定位获取城市名称
     * @param activity Activity
     * @param dateType 获取JSON或者XML true为JSON
     * @param callBack 获得城市名的回调
     */
    public static void startLocation(final Activity activity, final boolean dateType, final CallBack callBack) {
        if(callBack == null){
            return;
        }
        NetworkInfo network = ((ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE)).
                getActiveNetworkInfo();
        if (network == null || !network.isAvailable()) {
            Log.e("网络不可用", "网络不可用");
            callBack.onGetLocaltionFail(LocErrorType.netWorkError);
            return;
        }
        LocationManager manager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        //这里IDE会报权限问题，不需理会
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final String permission = Manifest.permission.ACCESS_COARSE_LOCATION;
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                    String message = activity.getResources().getString(R.string.permission_require_desc);
                    message = message + activity.getResources().getString(R.string.permission_setting_desc);
                    new AlertDialog.Builder(activity)
                            .setTitle("Notice")
                            .setMessage(message)
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    activity.requestPermissions(new String[]{permission}, REQUEST_CODE_ASK_PERMISSIONS);
                                }
                            })
                            .create().show();
                } else {
                    activity.requestPermissions(new String[]{permission}, REQUEST_CODE_ASK_PERMISSIONS);
                }
                callBack.onGetLocaltionFail(LocErrorType.permissionError);
                return;
            }
        }
        Location location  = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location == null) {
            callBack.onGetLocaltionFail(LocErrorType.cityError);
            return;
        }
        //纬度
        final double x = ((double) ((int) (location.getLatitude() * 1E6))) / 1000000;
        //经度
        final double y = ((double) ((int) (location.getLongitude() * 1E6))) / 1000000;

        AsyncTask loginTask = new AsyncTask<String,Void,String>() {
            @Override
            protected String doInBackground(String... params) {
                HttpClient httpClient = HttpService.getHttpClient();
                try {
                    String param = "?latlng=%1$s,%2$s&language=zh-CN&sensor=false";
                    param = String.format(param,String.valueOf(x),String.valueOf(y));
                    HttpGet httpGet = new HttpGet(params[0] + param);
                    HttpResponse response = httpClient.execute(httpGet);
                    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
                        HttpEntity responseEntity = response.getEntity();
                        String result = EntityUtils.toString(responseEntity);
                        if (result != null && result.length() > 0){
                            return result;
                        }
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null){
                    String cityName = dateType ? parseResultByJson(result):parseResultByDom(result);
                    if(cityName != null){
                        callBack.onGetLocaltionSuccess("");
                    }else{
                        callBack.onGetLocaltionFail(LocErrorType.cityError);
                    }
                }else {
                      callBack.onGetLocaltionFail(LocErrorType.failError);
                }
            }
        };
        if(dateType){
            TaskPool.getInstance().addTaskInNoBlockPool(loginTask, GET_GOOGLE_MAP_JSON_URL);
        }else{
            TaskPool.getInstance().addTaskInNoBlockPool(loginTask, GET_GOOGLE_MAP_XML_URL);
        }

    }

    /**
     * 解析XML字符串
     * @param resource
     * @return
     */
    private static String parseResultByDom(String resource) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputStream is = new ByteArrayInputStream(resource.getBytes("UTF-8"));
            Document dom = builder.parse(is);
            Element root = dom.getDocumentElement();
            NodeList nodeList = root.getElementsByTagName("status");
            Node node = nodeList.item(0);
            String nodeName = node.getNodeName();
            if("status".equalsIgnoreCase(nodeName)){
                String requestCode = node.getFirstChild().getNodeValue();
                if(requestCode.equalsIgnoreCase("OK")){
                    NodeList resultList = root.getElementsByTagName("result");
                    Element result = (Element) resultList.item(0);
                    NodeList addressComponents = result.getElementsByTagName("address_component");
                    for (int i = 0; i < addressComponents.getLength(); i++) {
                        Element addressElement = (Element) addressComponents.item(i);

                        NodeList typeList = addressElement.getElementsByTagName("type");
                        for (int j = 0; j < typeList.getLength(); j++) {
                            Node typeNode = typeList.item(j);
                            if(typeNode != null){
                                String typeValue = typeNode.getFirstChild().getNodeValue();
                                if("locality".equalsIgnoreCase(typeValue)) {
                                    NodeList longNameNodeList = addressElement.getElementsByTagName("long_name");
                                    Node cityNode = longNameNodeList.item(0);
                                    String cityName = cityNode.getFirstChild().getNodeValue();
                                    Log.e("cityName",cityName.toString());
                                    return cityName;
                                }
                            }
                        }
                    }
                }
            }


        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String parseResultByJson(String resource){

        try {
            JSONObject jsonObject = new JSONObject(resource);
            String statusCode = jsonObject.optString("status");
            if("OK".equalsIgnoreCase(statusCode)){
                JSONArray resultArray = jsonObject.optJSONArray("results");
                JSONObject addressObject = resultArray.optJSONObject(0);
                JSONArray addressArray = addressObject.optJSONArray("address_components");
                for (int i = 0; i < addressArray.length(); i++) {
                    JSONObject object = addressArray.optJSONObject(i);
                    JSONArray typeArray = object.optJSONArray("types");
                    if(typeArray != null && typeArray.length() > 0){
                        for (int j = 0; j < typeArray.length(); j++) {
                            String type = typeArray.optString(j);
                            if("locality".equalsIgnoreCase(type)){
                                String cityName = object.optString("long_name");
                                Log.e("cityName",cityName.toString());
                                return cityName;
                            }
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}
