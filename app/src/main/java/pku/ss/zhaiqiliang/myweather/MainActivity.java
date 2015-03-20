package pku.ss.zhaiqiliang.myweather;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.HostNameResolver;
import org.apache.http.impl.client.DefaultHttpClient;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.zip.GZIPInputStream;


import pku.ss.zhaiqiliang.util.NetUtil;

import java.util.Date;
import java.util.Calendar;


public class MainActivity extends ActionBarActivity implements View.OnClickListener {
    private ImageView mUpdateBtn;
    private TextView todayUpdateTime;
    private TextView todayWendu;
    private TextView todayShidu;
    private TextView todayFeng;
    private TextView todayMajor;
    private TextView todayAQI;
    private TextView todayQuality;
    private TextView todaySMTWTFS;

    private String updateTime;
    private String shidu;
    private String major;
    private String wendu;
    private String fengli;
    private String fengxiang;
    private String aqiS;
    private int aqi;
    private UIHandler uiUpdate;

    class UIHandler extends Handler{
        public UIHandler() {
        }

        public UIHandler(Looper L) {
            super(L);
        }
        @Override
        public void handleMessage(Message message) {
            todayUpdateTime.setText("今天" + updateTime + "发布");
            todayWendu.setText("温度:" + wendu + "ºC");
            todayShidu.setText("湿度:" + shidu);
            todayFeng.setText(fengxiang + fengli);
            todayMajor.setText(major);
            todayAQI.setText(aqiS);
            if (aqi >= 0 && aqi <= 50) {
                todayQuality.setText((CharSequence)"优");
            } else if (aqi >= 51 && aqi <= 100) {
                todayQuality.setText((CharSequence)"良");
            } else if (aqi >= 101 && aqi <= 150) {
                todayQuality.setText((CharSequence)"轻度污染");
            } else if (aqi >= 151 && aqi <= 200) {
                todayQuality.setText((CharSequence)"中度污染");
            } else if (aqi >= 201 && aqi <= 300) {
                todayQuality.setText((CharSequence)"重度污染");
            } else if (aqi >= 300) {
                todayQuality.setText((CharSequence)"严重污染");
            }
            Toast.makeText(MainActivity.this, "今日天气状态已经更新，数据更新时间："+updateTime, Toast.LENGTH_LONG).show();

        }
    }


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.weather_info);
            mUpdateBtn = (ImageView) findViewById(R.id.title_update_btn);
            mUpdateBtn.setOnClickListener(this);

            uiUpdate = new UIHandler();

            todayUpdateTime = (TextView) findViewById(R.id.today_update_time);
            todayWendu = (TextView) findViewById(R.id.today_wendu);
            todayShidu = (TextView) findViewById(R.id.today_shidu);
            todayFeng = (TextView) findViewById(R.id.today_feng);
            todayMajor = (TextView) findViewById(R.id.today_major);
            todayAQI = (TextView) findViewById(R.id.today_aqi);
            todaySMTWTFS = (TextView) findViewById(R.id.today_smtwtfs);
            todayQuality = (TextView) findViewById(R.id.today_quqlity);
            setWeekOfDate();
            SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
            String cityCode = sharedPreferences.getString("main_city_code", "101010100");
            updateWeather(cityCode);
        }


        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();

            //noinspection SimplifiableIfStatement
            if (id == R.id.action_settings) {
                return true;
            }

            return super.onOptionsItemSelected(item);
        }

        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.title_update_btn) {
                SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
                String cityCode = sharedPreferences.getString("main_city_code", "101010100");
                updateWeather(cityCode);
            }
        }

        private void quaryWeatherCode(String cityCode) {
            final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode;
            Log.d("myWeather", address);
            new Thread(new Runnable() {
                public void run() {
                    try {
                        HttpClient httpClient = new DefaultHttpClient();
                        HttpGet httpget = new HttpGet(address);
                        HttpResponse httpResponse = httpClient.execute(httpget);
                        if (httpResponse.getStatusLine().getStatusCode() == 200) {
                            HttpEntity entity = httpResponse.getEntity();

                            InputStream responseStream = entity.getContent();
                            responseStream = new GZIPInputStream(responseStream);
                            BufferedReader reader = new BufferedReader(new InputStreamReader(responseStream));
                            StringBuilder response = new StringBuilder();
                            String str;
                            while ((str = reader.readLine()) != null) {
                                response.append(str);
                            }
                            String responseStr = response.toString();
                            Log.d("myWeather", responseStr);
                            parseXMl(responseStr);
                            Message msg = new Message();
                            Bundle b = new Bundle();
                            b.putString("OK", "OK");
                            msg.setData(b);
                            MainActivity.this.uiUpdate.sendMessage(msg);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();

                    }
                }
            }).start();
        }

        private void updateWeather(String cityCode) {
            Log.d("myWeather", cityCode);
            if (NetUtil.getNetworkState(this) != NetUtil.NETWORK_NONE) {
                Log.d("myWeather", "网络OK");
                quaryWeatherCode(cityCode);
            } else {
                Log.d("myWeather", "网络挂了");
                Toast.makeText(MainActivity.this, "网络不通，请检查网络配置。", Toast.LENGTH_LONG).show();
            }
        }

        private void parseXMl(String xmldata) {
            try {
                XmlPullParserFactory fac = XmlPullParserFactory.newInstance();
                XmlPullParser xmlPullParser = fac.newPullParser();
                xmlPullParser.setInput(new StringReader(xmldata));

                Boolean fx = true;
                Boolean fl = true;
                int eventType = xmlPullParser.getEventType();
                Log.d("myWeather", "parseXML");
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    switch (eventType) {
                        case XmlPullParser.START_DOCUMENT:
                            break;
                        case XmlPullParser.START_TAG:
                            if (xmlPullParser.getName().equals("updatetime")) {
                                eventType = xmlPullParser.next();
                                updateTime = xmlPullParser.getText();
                                Log.d("myWeather", "updatetime" + xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("shidu")) {
                                eventType = xmlPullParser.next();
                                shidu = xmlPullParser.getText();
                                Log.d("myWeather", "shidu" + xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("MajorPollutants")) {
                                eventType = xmlPullParser.next();
                                major = xmlPullParser.getText();
                                Log.d("myWeather", "pm25" + xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("wendu")) {
                                eventType = xmlPullParser.next();
                                wendu = xmlPullParser.getText();
                                Log.d("myWeather", "wendu" + xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("aqi")) {
                                eventType = xmlPullParser.next();
                                aqiS = xmlPullParser.getText();
                                aqi = Integer.parseInt(aqiS);
                                Log.d("myWeather", "aqi" + xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("fengli") && fl) {
                                fl = false;
                                eventType = xmlPullParser.next();
                                fengli = xmlPullParser.getText();
                                Log.d("myWeather", "fengli" + xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("fengxiang") && fx) {
                                fx = false;
                                eventType = xmlPullParser.next();
                                fengxiang = xmlPullParser.getText();
                                Log.d("myWeather", "fengxiang" + xmlPullParser.getText());
                            }
                            break;
                        case XmlPullParser.END_TAG:
                            break;
                    }
                    eventType = xmlPullParser.next();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void setWeekOfDate() {
            Date now = new Date();
            String[] weekDays = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
            Calendar cal = Calendar.getInstance();
            cal.setTime(now);
            int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
            if (w < 0)
                w = 0;
//            return weekDays[w];
            todaySMTWTFS.setText(weekDays[w]);
        }
    }
//    private void updateUI(String responseStr){
//        Document doc = null;
//        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//        DocumentBuilder db;
//        try{
//            db = dbf.newDocumentBuilder();
//            doc = db.parse(new ByteArrayInputStream(responseStr.getBytes()));
//        } catch (ParserConfigurationException e){
//            e.printStackTrace();
//        } catch (Exception e){
//            e.printStackTrace();
//        }
//        doc.getDocumentElement().normalize();
//        Node updateTime = (Node)doc.getElementsByTagName("updatetime");
//        Log.d("myWeather", updateTime.getNodeValue());
//        //Log.d("myWeather", doc.getElementsByTagName("updatetime").getLength()+" "+updateTime.getNodeValue());
//
//    }

