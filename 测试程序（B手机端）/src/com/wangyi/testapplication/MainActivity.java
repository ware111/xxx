package com.wangyi.testapplication;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {
	 Button send;
	 EditText edit;
	 String message;
	 TextView tv;
	 boolean b;
	 byte[] bb = new byte[4];
	 DatagramSocket socket;
	 static SQLiteDatabase db;
	 
	 Handler handler = new Handler(){
			public void handleMessage(android.os.Message msg) {
				if (msg.what == 0x123){
					message = msg.obj.toString();
					Log.v("11111", message);
				}
			}
		};
		
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);	
			setContentView(R.layout.activity_main);
			send = (Button)findViewById(R.id.send);
			edit = (EditText)findViewById(R.id.edit);
			tv = (TextView)findViewById(R.id.tv);
			send.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
				    new ReceiveThread().start();
				    edit.setText(message);
				    operationDB("333");
//				    Cursor cursor = db.query("message", null, null, null, null, null, null);
//					List<Map<String, String>> list = new ArrayList<Map<String, String>>();
//					while (cursor.moveToNext()){
//						Map<String, String> map = new HashMap<String, String>();
//						map.put("time", cursor.getString(0));
//						map.put("message", cursor.getString(1));
//						list.add(map);
//					}
//					Log.v("111", "--------------------------------------------------------------------");
//					Log.v("22", list.get(2).get("time"));
//					Log.v("111", "--------------------------------------------------------------------");
//				    if (edit.getText().toString().startsWith("Ìø×ª", 0)){
				    	startActivity(new Intent(MainActivity.this, ListMessageActivity.class));
//				    }
			    
				}
			});	
			
		}
		
		class ReceiveThread extends Thread{
			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				try {
					InetAddress address = InetAddress.getByName("192.168.1.11");
					socket = new DatagramSocket(9000, address);
					byte[] data = new byte[100];
					DatagramPacket pack = new DatagramPacket(data, data.length);
					socket.receive(pack);
					final String str = new String(data, 0, data.length);	
					Message msg = new Message();
					msg.what = 0x123;
					msg.obj = str;
					handler.sendMessage(msg);
					bb = address.getAddress();
					b = socket.isConnected();
					
					socket.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		class SendThread extends Thread{
			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				try {
					DatagramSocket socket = new DatagramSocket(9000);
					InetAddress address = InetAddress.getByName("192.168.191.2");
					String str = edit.getText().toString();
					byte[] data = str.getBytes("utf-8");
					DatagramPacket pack = new DatagramPacket(data, data.length, address, 9000);
					socket.send(pack);
//					byte[] data1 = new byte[100];
//					DatagramPacket pack1 = new DatagramPacket(data1, data1.length, address, 9000);
//					socket.receive(pack1);
//					string = new String(pack1.getData());
					//iiaddress = socket.getInetAddress();
					socket.close();
					//Log.v("1111111111111", socket.isConnected() + "");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		public void operationDB(String msg){
			String CREATE_TABLE = "create table IF NOT EXISTS message(time text, msg text)"; 
			SQLiteOpenHelper so = new MySqlite(this, "info.db", null, 1);
			SQLiteDatabase db = so.getWritableDatabase();
			MainActivity.db = db;
			db.execSQL(CREATE_TABLE);
			ContentValues values = new ContentValues();
		    SimpleDateFormat formate = (SimpleDateFormat) SimpleDateFormat.getDateTimeInstance();
			Date date = new Date(System.currentTimeMillis());
			String str = formate.format(date);
			values.put("time", str);
			values.put("msg", msg);
			db.insert("message", null, values);
			
		}
}
