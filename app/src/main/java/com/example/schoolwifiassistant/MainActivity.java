package com.example.schoolwifiassistant;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class MainActivity extends Activity implements View.OnClickListener, PopupWindow.OnDismissListener, AdapterView.OnItemClickListener
{
    public static final int TIMEOUT = 4000;
    //    设置Message标记
    public static final int SHOW_RESPONSE = 0;
    //    设置记住密码
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    //    记住密码复选框
    private CheckBox rememberPass;
    //    发送退出请求按钮
    private Button sendExitRequest;
    //    发送登陆请求按钮
    private Button sendLoginResquest;
    //    显示请求结果
    private TextView responseTEXT;
    //    输入用户名
    private EditText editTextUserName;
    //    输入密码
    private EditText editTextPassword;
    //
    private PopupWindow popupWindowUsername;
    //    图片
    private ImageView imageViewMore;

    private boolean mShowing;
    private Map map;
    private List<UserNameList> userNameLists= new ArrayList<>();
    private ArrayList<UserNameList> mList = new ArrayList<>();
    private UserNameAdapter mAdapter;
    private ListView mListView;
    private boolean mInitPopup;
    //    用户名和密码
    private String userName;
    private String password;

    //    设置多线程
    private Handler handler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
//                得到Message标记
                case SHOW_RESPONSE:
                    String response = (String) msg.obj;
//显示结果
                    responseTEXT.setText(response);
            }
        }


    };

    @Override

    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        设置记住密码
        preferences = getSharedPreferences("remembera", MODE_PRIVATE);
        rememberPass = (CheckBox) findViewById(R.id.remember_pass);
//        绑定按钮和编辑框
        sendExitRequest = (Button) findViewById(R.id.exit);
        sendLoginResquest = (Button) findViewById(R.id.login);
        responseTEXT = (TextView) findViewById(R.id.response_text);
        editTextUserName = (EditText) findViewById(R.id.userName);
        editTextPassword = (EditText) findViewById(R.id.password);
        imageViewMore = (ImageView) findViewById(R.id.add);
        map = preferences.getAll();
        InitList();
        editor = preferences.edit();
//        设置默认记住密码
        boolean isRemember = preferences.getBoolean("remember_password", false);
        if (isRemember)
        {
//            将记住的密码填到文本框
            String account = mList.get(0).getName();
            String password = preferences.getString(mList.get(0).getName() + "password", "");
//            preferences.getAll();
            editTextUserName.setText(account);
            editTextPassword.setText(password);
        }

