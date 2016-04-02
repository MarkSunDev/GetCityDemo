package com.home.mark.getcitydemo;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.home.mark.getcitydemo.Util.GetCityNameByLocation;

public class MainActivity extends AppCompatActivity {

    private TextView mCityNameTv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPromiss();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mCityNameTv = (TextView) findViewById(R.id.tv_city_name);

        Button button = (Button) findViewById(R.id.btn_get_city);
        assert button != null;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetCityNameByLocation.startLocation(MainActivity.this, true, new GetCityNameByLocation.CallBack() {
                    @Override
                    public void onGetLocaltionSuccess(String cityName) {
                        Message msg = new Message();
                        msg.what = 200;
                        msg.obj = cityName;
                        mHandler.sendMessage(msg);

                    }

                    @Override
                    public void onGetLocaltionFail(GetCityNameByLocation.LocErrorType type) {
                        mHandler.sendEmptyMessage(500);
                    }
                });
            }

         });
    }

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 200:
                    String cityName = (String) msg.obj;
                    mCityNameTv.setText(cityName);
                    break;
                case 500:
                    mCityNameTv.setText("定位失败");
                    break;
            }
        }
    };

    private void checkPromiss(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String permission1 = Manifest.permission.ACCESS_COARSE_LOCATION;
            String permission2 = Manifest.permission.ACCESS_FINE_LOCATION;
            requestPermissions(new String[]{permission1,permission2}, 123);
        }
    }

}
