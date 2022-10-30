package com.is.androidServer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import com.is.androidServer.api.BookApi;
import com.is.androidServer.databinding.ActivityMainBinding;
import com.is.androidServer.utils.NetworkUtils;
import com.is.androidServer.utils.PermissionUtil;
import com.is.androidServer.wifitransfer.Defaults;
import com.is.androidServer.wifitransfer.ServerRunner;

import okhttp3.OkHttpClient;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private BookApi mBookApi;
    private OkHttpClient okHttpClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());

        initDatas();
        initEvent();
        permission();
    }

    private void permission(){
        PermissionUtil.checkPermission(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE},
                4004
        );
    }

    private void initEvent(){
        binding.tvRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initDatas();
            }
        });
    }
    public void initDatas() {
        okHttpClient = new OkHttpClient();
        mBookApi = new BookApi(okHttpClient);
        String wifiname = NetworkUtils.getConnectWifiSsid(this);
        if (!TextUtils.isEmpty(wifiname)) {
            binding.mTvWifiName.setText(wifiname.replace("\"", ""));
        } else {
            binding.mTvWifiName.setText("Unknow");
        }

        String wifiIp = NetworkUtils.getConnectWifiIp(this);
        if (!TextUtils.isEmpty(wifiIp)) {
            binding.tvRetry.setVisibility(View.GONE);
            binding.mTvWifiIp.setText("http://" + NetworkUtils.getConnectWifiIp(this) + ":" + Defaults.getPort());
            // 启动wifi传书服务器
            ServerRunner.startServer(mBookApi);
        } else {
            binding.mTvWifiIp.setText("请开启Wifi并重试");
            binding.tvRetry.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        if (ServerRunner.serverIsRunning) {
/*            new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("确定要关闭？Wifi传书将会中断！")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                           // finish();
                        }
                    }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();*/
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ServerRunner.stopServer();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 4004) {
            if (PermissionUtil.checkGrant(grantResults)) {
            } else {
            }
        }
    }
}