//        监听按钮事件
        sendExitRequest.setOnClickListener(this);
        sendLoginResquest.setOnClickListener(this);
        imageViewMore.setOnClickListener(this);
    }

    private void InitList()
    {


        if (map.size() != 0)
        {
            Set set = map.keySet();
            Iterator iterator = set.iterator();
            while (iterator.hasNext())
            {
               UserNameList userNameList = new UserNameList((String) iterator.next());

                if (userNameList.getName().indexOf("password") < 0 && !userNameList.getName().equals("") && !mList.contains(userNameList.getName()))
                {
                    mList.add(userNameList);
                }
            }
        }
    }

    @Override
    public void onClick(View v)
    {
        InitList();
//得到文本框里的用户名和密码
        userName = editTextUserName.getText().toString();
        password = editTextPassword.getText().toString();
//        RememberPassword rememberPassword=new RememberPassword();
//        rememberPassword.setRememberPassword(userName,password,rememberPass,editor,preferences);
//        检查复选框是否被选中
        if (rememberPass.isChecked())
        {
//           将记住密码设置为是
            editor.putBoolean("remember_password", true);
//            将用户名和密码保存
            if (!preferences.contains(userName) || !preferences.contains(userName + "password"))
            {
                editor.putString(userName, userName);
                editor.putString(userName + "password", password);
            }
        } else
        {
//            如果没有选中就清除记住的用户和密码

            editor.remove(userName);
            editor.remove(userName + "password");
        }
        editor.commit();

        //wifi ssid状态获取
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String wifiSsid = wifiInfo.toString();
//检测到按下登陆按钮
        if (v.getId() == R.id.login)
        {

//              判断目前是否连在宿舍wifi下
            if (wifiSsid.indexOf("dorm") >= 0)
            {
               SwuDormWifiLogin.setSwuDormWifiLogin(userName, password, preferences, editor, rememberPass, handler);
                //                  设置Message
                Message message = new Message();
                message.what = SHOW_RESPONSE;
                message.obj = "正在登录...";
                handler.sendMessage(message);

            } else if (wifiSsid.indexOf("swu-wifi") >= 0)
            {
                SwuWifiLogin.setSwuWifiLogin(userName, password, preferences, editor, rememberPass, handler);
                //                  设置Message
                Message message = new Message();
                message.what = SHOW_RESPONSE;
                message.obj = "正在登录...";
                handler.sendMessage(message);

            } else
            {
//                如果没有连接着两个wifi提示连接
                Message message = new Message();
                message.what = SHOW_RESPONSE;
                message.obj = "请连接swu-dorm-wifi或者swu-wifi后尝试登陆";
                handler.sendMessage(message);
            }

        }
//        检测到按下退出按钮
        if (v.getId() == R.id.exit)
        {
//判断是否连在校园网
            if (wifiSsid.indexOf("swu-wifi") >= 0)
            {

                WifiExit.setWifiExit(userName, password, preferences, editor, rememberPass, handler);
//                wifiExit(userName, password);
                Message message = new Message();
                message.what = SHOW_RESPONSE;
                message.obj = "正在退出";
                handler.sendMessage(message);

            } else
            {
                Message message = new Message();
                message.what = SHOW_RESPONSE;
                message.obj = "请连接swu-dorm-wifi或者swu-wifi后尝试登陆";
                handler.sendMessage(message);
            }

        }
        if (v.getId() == R.id.add)
        {
            Toast.makeText(this, "点击图片", Toast.LENGTH_SHORT).show();
            if (mList != null && mList.size() > 0 && !mInitPopup)
            {
                mInitPopup = true;
                initPopup();
            }
            if (popupWindowUsername != null)
            {
                if (!mShowing)
                {
                    popupWindowUsername.showAsDropDown(editTextUserName, 0, -5);
                    mShowing = true;
                } else
                {
                    popupWindowUsername.dismiss();
                }
            }
        }


    }

//    private void wifiExit(final String userName, final String password)
//    {
//        new Thread(new Runnable()
//        {
//            @Override
//            public void run()
//            {
//                HttpURLConnection connection = null;
//
//
//                try
//                {
////                    设置连接
//                    URL url = new URL("http://service.swu.edu.cn/fee/remote_logout2.jsp");
//                    connection = (HttpURLConnection) url.openConnection();
//                    connection.setRequestMethod("POST");
//                    connection.setConnectTimeout(TIMEOUT);
//                    connection.setReadTimeout(TIMEOUT);
//                    DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
////                   格式化参数
//                    String parameter = String.format("username=%s&password=%s&B1=", userName, password);
////                   提交参数
//                    outputStream.writeBytes(parameter);
////                   发送请求，并接受回复
//                    InputStream in = connection.getInputStream();
////                   设置缓存，设置格式为GBK
//                    BufferedReader reader = new BufferedReader(new InputStreamReader(in, "GBK"));
//                    StringBuilder response = new StringBuilder();
//                    String line;
//                    while ((line = reader.readLine()) != null)
//                    {
//                        response.append(line);
//                    }
////                    将收到的内容转化为字符串
//                    String responseText = response.toString();
//
//
////                    将网页回复信息用Message发送到主线程
//                    Message message = new Message();
//                    message.what = SHOW_RESPONSE;
//                    if (response != null)
//                    {
////                      用Jsoup解析网页
//                        Document document = Jsoup.parse(responseText);
//                        String resultText = document.body().text();
//                        if (resultText.indexOf("密码输入有误") >= 0)
//                        {
//                            editor.putString(userName + "password", "");
//                            editor.commit();
//                        }
//                        if (responseText.indexOf("成功")>=0)
//                        {
//                            RememberPassword rememberPassword=new RememberPassword();
//                            rememberPassword.setRememberPassword(userName,password,rememberPass,editor,preferences);
//                        }
//                        message.obj = resultText;
//                        handler.sendMessage(message);
//                    }
//
//
//                } catch (MalformedURLException e)
//                {
//                    e.printStackTrace();
//                } catch (ProtocolException e)
//                {
//                    e.printStackTrace();
//                } catch (IOException e)
//                {
//                    e.printStackTrace();
//                } finally
//                {
//                    if (connection != null)
//                    {
//                        connection.disconnect();
//                    }
//                }
//            }
//        }).start();
//    }




