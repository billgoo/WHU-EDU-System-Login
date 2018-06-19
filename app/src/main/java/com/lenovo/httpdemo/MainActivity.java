package com.lenovo.httpdemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {

    // 显示验证码
    private ImageView captchaImageView;
    // 验证码输入
    private EditText captchaEditText;
    // 登录按钮
    private Button loginButton;
    // 显示网页
    private TextView resultTextView;

    private static final String baseURL = "http://210.42.121.241";
    private static final String loginRelativeURL = "/servlet/Login";
    private static final String captchaRelativeURL = "/servlet/GenImg";
    private static final String indexRelativeURL = "/stu/stu_index.jsp";

    AsyncHttpClient asyncHttpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        captchaImageView = (ImageView) findViewById(R.id.img_info);
        captchaEditText = (EditText) findViewById(R.id.text_info);
        loginButton = (Button) findViewById(R.id.button);
        resultTextView = (TextView) findViewById(R.id.txt_Result);

        /**
         * WebSettings webSettings =   resultWebView .getSettings();
         * webSettings.setJavaScriptEnabled(true);
         * webSettings.setBuiltInZoomControls(true);
         * webSettings.setSupportZoom(true);
         */

        asyncHttpClient = new AsyncHttpClient();;
        // 创建异步请求的客户端对象
        captchaImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshCaptcha();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RequestParams params = new RequestParams();
                params.put("id", "2014300290060");
                // 设置请求的参数名和参数值
                params.put("pwd", "0d464421c998fac88ca492d5db06315d");
                params.put("xdvfb", captchaEditText.getText());
                // 设置请求的参数名和参数     params.put("auto", "on");
                // 设置请求的参数名和参数     params.put("webtype", "classic");
                // 设置请求的参数名和参数

                if (captchaEditText.getText().equals("")) {
                    Toast toast = Toast.makeText(MainActivity.this, "请填写验证码", Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }

                // 执行post方法
                asyncHttpClient.post(baseURL + loginRelativeURL, params, new AsyncHttpResponseHandler() {
                    /**
                     * 成功处理的方法
                     * statusCode:响应的状态码; headers:相应的头信息 比如 响应的时间，
                     * 响应的服务器 ;  * bytes :响应内容的字节
                     */
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        if (statusCode == 200) {
                            try {
                                String htmlText = new String(responseBody, "gb2312");
                                String toastString = "";
                                if (htmlText.contains("验证码错误")) {
                                    toastString = "验证码错误";
                                    captchaEditText.setText("");
                                } else if (htmlText.contains("用户名/密码错误")) {
                                    toastString = "用户名/密码错误";
                                    captchaEditText.setText("");
                                }

                                if (!toastString.equals("")) {
                                    Toast toast = Toast.makeText(MainActivity.this, toastString, Toast.LENGTH_SHORT);
                                    toast.show();
                                }

                                String html = new String(responseBody, "gb2312");
                                Log.i("htmlText: ", html);
                                Document doc = Jsoup.parse(html);
                                Element element = doc.getElementById("nameLable");
                                String name = element.ownText();
                                resultTextView.setText("\n登录成功! \n姓名：\t" + name + "\n");

                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        if (statusCode == 302) {

                            Toast toast = Toast.makeText(MainActivity.this, "登录成功，正在获取姓名...", Toast.LENGTH_SHORT);
                            toast.show();

                            asyncHttpClient.get(baseURL+indexRelativeURL, new AsyncHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                    if (statusCode == 200) {
                                        try {
                                            String html = new String(responseBody, "gb2312");
                                            Log.i("htmlText: ", html);
                                            Document doc = Jsoup.parse(html);
                                            Element element = doc.getElementById("nameLable");
                                            String name = element.ownText();
                                            resultTextView.setText("登录成功! 姓名： " + name);

                                        } catch (UnsupportedEncodingException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                                }
                            });

                        }
                    }
                });
            }
        });

        refreshCaptcha();

    }

    private void refreshCaptcha() {
        asyncHttpClient.get(baseURL + captchaRelativeURL, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                //创建工厂对象
                BitmapFactory bitmapFactory = new BitmapFactory();
                //工厂对象的bytes把字节转换成Bitmap对象
                Bitmap bitmap = bitmapFactory.decodeByteArray(responseBody, 0, responseBody.length);
                //设置图片
                captchaImageView.setImageBitmap(bitmap);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.i("TAG",String.valueOf(statusCode)); // 设置显示的文本
            }
        });
    }
}
