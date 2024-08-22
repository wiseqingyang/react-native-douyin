package com.ainuo.douyin;

import android.content.Intent;
import android.net.Uri;
import android.os.StrictMode;
import android.util.Log;

import com.bytedance.sdk.open.aweme.CommonConstants;
import com.bytedance.sdk.open.aweme.authorize.model.Authorization;
import com.bytedance.sdk.open.aweme.base.ImageObject;
import com.bytedance.sdk.open.aweme.base.MediaContent;
import com.bytedance.sdk.open.aweme.base.ShareParam;
import com.bytedance.sdk.open.aweme.base.TitleObject;
import com.bytedance.sdk.open.aweme.base.VideoObject;
import com.bytedance.sdk.open.aweme.common.handler.IApiEventHandler;
import com.bytedance.sdk.open.aweme.common.model.BaseReq;
import com.bytedance.sdk.open.aweme.common.model.BaseResp;
import com.bytedance.sdk.open.aweme.init.DouYinOpenSDKConfig;
import com.bytedance.sdk.open.aweme.share.Share;
import com.bytedance.sdk.open.douyin.DouYinOpenApiFactory;
import com.bytedance.sdk.open.douyin.api.DouYinOpenApi;
import com.bytedance.sdk.open.douyin.model.OpenRecord;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;

import java.io.File;
import java.util.ArrayList;

public class DouyinModule extends ReactContextBaseJavaModule implements LifecycleEventListener {

    public ReactApplicationContext mContext;
    public static DouYinOpenApi douyinOpenApi;
    private String callerLocalEntry = "com.ainuo.douyin.DouyinCallbackActivity";
    public static Intent callbackInit;
    public Promise promise;


    public DouyinModule(ReactApplicationContext reactContext) {
        super(reactContext);
        mContext = reactContext;

        reactContext.addLifecycleEventListener(this);
    }


    @Override
    public String getName() {
        return "DouYinModule";
    }

    public String getFileUri(String filePath) {

        File file = new File(filePath);
        String authority = mContext.getPackageName() + ".douyinFileProvider";
        Uri gpxContentUri;
        try {
            gpxContentUri = FileProviderAdapter.getUriForFile(mContext, authority, file);
        } catch (IllegalArgumentException e) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            gpxContentUri = Uri.fromFile(file);
        }
        // 授权给抖音访问路径,这里填抖音包名
        mContext.grantUriPermission("com.ss.android.ugc.aweme",
                gpxContentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return gpxContentUri.toString();   // contentUri.toString() 即是以"content://"开头的用于共享的路径
    }


    @ReactMethod
    public void init(String appId) {
        DouYinOpenApiFactory.initConfig(
                new DouYinOpenSDKConfig.Builder()
                        .context(mContext)
                        .clientKey(appId)
                        .build()
        );
        douyinOpenApi = DouYinOpenApiFactory.create(getCurrentActivity());

        DouyinReceiver.registerReceiver(mContext, douyinReceiver);
    }

    private DouyinReceiver douyinReceiver = new DouyinReceiver() {
        @Override
        public void handleIntent(Intent intent) {
            if (douyinOpenApi != null) {
                douyinOpenApi.handleIntent(intent, iApiEventHandler);
            }
        }
    };

    private IApiEventHandler iApiEventHandler = new IApiEventHandler() {
        @Override
        public void onReq(BaseReq req) {

        }

        @Override
        public void onResp(BaseResp resp) {
            WritableMap map = Arguments.createMap();
            Log.d("douyin__", String.valueOf(resp.errorCode));
            Log.d("douyin__", String.valueOf(resp.errorMsg));
            Log.d("douyin__", String.valueOf(resp.getType()));
            if (resp.getType() == CommonConstants.ModeType.SHARE_CONTENT_TO_TT_RESP) {
                map.putInt("code", resp.isSuccess() ? 0 : resp.errorCode);
                map.putString("msg", resp.errorMsg);
                promise.resolve(map);
            }

            if (resp.getType() == CommonConstants.ModeType.SEND_AUTH_RESPONSE) {
                Authorization.Response response = (Authorization.Response) resp;
                map.putInt("code", resp.isSuccess() ? 0 : response.errorCode);
                map.putString("msg", resp.errorMsg);
                if (resp.isSuccess()) {
                    WritableMap data = Arguments.createMap();
                    data.putString("authCode", response.authCode);
                    data.putString("state", response.state);
                    map.putMap("data", map);
                }
                promise.resolve(map);
            }
        }

        @Override
        public void onErrorIntent(Intent intent) {

        }
    };

