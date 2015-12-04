package com.example.schoolwifiassistant;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by 张孟尧 on 2015/12/5.
 */
public class UserNameAdapter extends ArrayAdapter<UserNameList>
{

    private int resourceId;
    public UserNameAdapter(Context context, int resource,List<UserNameList> objects)
    {
        super(context, resource, objects);
        resourceId=resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        UserNameList userNameView=getItem(position);
        View view= LayoutInflater.from(getContext()).inflate(resourceId, null);
        TextView userName=(TextView)view.findViewById(R.id.popuwindow_userName);
        userName.setText(userNameView.getName());
        return view;
    }
}
