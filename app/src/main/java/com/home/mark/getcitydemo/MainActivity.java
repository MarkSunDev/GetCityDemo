package com.home.mark.getcitydemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.home.mark.getcitydemo.Util.GetCityNameByLocation;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final TextView cityNameTv = (TextView) findViewById(R.id.tv_city_name);

        Button button = (Button) findViewById(R.id.btn_get_city);
        assert button != null;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetCityNameByLocation.startLocation(MainActivity.this, true, new GetCityNameByLocation.CallBack() {
                    @Override
                    public void onGetLocaltionSuccess(String cityName) {
                        assert cityNameTv != null;
                        cityNameTv.setText(cityName);
                    }

                    @Override
                    public void onGetLocaltionFail(GetCityNameByLocation.LocErrorType type) {
                        cityNameTv.setText("定位失败");
                    }
                });
            }
        });
    }



}
