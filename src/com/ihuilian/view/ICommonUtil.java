package com.ihuilian.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;

import com.ihuilian.application.HLApplication;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * 提示信息的管理
 */
@SuppressLint("SimpleDateFormat")
public class ICommonUtil {
    private static ProgressDialog dialog;

    private static int mScreenWidth = 0;
    private static int mScreenHeight = 0;

    public static final String USER_PIC = "USER_PIC";

    /**
     * debug环境时：返回选择的"dev."/"bet.".. 测试环境短路径，
     * release环境时：返回""-正式环境短路径；
     *
     * @return isDebug=false ""
     * isDebug=true "dev."/"bet."....
     */
//    public static String getDEV() {
//        return isApkDebugable() ? SharedPerUtil.getConfigInfo(MyApplication.context, SharedConfigKey.environmentName) : "";
//    }

    /**
     * 在Android应用程序中来判断当前应用是否处于debug状态来做一些操作
     */
//    public static boolean isApkDebugable() {
//        try {
//            ApplicationInfo info = MyApplication.context.getApplicationInfo();
//            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
//        } catch (Exception e) {
//
//        }
//        return false;
//    }

    public static int getScreenWidth(Context context) {
        initScreenInfo(context);
        return mScreenWidth;
    }

    public static int getScreenHeight(Context context) {
        initScreenInfo(context);
        return mScreenHeight;
    }

    /**
     * 判断请求结果是否正确返回
     *
     * @param context
     * @param json
     * @return json code
     */
    public static int getJsonCode(Context context, String json) {
        int codeString = -1;
        try {
            JSONObject jsonO = new JSONObject(json);
            codeString = Integer.parseInt(jsonO.getString("code"));

        } catch (JSONException e) {
        }
        return codeString;
    }

    /**
     * 判断请求结果是否正确返回
     *
     * @param context
     * @param json
     * @return json code
     */
    public static String getJsonMessage(Context context, String json) {
        String message = "";
        try {
            JSONObject jsonO = new JSONObject(json);
            message = jsonO.getString("message");

        } catch (JSONException e) {
        }
        return message;
    }

    /**
     * 获取图片Uri的绝对路径
     *
     * @param uri
     * @return
     */
    @SuppressWarnings("deprecation")
    public static String getAbsoluteImagePath(Activity act, Uri uri) {
        if (uri.getScheme().equals("file")) {
            return uri.getPath();
        }
        // can post image
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = act.managedQuery(uri, proj, // Which columns to return
                null, // WHERE clause; which rows to return (all rows)
                null, // WHERE clause selection arguments (none)
                null);// Order-by clause (ascending by name)
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }



    /**
     * 设置关键字变色
     *
     * @return
     */
    public static Spanned resetColorString(String oldString, String keyWord) {
        if (TextUtils.isEmpty(oldString)) {
            oldString = "";
        }
        Spanned temp = Html.fromHtml(oldString);
        if (oldString != null && oldString.contains(keyWord)) {
            int index = oldString.indexOf(keyWord);
            int len = keyWord.length();

            temp = Html.fromHtml(oldString.substring(0, index)
                    + "<font color=#00C2fa>"
                    + oldString.substring(index, index + len) + "</font>"
                    + oldString.substring(index + len, oldString.length()));

        }
        return temp;
    }

