package cn.houlang.support.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.houlang.support.ui.loading.CircleProgressLoading;


/**
 *
 * @author #Suyghur,
 * @date 2019/07/17
 */
public class CircleProgressLoadingDialog extends Dialog {

    Context mContext;
    Bundle mBundle = null;
    boolean hasMessage = false;
    String mMessage;
    int mMessageSize;
    int mMessageColor[];
    int mSize = 0;

    private CircleProgressLoadingDialog(Context context) {
        super(context);
    }

    private void initDialog() {
        getWindow().setBackgroundDrawable(new BitmapDrawable());
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCanceledOnTouchOutside(false);
        setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                return keyCode == KeyEvent.KEYCODE_BACK;
            }
        });

        LinearLayout.LayoutParams mainLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        LinearLayout linearLayout = new LinearLayout(mContext);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setBackgroundColor(Color.TRANSPARENT);
        linearLayout.setLayoutParams(mainLayoutParams);
        setContentView(linearLayout);

        //创建CircleProgressLoading的布局

        LinearLayout.LayoutParams cpLayoutParams;
        if (mSize == 0) {
            cpLayoutParams = new LinearLayout.LayoutParams(160, 160);
        } else {
            cpLayoutParams = new LinearLayout.LayoutParams(mSize, mSize);
        }
        cpLayoutParams.gravity = Gravity.CENTER;
        //创建CircleProgressLoading
        CircleProgressLoading circleProgressLoading = new CircleProgressLoading(mContext);
        circleProgressLoading.setParams(mBundle);
        circleProgressLoading.setLayoutParams(cpLayoutParams);
        //把CircleProgressLoading添加的父容器
        linearLayout.addView(circleProgressLoading);

        if (hasMessage) {
            //创建TextView布局
            LinearLayout.LayoutParams tvLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tvLayoutParams.topMargin = 30;
            tvLayoutParams.gravity = Gravity.CENTER;
            //创建TextView
            TextView textView = new TextView(mContext);
            textView.setTextSize(mMessageSize);
            textView.setText(mMessage);
            textView.setTextColor(Color.argb(mMessageColor[0], mMessageColor[1], mMessageColor[2], mMessageColor[3]));
            textView.setLayoutParams(tvLayoutParams);
            //把textView添加到父容器
            linearLayout.addView(textView);
        }

    }

    public static class Builder {
        CircleProgressLoadingDialog loadingDialog;

        public Builder(Context context) {
            loadingDialog = new CircleProgressLoadingDialog(context);
            loadingDialog.mContext = context;
        }


        public Builder setMessage(String message, int messageSize, int[] messageColor) {
            loadingDialog.mMessage = message;
            loadingDialog.mMessageSize = messageSize;
            loadingDialog.mMessageColor = messageColor;
            return this;
        }

        public Builder hasMessage(boolean hasMessage) {
            loadingDialog.hasMessage = hasMessage;
            return this;
        }

        public Builder setLoadingDialogParams(Bundle bundle) {
            loadingDialog.mBundle = bundle;
            return this;
        }

        public Builder setLoadingDialogSize(int size) {
            loadingDialog.mSize = size;
            return this;
        }

        public CircleProgressLoadingDialog build() {
            loadingDialog.initDialog();
            return loadingDialog;
        }
    }
}
