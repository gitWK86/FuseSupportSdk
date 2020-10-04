package com.houlang.support.demo;

import android.app.Activity;
import android.os.Bundle;

import cn.houlang.support.ui.circleprogress.CircleProgressLoadingDialog;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CircleProgressLoadingDialog.Builder builder = new CircleProgressLoadingDialog.Builder(this);
    }
}