//    private void swuDormWifiLogin(final String userName, final String password)
//    {
//        new Thread(new Runnable()
//        {
//            @Override
//            public void run()
//            {
//                HttpURLConnection connection = null;
//
//                try
//                {
//                    //                  设置Message
//                    Message message = new Message();
//                    message.what = SHOW_RESPONSE;
//
//                    //宿舍wifi网址
//                    URL urlSwuDormWifi = new URL("http://222.198.120.8:8080/loginPhoneServlet");
//                    connection = (HttpURLConnection) urlSwuDormWifi.openConnection();
//                    connection.setRequestMethod("POST");
//                    connection.setReadTimeout(TIMEOUT);
//                    connection.setConnectTimeout(TIMEOUT);
//                    connection.setDoOutput(true);
//                    connection.setDoInput(true);
//                    DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
//                    long loginTime = System.currentTimeMillis();
////                    格式化参数
//                    String parmas = String.format("username=%s&password=%s&loginTime=%d", userName, password, loginTime);
////                    提交参数
//                    dataOutputStream.writeBytes(parmas);
//
//                    InputStream inputStream = connection.getInputStream();
//                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
//                    StringBuilder response = new StringBuilder();
//                    String line;
//                    while ((line = reader.readLine()) != null)
//                    {
//                        response.append(line);
//                    }
//                    String responseText = response.toString();
//
//
//                    if (responseText != null)
//                    {
//                        Document document;
//                        document = Jsoup.parse(responseText);
//                        String loginResult = document.body().text();
////                      处理各种情况
//                        if (loginResult.indexOf("登录成功") >= 0)
//                        {
//                            message.obj = "登陆成功";
//
//                            RememberPassword.setRememberPassword(userName, password, rememberPass, editor, preferences);
//                        } else if (loginResult.indexOf("You are already logged in") >= 0)
//                        {
//                            message.obj = "账号已经在其他地方登陆,请退出";
//
//                            RememberPassword.setRememberPassword(userName, password, rememberPass, editor, preferences);
//
//                        } else if (loginResult.indexOf("请求被拒绝") >= 0)
//                        {
//                            message.obj = "请求被拒绝，请几秒后重试";
//
//                            RememberPassword.setRememberPassword(userName, password, rememberPass, editor, preferences);
//
//                        } else if (loginResult.indexOf("short") >= 0)
//                        {
//                            message.obj = "登陆频繁,请稍后重试";
//
//                            RememberPassword.setRememberPassword(userName, password, rememberPass, editor, preferences);
//
//                        } else if (loginResult.indexOf("认证过程中") >= 0)
//                        {
//                            message.obj = "正在认证...";
//                        } else if (loginResult.indexOf("不能为空") >= 0)
//                        {
//                            message.obj = "用户名不能为空";
//                        } else if (loginResult.indexOf("Password check failed") >= 0)
//                        {
//                            message.obj = "密码或用户名错误";
//                            editor.putString(userName + "password", "");
//                            editor.commit();
//                        } else if (loginResult.indexOf("exist") >= 0)
//                        {
//                            message.obj = "用户名不存在";
//                        } else message.obj = loginResult;
//                    }
//                    handler.sendMessage(message);
//
//
//                } catch (MalformedURLException e)
//                {
//                    e.printStackTrace();
//                } catch (ProtocolException e)
//                {
//                    e.printStackTrace();
//                } catch (IOException e)
//                {
//                    e.printStackTrace();
//                } finally
//                {
//                    if (connection != null)
//                    {
//                        connection.disconnect();
//                    }
//                }
//            }
//        }).start();
//    }