    @ReactMethod
    public void isAppInstalled(Promise promise) {
        if (douyinOpenApi == null) {
            WritableMap map = Arguments.createMap();
            map.putInt("code", -99);
            map.putString("msg", "sdk 未初始化");
            promise.resolve(map);
            return;
        }
        WritableMap map = Arguments.createMap();
        map.putInt("code", 0);
        map.putBoolean("data", douyinOpenApi.isAppInstalled());
        promise.resolve(map);
    }

    ;

    @ReactMethod
    public void shareVideo(ReadableMap config, Promise promise) {

        if (douyinOpenApi == null) {
            WritableMap map = Arguments.createMap();
            map.putInt("code", -99);
            map.putString("msg", "sdk 未初始化");
            promise.resolve(map);
            return;
        }

        ReadableArray fileNames = config.getArray("videos");
        Boolean isPublish = config.getBoolean("isPublish");
        String shortTitle = config.getString("shortTitle");
        String title = config.getString("title");

        if (douyinOpenApi.isShareSupportFileProvider() &&
                android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {

            this.promise = promise;

            ArrayList<String> videos = new ArrayList<>();
            for (int i = 0; i < fileNames.size(); i++) {
                String path = getFileUri(fileNames.getString(i));
                videos.add(path);
            }

            Share.Request request = new Share.Request();
            VideoObject videoObject = new VideoObject();
            videoObject.mVideoPaths = videos; // 该地方路径可以传FileProvider格式的路径
            MediaContent content = new MediaContent();
            content.mMediaObject = videoObject;
            request.mMediaContent = content;
            //指定当前类名
            request.callerLocalEntry = callerLocalEntry;

            request.newShare = true;
            ShareParam shareParam = new ShareParam();
            request.shareParam = shareParam;

            TitleObject titleObject = new TitleObject();
            shareParam.titleObject = titleObject;
            titleObject.shortTitle = shortTitle;   // 抖音30.0.0版本开始支持该字段
            titleObject.title = title;

            if (douyinOpenApi.isAppSupportShareToPublish() && isPublish) {
                request.shareToPublish = true;
            }

            douyinOpenApi.share(request);
        } else {
            WritableMap map = Arguments.createMap();
            map.putInt("code", -98);
            map.putString("msg", "当前版本不支持分享");
            promise.resolve(map);
        }
    }

    @ReactMethod
    public void record(Promise promise) {
        OpenRecord.Request request = new OpenRecord.Request();
        if (douyinOpenApi != null && douyinOpenApi.isSupportOpenRecordPage()) {  // 判断抖音版本是否支持打开抖音拍摄页
            douyinOpenApi.openRecordPage(request);
        }
    }

    //授权登录
    @ReactMethod
    public void auth(String scope, String state, Promise promise) {
        Authorization.Request request = new Authorization.Request();
        this.promise = promise;
        request.callerLocalEntry = callerLocalEntry;
        request.scope = scope;
        request.state = state;                                 // 用于保持请求和回调的状态，授权请求后原样带回给第三方。
        douyinOpenApi.authorize(request);                    // 优先使用抖音app进行授权，如果抖音app因版本或者其他原因无法授权，则使用wap页授权
    }


    @Override
    public void onHostResume() {

    }

    @Override
    public void onHostPause() {

    }

    @Override
    public void onHostDestroy() {

    }
}
