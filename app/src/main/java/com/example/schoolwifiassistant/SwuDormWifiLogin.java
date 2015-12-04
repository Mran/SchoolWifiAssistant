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
public class SwuDormWifiLogin
{

    public static  void setSwuDormWifiLogin(final String userName, final String password, final SharedPreferences preferences, final SharedPreferences.Editor editor
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

                    //宿舍wifi网址
                    URL urlSwuDormWifi = new URL("http://222.198.120.8:8080/loginPhoneServlet");
                    connection = (HttpURLConnection) urlSwuDormWifi.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setReadTimeout(Constant.TIMEOUT);
                    connection.setConnectTimeout(Constant.TIMEOUT);
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
                    long loginTime = System.currentTimeMillis();
//                    格式化参数
                    String parmas = String.format("username=%s&password=%s&loginTime=%d", userName, password, loginTime);
//                    提交参数
                    dataOutputStream.writeBytes(parmas);

                    InputStream inputStream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
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
//                      处理各种情况
                        if (loginResult.indexOf("登录成功") >= 0)
                        {
                            message.obj = "登陆成功";

                            RememberPassword.setRememberPassword(userName, password, rememberPass, editor, preferences);
                        } else if (loginResult.indexOf("You are already logged in") >= 0)
                        {
                            message.obj = "账号已经在其他地方登陆,请退出";

                            RememberPassword.setRememberPassword(userName, password, rememberPass, editor, preferences);

                        } else if (loginResult.indexOf("请求被拒绝") >= 0)
                        {
                            message.obj = "请求被拒绝，请几秒后重试";

                            RememberPassword.setRememberPassword(userName, password, rememberPass, editor, preferences);

                        } else if (loginResult.indexOf("short") >= 0)
                        {
                            message.obj = "登陆频繁,请稍后重试";

                            RememberPassword.setRememberPassword(userName, password, rememberPass, editor, preferences);

                        } else if (loginResult.indexOf("认证过程中") >= 0)
                        {
                            message.obj = "正在认证...";
                        } else if (loginResult.indexOf("不能为空") >= 0)
                        {
                            message.obj = "用户名不能为空";
                        } else if (loginResult.indexOf("Password check failed") >= 0)
                        {
                            message.obj = "密码或用户名错误";
                            editor.putString(userName + "password", "");
                            editor.commit();
                        } else if (loginResult.indexOf("exist") >= 0)
                        {
                            message.obj = "用户名不存在";
                        } else message.obj = loginResult;
                    }
                    handler.sendMessage(message);


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
