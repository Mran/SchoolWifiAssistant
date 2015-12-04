package com.example.schoolwifiassistant;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.widget.CheckBox;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;


/**
 * Created by 张孟尧 on 2015/12/4.
 */
public class WifiExit
{
    public static void setWifiExit(final String userName, final String password, final SharedPreferences preferences, final SharedPreferences.Editor editor
            , final CheckBox rememberPass, final Handler handler)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                HttpURLConnection connection = null;


                try
                {
//                    设置连接
                    URL url = new URL("http://service.swu.edu.cn/fee/remote_logout2.jsp");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setConnectTimeout(Constant.TIMEOUT);
                    connection.setReadTimeout(Constant.TIMEOUT);
                    DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
//                   格式化参数
                    String parameter = String.format("username=%s&password=%s&B1=", userName, password);
//                   提交参数
                    outputStream.writeBytes(parameter);
//                   发送请求，并接受回复
                    InputStream in = connection.getInputStream();
//                   设置缓存，设置格式为GBK
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in, "GBK"));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null)
                    {
                        response.append(line);
                    }
//                    将收到的内容转化为字符串
                    String responseText = response.toString();


//                    将网页回复信息用Message发送到主线程
                    Message message = new Message();
                    message.what = Constant.SHOW_RESPONSE;
                    if (response != null)
                    {
//                      用Jsoup解析网页
                        Document document = Jsoup.parse(responseText);
                        String resultText = document.body().text();
                        if (resultText.indexOf("密码输入有误") >= 0)
                        {
                            editor.putString(userName + "password", "");
                            editor.commit();
                        }
                        if (responseText.indexOf("成功") >= 0)
                        {
                            RememberPassword rememberPassword = new RememberPassword();
                            rememberPassword.setRememberPassword(userName, password, rememberPass, editor, preferences);
                        }
                        message.obj = resultText;
                        handler.sendMessage(message);
                    }


                } catch (MalformedURLException e)
                {
                    e.printStackTrace();
                } catch (ProtocolException e)
                {
                    e.printStackTrace();
                } catch (IOException e)
                {
                    e.printStackTrace();
                } finally
                {
                    if (connection != null)
                    {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }
}
