package com.coolweather.android;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.coolweather.android.gson.Forecast;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private static final String TAG = "WeatherActivity";

    private ScrollView weatherLayout;

    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        //初始化各個控件
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        aqiText = (TextView) findViewById(R.id.aqi_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        comfortText = (TextView) findViewById(R.id.comfort_text);
        carWashText = (TextView) findViewById(R.id.car_wash_text);
        sportText = (TextView) findViewById(R.id.sport_text);
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
//        String weatherString = prefs.getString("weather",null);
//        Log.d(TAG, "onCreate: " + weatherString);
//        if(weatherString != null){
//            //有緩存時直接解析天氣數據
//            Weather weather = Utility.handleWeatherResponse(weatherString);
//            showWeatherInfo(weather);
//        }else{
//            //無緩存時去服務器查詢天氣數據
//            String weatherId = getIntent().getStringExtra("weather_id");
//            weatherLayout.setVisibility(View.INVISIBLE);
//            requestWeather(weatherId);
//        }

        String weatherId = getIntent().getStringExtra("weather_id");
        requestWeather(weatherId);

    }

    public void requestWeather(final String weatherId){
        Log.d(TAG, "run: 77777777777777777777777777");
        //獲取對應地址的Url
        String weatherUrl = "http://guolin.tech/api/weather?cityid="+weatherId+"&key=6f08f409be284b0f8df4bef14cf4c7f5";
        Log.d(TAG, "requestWeather: " + weatherId);
        //發送Http請求
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            //失敗時返回的結果
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"獲取天氣數據失敗",Toast.LENGTH_SHORT).show();
                    }
                });
            }
            //成功時返回的結果
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                Log.d(TAG, "run: 77777777777777777777777777");
                final  Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather!=null && "ok".equals(weather.status)){
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            showWeatherInfo(weather);

                        }else{
                            Toast.makeText(WeatherActivity.this, "獲取天氣數據失敗", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    /**
     * 處理並展示Weather實體類中的數據
     */

    private void showWeatherInfo(Weather weather){
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "度";
        String weatherInfo = weather.now.more.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();

        Log.d(TAG, "showWeatherInfo: " + cityName);

        for(Forecast forecast:weather.forecastList){
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
            TextView dateText = (TextView) view.findViewById(R.id.data_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }
        if(weather.aqi != null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }

        String comfort = "舒適度：" + weather.suggestion.comfort.info;
        String carWash = "洗車：" + weather.suggestion.carWash.info;
        String sport = "運動建議：" + weather.suggestion.sport.info;

        Log.d(TAG, "showWeatherInfo: " + comfort);
        Log.d(TAG, "showWeatherInfo: " + carWash);
        Log.d(TAG, "showWeatherInfo: " + sport);

        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);


    }
}