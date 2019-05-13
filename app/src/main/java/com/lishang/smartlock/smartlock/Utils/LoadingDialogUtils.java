package com.lishang.smartlock.smartlock.Utils;

import android.app.Activity;
import android.app.ProgressDialog;

import java.lang.ref.WeakReference;

/**
 * Created by 方毅超 on 2017/7/19.
 * 数据加载框
 */
public class LoadingDialogUtils {
    /**
     * 数据访问等待框
     */
    private static ProgressDialog loadingDialog;
    private static WeakReference<Activity> reference;

    public static void init(Activity act, String str) {
        if (loadingDialog == null || reference == null || reference.get() == null || reference.get().isFinishing()) {
            reference = new WeakReference<>(act);

            loadingDialog = new ProgressDialog(reference.get());
            loadingDialog.setMessage(str);
            loadingDialog.setCancelable(false);
        }
    }

    public static void setCancelable(boolean b) {
        if (loadingDialog == null) return;
        loadingDialog.setCancelable(b);
    }

    /**
     * 显示等待框
     */
    public static void show(Activity act,String str) {
        init(act,str);
        loadingDialog.show();
    }

    /**
     * 隐藏等待框
     */
    public static void dismiss() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }


    /**
     * 注销加载框，避免发生内存泄露
     */
    public static void unInit() {
        dismiss();
        loadingDialog = null;
        reference = null;
    }
}