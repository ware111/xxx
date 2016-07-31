package com.cvee.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cvee.R;
import com.cvee.utils.Configuration;
import com.cvee.utils.Utils;
/**
 * 描述：登陆界面
 * @author Administrator
 *
 */
public class LoginActivity extends Activity {
	private EditText wipEdit;
	private EditText jipEdit;
	private EditText portEdit;
	private EditText nameEdit;
	private EditText passwordEdit;
	private Button loginButton;
	private SharedPreferences sharedPreferences;//存储用户信息
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
		.detectDiskReads()
		.detectDiskWrites()
		.detectNetwork()
		.penaltyLog()
		.build());
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
		.detectLeakedSqlLiteObjects()
		.penaltyLog()
		.penaltyDeath()
		.build());
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		if (!Utils.checkNet(LoginActivity.this)) {
			Utils.AlertNetError(LoginActivity.this);
			return;
		}else{
			initView();
		}
	}
	/*
	 * 描述：实例化控件 
	 */
	private void initView(){
		wipEdit = (EditText)findViewById(R.id.login_wipEdit);
		jipEdit = (EditText)findViewById(R.id.login_jipEdit);
		nameEdit = (EditText)findViewById(R.id.login_nameEdit);
		portEdit = (EditText)findViewById(R.id.login_portEdit);
		passwordEdit = (EditText)findViewById(R.id.login_passwordEdit);
		loginButton = (Button)findViewById(R.id.loginButton);
		//读取用户信息
		sharedPreferences = getSharedPreferences("user", MODE_WORLD_READABLE);
		Configuration.wip = sharedPreferences.getString("wip", "");
		Configuration.jip = sharedPreferences.getString("jip", "");
		Configuration.name = sharedPreferences.getString("name", "");
		Configuration.password = sharedPreferences.getString("password", "");
		Configuration.port = sharedPreferences.getString("port", "");
		wipEdit.setText(Configuration.wip);
		jipEdit.setText(Configuration.jip);
		nameEdit.setText(Configuration.name);
		passwordEdit.setText(Configuration.password);
		portEdit.setText(Configuration.port);
		
		loginButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String wip = wipEdit.getText().toString().trim();
				String jip = jipEdit.getText().toString().trim();
				String name = nameEdit.getText().toString().trim();
				String password = passwordEdit.getText().toString().trim();
				String port = portEdit.getText().toString().trim();
				if (wip.equals("")|jip.equals("")|name.equals("")|
						password.equals("")|port.equals("")) {
					Toast.makeText(LoginActivity.this, "请把信息填写完整！", Toast.LENGTH_SHORT).show();
				}else{
					Configuration.wip = wip;
					Configuration.jip = jip;
					Configuration.name = name;
					Configuration.password = password;
					Configuration.port = port;
					//保存用户信息
					Editor editor = sharedPreferences.edit();
					editor.putString("wip", Configuration.wip);
					editor.putString("jip", Configuration.jip);
					editor.putString("name", Configuration.name);
					editor.putString("password", Configuration.password);
					editor.putString("port", Configuration.port);
					editor.commit();
					Intent intent = new Intent(LoginActivity.this,MainActivity.class);
					startActivity(intent);
				}
			}
		});
	}
}
