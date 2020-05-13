package com.cyh.netcontroldemo;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class MainActivity extends Activity implements View.OnClickListener{
    private Button getButton;
    private Button parseDataButton;
    private TextView textView;

    private String mResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
    }


    /**
     * Find the Views in the layout<br />
     * <br />
     * Auto-created on 2020-05-13 11:36:13 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    private void findViews() {
        getButton = (Button)findViewById( R.id.getButton );
        parseDataButton = (Button)findViewById( R.id.parseDataButton );
        textView = (TextView)findViewById( R.id.textView );

        getButton.setOnClickListener( this );
        parseDataButton.setOnClickListener( this );
    }

    /**
     * Handle button click events<br />
     * <br />
     * Auto-created on 2020-05-13 11:36:13 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    @Override
    public void onClick(View v) {
        if ( v == getButton ) {
            // Handle clicks for getButton
         //从网络请求数据
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String urlString = "http://www.imooc.com/api/teacher?type=2&page=1";
                    mResult = resultDataBySet(urlString);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mResult = decode(mResult);
                            textView.setText(mResult);
                        }
                    });

                }
            }).start();


        } else if ( v == parseDataButton ) {
            // Handle clicks for parseDataButton

            handleJSONData(mResult);
        }
    }

    /**
     * 解析Json数据
     * @param mResult
     */
    private void handleJSONData(String mResult) {

        try {
            ListenResult listenResult = new ListenResult();
            JSONObject jsonObject = new JSONObject(mResult);
            int status = jsonObject.getInt("status");
            JSONArray data = jsonObject.getJSONArray("data");

            List<ListenResult.DataBean> lessonList = new ArrayList<>();

            if (data != null && data.length() > 0){
                for(int i = 0; i < data.length(); i++){
                    JSONObject item = (JSONObject) data.get(i);
                    int id = item.getInt("id");
                    String name = item.getString("name");
                    String picSmall = item.getString("picSmall");
                    String picBig = item.getString("picBig");
                    int learner = item.getInt("learner");
                    String description = item.getString("description");


                    ListenResult.DataBean lesson = new ListenResult.DataBean();
                    lesson.setId(id);
                    lesson.setName(name);
                    lesson.setPicSmall(picSmall);
                    lesson.setPicBig(picBig);
                    lesson.setDescription(description);
                    lesson.setLearner(learner);
                    lessonList.add(lesson);
                }

                listenResult.setStatus(status);
                listenResult.setData(lessonList);
                textView.setText("data is : " + listenResult.toString());
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String resultDataBySet(String urlString) {
        String result = null;
        //1.构造URL对象（有url地址）
        //2.打开URL连接，返回HttpURLConnection对象，拿到connection连接。
        //3、设置请求连接属性
        //对connection设置参数：请求超时、请求方法、请求属性（键值对，比如希望拿到的数据是json格式和UTF-8格式）
        //发起连接
        ///4、获取响应码，判断连接结果码
        //5、读取输入流并解析
        try {
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(30*1000);
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Content-Type","application/json");
            urlConnection.setRequestProperty("Charset","UTF-8");
            urlConnection.setRequestProperty("Accept-Charset","UTF-8");
            urlConnection.connect();

            int responseCode = urlConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK){
                InputStream inputStream = urlConnection.getInputStream();

                result = streamToString(inputStream);

            }else {
                String responseMessage = urlConnection.getResponseMessage();
                Log.e(TAG,"responseMessage: " + responseMessage);
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 将输入流转换成字符串
     *
     * @param is 从网络获取的输入流
     * @return 字符串
     */
    public String streamToString(InputStream is) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            baos.close();
            is.close();
            byte[] byteArray = baos.toByteArray();
            return new String(byteArray);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    /**
     * 将Unicode字符转换为UTF-8类型字符串
     */
    public static String decode(String unicodeStr) {
        if (unicodeStr == null) {
            return null;
        }
        StringBuilder retBuf = new StringBuilder();
        int maxLoop = unicodeStr.length();
        for (int i = 0; i < maxLoop; i++) {
            if (unicodeStr.charAt(i) == '\\') {
                if ((i < maxLoop - 5)
                        && ((unicodeStr.charAt(i + 1) == 'u') || (unicodeStr
                        .charAt(i + 1) == 'U')))
                    try {
                        retBuf.append((char) Integer.parseInt(unicodeStr.substring(i + 2, i + 6), 16));
                        i += 5;
                    } catch (NumberFormatException localNumberFormatException) {
                        retBuf.append(unicodeStr.charAt(i));
                    }
                else {
                    retBuf.append(unicodeStr.charAt(i));
                }
            } else {
                retBuf.append(unicodeStr.charAt(i));
            }
        }
        return retBuf.toString();
    }
}
