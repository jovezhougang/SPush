package com.jove.spush2;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Process;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.coloros.mcssdk.PushManager;
import com.coloros.mcssdk.callback.PushCallback;
import com.coloros.mcssdk.mode.SubscribeResult;
import com.vivo.push.IPushActionListener;
import com.vivo.push.PushClient;
import com.xiaomi.mipush.sdk.MiPushClient;

import java.util.ArrayList;
import java.util.List;

public class SPushManager {
    private static SPushManager manager;
    private Context context;

    private SPushManager(final Context context) {
        this.context = context.getApplicationContext();
        if (shouldInit(this.context)) {
            PushClient.getInstance(this.context).initialize();
            if (PushClient.getInstance(this.context).isSupport()) {
                PushClient.getInstance(this.context)
                        .turnOnPush(new IPushActionListener() {
                            @Override
                            public void onStateChanged(int state) {
                                if (0 == state) {
                                    PushClient.getInstance(SPushManager.this.context)
                                            .setTopic("", new IPushActionListener() {
                                                @Override
                                                public void onStateChanged(int i) {
                                                    if (0 == i) {
                                                        for (String topic : createPushTags
                                                                (SPushManager.this.context).split
                                                                (",")) {
                                                            PushClient.getInstance(SPushManager.this
                                                                    .context).setTopic(topic, new
                                                                    IPushActionListener() {
                                                                        @Override
                                                                        public void
                                                                        onStateChanged(int i) {
                                                                        }
                                                                    });
                                                        }
                                                    }
                                                }
                                            });
                                }
                            }
                        });
            } else if (PushManager.isSupportPush(this.context)) {
                PushManager.getInstance().register(this.context, getMetaData(this.context
                        , "opop.app_key"), getMetaData(this.context
                        , "opop.app_secret")
                        , mOpopPushCallback);
            } else if (getMetaData(this.context
                    , "UMENG_CHANNEL").toLowerCase().contains("xiaomi")) {
                MiPushClient.registerPush(this.context, getMetaData(this.context
                        , "xmi.app_id"), getMetaData(this.context
                        , "xmi.app_key"));
            } else if (getMetaData(this.context
                    , "UMENG_CHANNEL").toLowerCase().contains("meizu")) {
                com.meizu.cloud.pushsdk.PushManager
                        .register(this.context, getMetaData(this.context
                                , "meizu.app_id"), getMetaData(this.context
                                , "meizu.app_key"));
            }
            {
                final PackageManager pkgManager = this.context.getPackageManager();
                final boolean sdCardWritePermission =
                        pkgManager.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                this.context.getPackageName()) == PackageManager.PERMISSION_GRANTED;
                final boolean phoneSatePermission =
                        pkgManager.checkPermission(Manifest.permission.READ_PHONE_STATE, this
                                .context.getPackageName())
                                == PackageManager.PERMISSION_GRANTED;
                if (Build.VERSION.SDK_INT < 23 || (sdCardWritePermission && phoneSatePermission)) {
                    com.igexin.sdk.PushManager.getInstance().initialize(this.context
                            , GTPushService.class);
                }
                com.igexin.sdk.PushManager.getInstance().registerPushIntentService(this.context
                        , MGTIntentService.class);
            }
        }
    }


    private PushCallback mOpopPushCallback = new PushCallback() {
        @Override
        public void onRegister(int code, String registerId) {
            if (0 == code) {
                final ArrayList<String> tags = new ArrayList<>();
                for (String tag : createPushTags(SPushManager.this.context)
                        .split(",")) {
                    tags.add(tag);
                }
                PushManager.getInstance().setTags(tags);
            }
        }

        @Override
        public void onUnRegister(int i) {

        }

        @Override
        public void onGetAliases(int i, List<SubscribeResult> list) {

        }

        @Override
        public void onSetAliases(int i, List<SubscribeResult> list) {

        }

        @Override
        public void onUnsetAliases(int i, List<SubscribeResult> list) {

        }

        @Override
        public void onSetUserAccounts(int i, List<SubscribeResult> list) {

        }

        @Override
        public void onUnsetUserAccounts(int i, List<SubscribeResult> list) {

        }

        @Override
        public void onGetUserAccounts(int i, List<SubscribeResult> list) {

        }

        @Override
        public void onSetTags(int i, List<SubscribeResult> list) {

        }

        @Override
        public void onUnsetTags(int i, List<SubscribeResult> list) {

        }

        @Override
        public void onGetTags(int i, List<SubscribeResult> list) {

        }

        @Override
        public void onGetPushStatus(int i, int i1) {

        }

        @Override
        public void onSetPushTime(int i, String s) {

        }

        @Override
        public void onGetNotificationStatus(int i, int i1) {

        }
    };

    public static void initialize(Context context) {
        getManager(context);
    }

    public static SPushManager getManager(Context context) {
        synchronized (SPushManager.class) {
            if (null == manager) {
                manager = new SPushManager(context);
            }
        }
        return manager;
    }

    public static String getMetaData(final @NonNull Context context
            , final @NonNull String key) {
        try {
            final ApplicationInfo info = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName()
                            , PackageManager.GET_META_DATA);
            return info.metaData.getString(key, "").replace("M#"
                    , "");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    private static boolean shouldInit(Context context) {
        final ActivityManager am = ((ActivityManager) context.getSystemService(Context
                .ACTIVITY_SERVICE));
        final List<ActivityManager.RunningAppProcessInfo> processInfos = am
                .getRunningAppProcesses();
        final String mainProcessName = context.getPackageName();
        final int myPid = Process.myPid();
        for (ActivityManager.RunningAppProcessInfo info : processInfos) {
            if (info.pid == myPid && mainProcessName.equals(info.processName)) {
                return true;
            }
        }
        return false;
    }


    public static int getAppVersionCode(Context context) {
        final PackageManager pm = context.getPackageManager();
        if (null != pm) {
            try {
                PackageInfo info = pm.getPackageInfo(context.getPackageName(), 0);
                return info.versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return 1;
    }

    public static String createPushTags(Context context) {
        final StringBuilder sBuilder = new StringBuilder();
        if (!TextUtils.isEmpty(SPushManager.getMetaData(context
                , "UMENG_CHANNEL"))) {
            sBuilder.append(SPushManager.getMetaData(context
                    , "UMENG_CHANNEL"));
            sBuilder.append(",");
        }
        sBuilder.append(SPushManager.getAppVersionCode(context));
        sBuilder.append(",");
        sBuilder.append(SPushManager.getAppVersionName(context));
        sBuilder.append(",");
        sBuilder.append(context.getPackageName());
        return sBuilder.toString();
    }

    public static String getAppVersionName(Context context) {
        final PackageManager pm = context.getPackageManager();
        if (null != pm) {
            try {
                PackageInfo info = pm.getPackageInfo(context.getPackageName(), 0);
                return info.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return "V1.0";
    }
}
