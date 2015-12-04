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
 * Created by 张孟尧 on 2015/12/5.
 */
public class SwuWifiLogin
{
        public static void setSwuWifiLogin(final String userName, final String password, final SharedPreferences preferences, final SharedPreferences.Editor editor
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
//                  设置Message
                    Message message = new Message();
                    message.what = Constant.SHOW_RESPONSE;
                    message.obj = "正在登录...";
                    handler.sendMessage(message);
                    //wifi登陆地址
                    URL url = new URL("http://202.202.96.57:9060/login/login1.jsp");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setConnectTimeout(Constant.TIMEOUT);
                    connection.setReadTimeout(Constant.TIMEOUT);
                    DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
//
                    String parameter = String.format("username=%s&password=%s&if_login=&B2=", userName, password);
                    outputStream.writeBytes(parameter);

                    InputStream in = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in, "GBK"));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null)
                    {
                        response.append(line);
                    }
                    String responseText = response.toString();
                    if (responseText != null)
                    {
                        Document document;
                        document = Jsoup.parse(responseText);
                        String loginResult = document.body().text();

                        if (loginResult.indexOf("您已通过登录审核") >= 0)
                        {
                            if (loginResult.indexOf(userName) >= 0)
                            {
                                message.obj = "登陆成功";
                                RememberPassword.setRememberPassword(userName, password, rememberPass, editor, preferences);

                            } else
                            {
                                message.obj = "您已经使用另一个账号登陆";
                                RememberPassword.setRememberPassword(userName, password, rememberPass, editor, preferences);

                            }
                        } else if (loginResult.indexOf("密码错误") >= 0)
                        {
                            message.obj = "密码或用户名错误";
                            editor.putString(userName + "password", "");
                            editor.commit();
                        } else if (loginResult.indexOf("账号已在其他计算机上登录") >= 0)
                        {
                            message.obj = "账号已在其他地方登陆，请退出";
                            RememberPassword.setRememberPassword(userName, password, rememberPass, editor, preferences);
                        } else

                        {
                            message.obj = "未知错误";
                        }
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
        }

        ).start();
    }
}
