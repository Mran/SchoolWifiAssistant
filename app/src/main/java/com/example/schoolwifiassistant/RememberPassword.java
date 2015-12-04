package com.example.schoolwifiassistant;

import android.content.SharedPreferences;
import android.widget.CheckBox;

/**
 * Created by 张孟尧 on 2015/12/3.
 */
public class RememberPassword
{
    public static void setRememberPassword(String userName, String password, CheckBox rememberPass, SharedPreferences.Editor editor, SharedPreferences preferences)

    {

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
    }
}
