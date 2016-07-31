package com.wangyi.testapplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ListMessageActivity extends Activity {
	ListView list;
	List<Map<String, String>> data;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	// TODO Auto-generated method stub
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.listactivity);
    	list = (ListView)findViewById(R.id.list);
    	SQLiteDatabase db = MainActivity.db;
    	final Cursor cursor = db.query("message", null, null, null, null, null, null);
    	data = new ArrayList<Map<String, String>>();
		while (cursor.moveToNext()){
			
			Map<String, String> map = new HashMap<String, String>();
			map.put("time", cursor.getString(0));
			map.put("message", cursor.getString(1));
			data.add(map);
		}
    	//String[] message = new String[100];
    	//int i = 0;
//    	List<Map<String, String>> data = new ArrayList<Map<String,String>>();
//    	while(cursor.moveToNext()){
//    		Map<String, String> map = new HashMap<String, String>();
//    		//message[i] = cursor.getString(1);
//    		//i++;
//    		map.put("time", cursor.getString(0));
//    		map.put("message", cursor.getString(1));
//    		data.add(map);
//    	}
    	
    	//ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.messagelist, message); 
        BaseAdapter adapter = new BaseAdapter() {
			
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				// TODO Auto-generated method stub
				//getResources().getLayout(id);
				 LayoutInflater layout = getLayoutInflater();
				if (convertView == null){
					convertView = layout.inflate(R.layout.activity_content, null);
				} 
			//	convertView = layout.inflate(R.layout.activity_content, null);
				//TextView time = (TextView) convertView.findViewById(R.id.time);
//				TextView message = (TextView)convertView.findViewById(R.id.message);
				TextView time = (TextView) convertView.findViewById(R.id.time);
				TextView message = (TextView)convertView.findViewById(R.id.message);
				if (position < data.size()){
					time.setText(data.get(position).get("time"));
					message.setText(data.get(position).get("message"));
					Log.v("33", time.getText().toString());
				Log.v("111", "--------------------------------------------------------------------");
				Log.v("22", data.get(position).get("time"));
				Log.v("111", "--------------------------------------------------------------------");
				}
				
				//time.setText(list.get(0).toString());
				//message.setText(list.get(0).get("message"));
				return convertView;
			}
			
			@Override
			public long getItemId(int position) {
				// TODO Auto-generated method stub
				return position;
			}
			
			@Override
			public Object getItem(int position) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public int getCount() {
				// TODO Auto-generated method stub
				return cursor.getCount();
			}
		};
    	list.setAdapter(adapter);
    }
}