    private static long lastClickTime;
    /**
     * 防止多次点击，两次点击的时间差大于800ms
     *
     * @return
     */
    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (0 < timeD && timeD < 300) {
            return true;
        }
        lastClickTime = time;
        return false;
    }


    private static void initScreenInfo(Context context) {
        if (mScreenWidth == 0 || mScreenHeight == 0) {
            Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
            DisplayMetrics dm = new DisplayMetrics();
            try {
                if (Build.VERSION.SDK_INT < 13) {
                    display.getMetrics(dm);
                } else {
                    display.getMetrics(dm);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            mScreenWidth = dm.widthPixels;
            mScreenHeight = dm.heightPixels;
        }
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 获取当前网络类型
     *
     * @return -1：没有网络   1：WIFI网络   2：WAP网络    3：NET网络
     */
    /*public static final int NETTYPE_WIFI = 0x01;
    public static final int NETTYPE_CMWAP = 0x02;
    public static final int NETTYPE_CMNET = 0x03;

    public static int getNetworkType() {
        Context context = HLApplication.appcontext;
        int netType = -1;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null) {
            return netType;
        }
        int nType = networkInfo.getType();
        if (nType == ConnectivityManager.TYPE_MOBILE) {
            String extraInfo = networkInfo.getExtraInfo();
            if (!TextUtils.isEmpty(extraInfo)) {
                if (extraInfo.toLowerCase().equals("cmnet")) {
                    netType = NETTYPE_CMNET;
                } else {
                    netType = NETTYPE_CMWAP;
                }
            }
        } else if (nType == ConnectivityManager.TYPE_WIFI) {
            netType = NETTYPE_WIFI;
        }
        return netType;
    }*/

    /**
     * 获取设备信息
     *
     * @return
     */
    public static String getDeviceInfo() {
        StringBuffer sb = new StringBuffer();
        sb.append(Build.MODEL)
                .append("_")
                .append(Build.VERSION.RELEASE)
                .append("_")
                .append(Build.VERSION.SDK_INT);
        return sb.toString().trim();
    }

    /**
     * 根据容器的大小，以及图片的大小，计算缩放后图片的大小
     */
    public static int[] calcScaleSize(int picWidth, int picHidth, int containerWidth, int containerHeight) {

        int resizeWidth = picWidth;
        int resizeHeight = picHidth;

        if (picWidth < containerWidth && picHidth > containerHeight) {// 图片高度大于屏幕高度
            resizeWidth = picWidth * containerHeight / picHidth;
            resizeHeight = containerHeight;
        } else if (picWidth > containerWidth && picHidth > containerHeight) { // 图片宽高都大于屏幕宽高
            if (picWidth > picHidth) {
                resizeWidth = containerWidth;
                resizeHeight = picHidth * containerWidth / picWidth;
            } else {
                resizeWidth = picWidth * containerHeight / picHidth;
                resizeHeight = containerHeight;
            }
        } else if (picWidth > containerWidth && picHidth < containerHeight) {// 图片宽度大于图片宽度
            resizeWidth = containerWidth;
            resizeHeight = picHidth * containerWidth / picWidth;
        }
        return new int[]{resizeWidth, resizeHeight};
    }

    /**
     * 获取当前网络类型
     * @return -1：没有网络 1：WIFI 2：2G 3：3G 4:4G
     */
    public static final int NETTYPE_WIFI = 0x01;
    public static final int NETTYPE_2G = 0x02;
    public static final int NETTYPE_3G = 0x03;
    public static final int NETTYPE_4G = 0x04;

    public static int getNetworkType() {
        int strNetworkType = -1;
        Context context = HLApplication.appcontext;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                //Wifi网络
                strNetworkType = NETTYPE_WIFI;
            } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                String _strSubTypeName = networkInfo.getSubtypeName();
                Log.d("cocos2d-x", "Network getSubtypeName : " + _strSubTypeName);
                // TD-SCDMA   networkType is 17
                int networkType = networkInfo.getSubtype();
                switch (networkType) {
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN: //api<8 : replace by 11
                        //2G网络
                        strNetworkType = NETTYPE_2G;
                        break;
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B: //api<9 : replace by 14
                    case TelephonyManager.NETWORK_TYPE_EHRPD:  //api<11 : replace by 12
                    case TelephonyManager.NETWORK_TYPE_HSPAP:  //api<13 : replace by 15
                        //3G网络
                        strNetworkType = NETTYPE_3G;
                        break;
                    case TelephonyManager.NETWORK_TYPE_LTE:    //api<11 : replace by 13
                        //4G网络
                        strNetworkType = NETTYPE_4G;
                        break;
                    default:
                        // http://baike.baidu.com/item/TD-SCDMA 中国移动 联通 电信 三种3G制式
                        /*if (_strSubTypeName.equalsIgnoreCase("TD-SCDMA") || _strSubTypeName.equalsIgnoreCase("WCDMA") || _strSubTypeName.equalsIgnoreCase("CDMA2000")) {
                            strNetworkType = NETTYPE_3G;
                        } else {
                            strNetworkType = _strSubTypeName;
                        }*/
                        strNetworkType = NETTYPE_3G;//其他暂时默认3G
                        break;
                }
                Log.d("cocos2d-x", "Network getSubtype : " + Integer.valueOf(networkType).toString());
            }
        }

        Log.d("cocos2d-x", "Network Type : " + strNetworkType);

        return strNetworkType;
    }
}

