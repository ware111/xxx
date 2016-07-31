package com.cvee.activity;
/**
 * 描述：此类为程序运行第一个加载的Activity，
 * 其主要作用是：
 * 1.控制其余Activity的切换
 * 2.建立存储数据的数据库和表
 */
import android.app.TabActivity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.TabHost;

import com.cvee.R;
import com.cvee.sqlite.Mysqlite;
import com.cvee.utils.Utils;
public class MainActivity extends TabActivity implements OnCheckedChangeListener
{
	public static TabHost mHost = null;	
	private Mysqlite mySqlite;
	@SuppressWarnings("deprecation")
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
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_maintabs);
		//建报警信息表
		setTable();
		
		mHost = getTabHost();		
		initRadios();
	}
	/**
	 * 描述：初始化底部按钮
	 */
	private void initRadios(){
			mHost.addTab(buildTabSpec("a0", "a", new Intent(this,
					HomeVideoActivity.class)));
			mHost.addTab(buildTabSpec("b0", "b", new Intent(this,
					NewsFindActivity.class)));
		((RadioButton) findViewById(R.id.radio_button0))
				.setOnCheckedChangeListener(this);
		((RadioButton) findViewById(R.id.radio_button1))
				.setOnCheckedChangeListener(this);
	}

	/**
	 * 描述：切换模块
	 */
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (isChecked) {
			switch (buttonView.getId()) {
			case R.id.radio_button0:
				mHost.setCurrentTabByTag("a0");
				break;
			case R.id.radio_button1:
				mHost.setCurrentTabByTag("b0");
				break;
			}
		}
	}

	private TabHost.TabSpec buildTabSpec(String tag, String resLabel,
			Intent content) {
		return mHost
				.newTabSpec(tag)
				.setIndicator(resLabel,
						getResources().getDrawable(R.drawable.ic_launcher))
				.setContent(content);
	}
    /**
	 * 描述：建表的方法
	 */
    public  void setTable(){
    	mySqlite=new Mysqlite(MainActivity.this, "users.db", 1);
    	String sql ="create table if not exists alerting_table(time varchar(10) ,news varchar(10))";
		SQLiteDatabase db=mySqlite.getWritableDatabase();
		db.execSQL(sql);
		db.close();
    }
    /**
     * 描述：退出程序
     */
	@Override
	public void finish()
	{
		Utils.showExitDialog(MainActivity.this);
	}

}