//    private void swuWifiLogin(final String userName, final String password)
//    {
//        new Thread(new Runnable()
//        {
//            @Override
//            public void run()
//            {
//                HttpURLConnection connection = null;
//
//
//                try
//                {
////                  设置Message
//                    Message message = new Message();
//                    message.what = SHOW_RESPONSE;
//                    message.obj = "正在登录...";
//                    handler.sendMessage(message);
//                    //wifi登陆地址
//                    URL url = new URL("http://202.202.96.57:9060/login/login1.jsp");
//                    connection = (HttpURLConnection) url.openConnection();
//                    connection.setRequestMethod("POST");
//                    connection.setConnectTimeout(TIMEOUT);
//                    connection.setReadTimeout(TIMEOUT);
//                    DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
////
//                    String parameter = String.format("username=%s&password=%s&if_login=&B2=", userName, password);
//                    outputStream.writeBytes(parameter);
//
//                    InputStream in = connection.getInputStream();
//                    BufferedReader reader = new BufferedReader(new InputStreamReader(in, "GBK"));
//                    StringBuilder response = new StringBuilder();
//                    String line;
//                    while ((line = reader.readLine()) != null)
//                    {
//                        response.append(line);
//                    }
//                    String responseText = response.toString();
//                    if (responseText != null)
//                    {
//                        Document document;
//                        document = Jsoup.parse(responseText);
//                        String loginResult = document.body().text();
//
//                        if (loginResult.indexOf("您已通过登录审核") >= 0)
//                        {
//                            if (loginResult.indexOf(userName) >= 0)
//                            {
//                                message.obj = "登陆成功";
//                                RememberPassword.setRememberPassword(userName, password, rememberPass, editor, preferences);
//
//                            } else
//                            {
//                                message.obj = "您已经使用另一个账号登陆";
//                                RememberPassword.setRememberPassword(userName, password, rememberPass, editor, preferences);
//
//                            }
//                        } else if (loginResult.indexOf("密码错误") >= 0)
//                        {
//                            message.obj = "密码或用户名错误";
//                            editor.putString(userName + "password", "");
//                            editor.commit();
//                        } else if (loginResult.indexOf("账号已在其他计算机上登录") >= 0)
//                        {
//                            message.obj = "账号已在其他地方登陆，请退出";
//                            RememberPassword.setRememberPassword(userName, password, rememberPass, editor, preferences);
//                        } else
//
//                        {
//                            message.obj = "未知错误";
//                        }
//                        handler.sendMessage(message);
//                    }
//                } catch (MalformedURLException e)
//                {
//                    e.printStackTrace();
//                } catch (ProtocolException e)
//                {
//                    e.printStackTrace();
//                } catch (IOException e)
//                {
//                    e.printStackTrace();
//                } finally
//                {
//                    if (connection != null)
//                    {
//                        connection.disconnect();
//                    }
//                }
//            }
//        }
//
//        ).start();
//    }

    private void initPopup()
    {
        mAdapter = new UserNameAdapter(this,
                R.layout.popuwindow_layout, userNameLists);
        mListView = (ListView)findViewById(R.id.popuwindow_userName);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(MainActivity.this);
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        int width = editTextUserName.getWidth();
        View contentView= LayoutInflater.from(this).inflate(R.layout.popuwindow_layout,null);
        popupWindowUsername = new PopupWindow(contentView, width, height, true);
        popupWindowUsername.setOutsideTouchable(true);
        popupWindowUsername.setBackgroundDrawable(new ColorDrawable(Color.argb(50, 52, 53, 55)));
        popupWindowUsername.setOnDismissListener(MainActivity.this);
    }

    @Override
    public void onDismiss()
    {
        mShowing = false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        editTextUserName.setText(userNameLists.get(position).getName());
        editTextPassword.setText(preferences.getString(userNameLists.get(position).getName() + "password", ""));
        popupWindowUsername.dismiss();
    }
}
