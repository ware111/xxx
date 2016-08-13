package com.cvee.activity;
/**
* 描述：此类为摄像头操作核心类
* 作用：对摄像头（音视频）进行各种操作
* @author 
*/
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import net.reecam.IpCamera;
import net.reecam.IpCamera.PTZ_COMMAND;
import net.reecam.SimpleAudioTrack;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.format.Time;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.cvee.R;
import com.cvee.socket.UDPClientSocket;
import com.cvee.sqlite.Mysqlite;
import com.cvee.utils.SysApplication;
import com.cvee.utils.Utils;
import com.misc.objc.NSData;
import com.misc.objc.NSNotification;
import com.misc.objc.NSNotificationCenter;
import com.misc.objc.NSSelector;

public class HomeVideoActivity extends Activity implements OnTouchListener{
	//摄像头控制按钮
	private Button backButton;
	private Button upButton;
	private Button downButton;
	private Button leftButton;
	private Button rightButton;
	private Button rurnButton;
	private Button defendButton;//布防撤防按钮
	private TextView defendText;//显示布防状态
	
	//*****************************
	//ZDH
	public static TextView wsd;
	private ToggleButton queryWSD;
	private ToggleButton open;
	//*****************************
	
	private boolean isPlay = true;
	private IpCamera camera;
	Thread arthread = null, apthread = null;
	private boolean iscapturing = false;
	private Handler mChildHandler;
	private boolean isrecording = false;
	GestureDetector mGestureDetector;
	private Mysqlite mySqlite;
	/**
	 * 描述：手势监听类
	 * @author Administrator
	 *
	 */
	class MyGestureListener implements GestureDetector.OnGestureListener {
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float vX,
				float vY) {			
			float x, y;
			x = e1.getX() - e2.getX();
			y = e1.getY() - e2.getY();
			if ((y <= 50) && y >= -50) {
				if (x > 120) {
					// camera.set_flip(1);
					camera.ptz_control(PTZ_COMMAND.P_RIGHT);
					return true;
				} else if (x < -120) {
					camera.ptz_control(PTZ_COMMAND.P_LEFT);
					return true;
				}
			}

			if ((x <= 50) && x >= -50) {
				if (y > 120) {
					// camera.set_flip(1);
					camera.ptz_control(PTZ_COMMAND.T_DOWN);
					return true;
				} else if (y < -120) {
					camera.ptz_control(PTZ_COMMAND.T_UP);
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean onDown(MotionEvent e) {
			// TODO Auto-generated method stub
			return true;
		}

		@Override
		public void onLongPress(MotionEvent e) {
			  // TODO Auto-generated method stub
			 Log.e("type:", "Long Press!");
		}
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			// TODO Auto-generated method stub
			return true;
		}

		@Override
		public void onShowPress(MotionEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			// TODO Auto-generated method stub
			camera.ptz_control(PTZ_COMMAND.PT_STOP);
			return true;
		}
	}
	class MyView extends SurfaceView implements SurfaceHolder.Callback{
		SurfaceHolder holder;
		Camera photo;
		public MyView(Context context, Camera photo) {
			super(context);
		    this.photo = photo;
		    holder = getHolder();
		    holder.addCallback(this);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			// TODO Auto-generated method stub
			photo.setDisplayOrientation(90);
			try {
				photo.setPreviewDisplay(holder);
				photo.startPreview();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			// TODO Auto-generated method stub
			
		}
		
	}
	FrameLayout layout;
	Camera photo;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_homevideo);
		layout = (FrameLayout)findViewById(R.id.imageView1);
		photo = Camera.open();
		MyView view = new MyView(getApplication(), photo);
		layout.addView(view);
		initView();
		// 开启服务器 
        UDPServer server = new UDPServer();
        server.start();
        SysApplication exit = SysApplication.getInstance();
        exit.addActivity(this);
		try {
			camera = new IpCamera("",com.cvee.utils.Configuration.IPCAMERA_NAME ,
					com.cvee.utils.Configuration.jip,
					com.cvee.utils.Configuration.port,
					com.cvee.utils.Configuration.name,
					com.cvee.utils.Configuration.password,
					com.cvee.utils.Configuration.IPCAMERA_TIME);
		} catch (Exception e) {
			// TODO: handle exception
		}
			           
        // 音频控制按钮,控制音频播放
        ToggleButton tbtn = (ToggleButton) findViewById(R.id.tbn_audio);
        tbtn.setOnClickListener(new ToggleButton.OnClickListener()
        {

			@Override
			public void onClick(View v) {
				ToggleButton tbtn = (ToggleButton) findViewById(R.id.tbn_audio);
				if(tbtn.isChecked())
				{
					// 请求摄像机传输音频数据
					camera.play_audio();
					tbtn.setBackgroundResource(R.drawable.closevoie_selector);
				}
				else
				{
					// 请求摄像机停止传输音频数据
					camera.stop_audio();
					tbtn.setBackgroundResource(R.drawable.openvoie_selector);
				}
			}
        });
        
        // 谈话控制按钮,控制音频捕捉
        tbtn = (ToggleButton) findViewById(R.id.tbn_talk);
        tbtn.setOnClickListener(new ToggleButton.OnClickListener()
        {
			@Override
			public void onClick(View v) {
				ToggleButton tbtn = (ToggleButton) findViewById(R.id.tbn_talk);
				if(tbtn.isChecked())
				{
					// 告诉相机接收数据、捕捉说话
					camera.start_talk();
					tbtn.setBackgroundResource(R.drawable.closetalk_selector);
				}
				else
				{
					// 告诉相机停止接收数据、捕捉说话
					camera.stop_talk();
					tbtn.setBackgroundResource(R.drawable.opentalk_selector);
				}
			}
        	
        });
        
        // 录制按钮,控制音频和视频记录功能
        tbtn = (ToggleButton) findViewById(R.id.tbn_record);
        tbtn.setOnClickListener(new ToggleButton.OnClickListener()
        {

			@Override
			public void onClick(View v) {
				// 把录制的音频和视频保存到SD卡上
				ToggleButton tbtn = (ToggleButton) findViewById(R.id.tbn_record);
				if(tbtn.isChecked())
				{
					Time t=new Time();
					t.setToNow(); // 取得系统时间。
					String filename = String.format("/record%04d%02d%02d%02d%02d%02d.avi", 
							t.year, t.month + 1, t.monthDay, t.hour, t.minute, t.second); 
							
					if(android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)){
						camera.start_record(android.os.Environment.getExternalStorageDirectory()+ filename);
					}
						
					else{
						Toast.makeText(HomeVideoActivity.this, "请插入SD卡", Toast.LENGTH_SHORT).show();
					}
					tbtn.setBackgroundResource(R.drawable.closevideo_selector);
				}
				else
				{
					camera.stop_record();
					tbtn.setBackgroundResource(R.drawable.openvideo_selector);
				}
			}
        	
        });
        
        // 手势控制相机运动
        mGestureDetector = new GestureDetector(this, new MyGestureListener());
	}
	
	/**
	* 描述：接收视频状态发生改变的方法
	*/
	public void OnVideoStatusChanged(NSNotification note)
	{
		final TextView tv = (TextView) findViewById(R.id.video_status);
		final String status = note.userInfo().get("status").toString();
		tv.post(new Runnable() {
			
			@Override
			public void run() {
				tv.setText(status);
			}
		});
		
		if(status.equals("STOPPED"))
		{
			if(camera.started)
				camera.play_video();
		}
		else if(status.equals("PLAYING"))
		{
			
		}
	}
	
	/**
	* 描述：接收相机状态发生改变的方法
	*/
	public void OnCameraStatusChanged(NSNotification note)
	{
		final TextView tv = (TextView) findViewById(R.id.camera_status);
		final String status = note.userInfo().get("status").toString();
		tv.post(new Runnable() {
			
			@Override
			public void run() {
				tv.setText(status);
			}
		});
		
		if(status.equals("CONNECTED"))
		{
			camera.play_video();
		}else{
			
		}
	}
	
	/**
	* 描述：接收音频状态发生改变的方法
	*/
	public void OnAudioStatusChanged(NSNotification note)
	{			
		// TODO 
		IpCamera camera = (IpCamera)note.object();
		
		Log.e("AUDIO STATUS", camera.getHost() + ":" + note.userInfo().get("status").toString());
		String status = note.userInfo().get("status").toString();
		if(status.equals("PLAYING"))
		{
			startAudioPlay();			
//			tbtn.setChecked(true);
		}
		else if(status.equals("STOPPED"))
		{
			//tbtn.setChecked(false);
			stopAudioPlay();
		}
	}
	
	public void OnAudio(NSNotification note) {
		// 通知线程
		if(mChildHandler != null)
		{
			Message msg = mChildHandler.obtainMessage();
			msg.obj = note;
			mChildHandler.sendMessage(msg);
		}
	}
	
	/**
	* 描述：视频数据处理
	*/
	public void OnImageChanged(NSNotification note)
	{
		if(!((IpCamera)note.object()).equals(camera))
		{
			return;
		}
		
		// 解码并显示数据
		NSData data = (NSData)note.userInfo().get("data");
		final Bitmap bitmap;
		try
		{
			bitmap = BitmapFactory.decodeByteArray(data.bytes(), 0, data.length());
		}
		catch(OutOfMemoryError e)
		{
			NSNotificationCenter nc = NSNotificationCenter.defaultCenter();
	        nc.removeObserver(this, IpCamera.IPCamera_Image_Notification, camera);
	        nc.addObserver(this, new NSSelector("OnImageChanged"), IpCamera.IPCamera_Image_Notification, camera);
			System.gc();
			return;
		}
		
		//启动线程显示视频数据
		final ImageView lv = (ImageView)findViewById(R.id.imageView1);
		lv.post(new Runnable()
		{
			public void run()
			{
				lv.setImageBitmap(bitmap);
				//bitmap.recycle();
			}
		}
		);
	}

	public void OnTalkStatusChanged(NSNotification note)
	{
		String status = note.userInfo().get("status").toString();
		Log.e("Talk Status", camera.getHost() + " " + status);
		if(status.equals("PLAYING"))
		{
			//tbtn.setChecked(true);
			beginRecord();
		}
		else if(status.equals("STOPPED"))
		{
			//tbtn.setChecked(false);
			this.stopRecording();
		}
	}
	

	
	protected void stopRecording()
	{
		isrecording = false;
		arthread = null;
	}
	
	protected void beginRecord()
	{
		if(isrecording)
			return;
		isrecording = true;
		
		arthread = new Thread()
		{
			public void run() {
				int frequency = 8000;
				int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
				int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

				try {					
					// 创建一个新的AudioRecord对象记录音频。
					int bufferSize = AudioRecord.getMinBufferSize(frequency,
							channelConfiguration, audioEncoding) * 2;
					AudioRecord audioRecord = new AudioRecord(
							MediaRecorder.AudioSource.MIC, frequency,
							channelConfiguration, audioEncoding, bufferSize);

					byte[] buffer = new byte[IpCamera.AUDIO_BUFFER_SIZE];
					audioRecord.startRecording();

					Log.e("AudioTrack", "Begin Recording");
					while (isrecording) {
						int bufferReadResult = audioRecord.read(buffer, 0,
								IpCamera.AUDIO_BUFFER_SIZE);						
						camera.talk(buffer, bufferReadResult);
					}

					audioRecord.stop();
					Log.e("AudioTrack", "Stop Recording");

				} catch (Throwable t) {
					Log.e("AudioRecord", "Recording Failed");
				}
			}
		};
		
		arthread.start();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		NSNotificationCenter nc = NSNotificationCenter.defaultCenter();	
		nc.removeObserver(this);

		camera.stop_record();
		camera.stop_video();
		camera.stop_audio();
		camera.stop_talk();		
	}

	@Override
	protected void onResume() {
		super.onResume();
		NSNotificationCenter nc = NSNotificationCenter.defaultCenter();
		nc.addObserver(this, new NSSelector("OnCameraStatusChanged"), IpCamera.IPCamera_CameraStatusChanged_Notification, camera);	
		nc.addObserver(this, new NSSelector("OnVideoStatusChanged"), IpCamera.IPCamera_VideoStatusChanged_Notification, camera);
		nc.addObserver(this, new NSSelector("OnAudioStatusChanged"), IpCamera.IPCamera_AudioStatusChanged_Notification, null);
		nc.addObserver(this, new NSSelector("OnTalkStatusChanged"), IpCamera.IPCamera_TalkStatusChanged_Notification, camera);
		nc.addObserver(this, new NSSelector("OnImageChanged"), IpCamera.IPCamera_Image_Notification, camera);
		TextView tv = (TextView) findViewById(R.id.camera_status);
		String status = camera.camera_status.toString();
		tv.setText(status);
		tv = (TextView) findViewById(R.id.video_status);
		status = camera.video_status.toString();
		tv.setText(status);
		// 恢复相机状态
		if(!camera.started){
			camera.start();	
			isPlay = true;
			rurnButton.setBackgroundResource(R.drawable.pause_selector);
		}
			
		
		status = camera.camera_status.toString();
		if(status.equals("CONNECTED"))
		{
			camera.play_video();
		}
	}
	
	/**
	* 启动线程并向相机发送请求音视频信息
	*/
	protected void beginCapture()
	{
		if(iscapturing)
			return;
		iscapturing = true;
		
		arthread = new Thread()
		{			
			public void run() {
				int frequency = 8000;
				int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
				int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

				try {					
					// 创建一个新的AudioRecord对象记录音频。
					int bufferSize = AudioRecord.getMinBufferSize(frequency,
							channelConfiguration, audioEncoding) * 2;
					AudioRecord audioRecord = new AudioRecord(
							MediaRecorder.AudioSource.MIC, frequency,
							channelConfiguration, audioEncoding, bufferSize);

					byte[] buffer = new byte[IpCamera.AUDIO_BUFFER_SIZE];
					audioRecord.startRecording();

					Log.e("AudioTrack", "Begin Recording");
					while (iscapturing) {
						int bufferReadResult = audioRecord.read(buffer, 0,
								IpCamera.AUDIO_BUFFER_SIZE);
						
						camera.talk(buffer, bufferReadResult);
					}

					audioRecord.stop();
					Log.e("AudioTrack", "Stop Recording");

				} catch (Throwable t) {
					Log.e("AudioRecord", "Recording Failed");
				}
			}
		};
		
		arthread.start();
	}
	
	/**
	* 描述：停止音频捕捉
	*/
	protected void stopCapturing()
	{
		iscapturing = false;		
		arthread = null;
	}
	
	/**
	* 描述：播放音频
	*/
	protected void startAudioPlay()
	{		
		apthread = new Thread(new Runnable() {
			
			public void run() {
				final SimpleAudioTrack audioTrack;
				
				try {
					// 创建一个新的AudioTrack对象使用相同的参数作为AudioRecord
					audioTrack = new SimpleAudioTrack(8000,
							AudioFormat.CHANNEL_CONFIGURATION_MONO,
							AudioFormat.ENCODING_PCM_16BIT);
					// 开始回放
					audioTrack.init();
					Log.e("AudioTrack", "Ready");
					// 把音频缓冲到AudioTrack对象

				} catch (Throwable t) {
					Log.e("AudioTrack", "Playback Failed");
					return;
				}
				
				Looper.prepare();
				mChildHandler = new Handler() {
					public void handleMessage(Message msg) {
						NSNotification note = (NSNotification) msg.obj;
						if(note == null)
						{
							// 请求停止
							mChildHandler = null;
							Log.e("AudioTrack", "Playback Quit");
							Looper.myLooper().quit();
						}
						else
						{
							// 播放音频数据
							int play_time = (Integer) note.userInfo().get("tick");
							int now_time = IpCamera.times(null); 
							if((play_time - now_time) < -10)
							{
								// drop delayed packet
								//Log.e("AudioTrack", "Drop delayed packet " + (now_time - get_time));
								return;
							}
							NSData data = (NSData) note.userInfo().get("data");
							audioTrack.playAudioTrack(data.bytes(), 0, data.length());
						}
					}
				};

				Looper.loop();
			}
		});
		apthread.start();
		NSNotificationCenter nc = NSNotificationCenter.defaultCenter();
		nc.addObserver(this, new NSSelector("OnAudio"),
				IpCamera.IPCamera_Audio_Notification, camera);
	}
	
	/**
	* 描述：停止音频
	*/
	protected void stopAudioPlay()
	{
		NSNotificationCenter nc = NSNotificationCenter.defaultCenter();
		nc.removeObserver(this, IpCamera.IPCamera_Audio_Notification, camera);
		if(mChildHandler != null)
		{
			Message msg = mChildHandler.obtainMessage();
			msg.obj = null;
			mChildHandler.sendMessage(msg);
		}
		
		apthread = null;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
	  return mGestureDetector.onTouchEvent(event);
	}		
	/** 
	 *描述： 屏幕和键盘状态处理
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    // 检测屏幕的方向：纵向或横向
		super.onConfigurationChanged(newConfig);
	    if (this.getResources().getConfiguration().orientation 

	            == Configuration.ORIENTATION_LANDSCAPE) {

	        //当前为横屏， 在此处添加额外的处理代码

	    }

	    else if (this.getResources().getConfiguration().orientation 

	            == Configuration.ORIENTATION_PORTRAIT) {

	        //当前为竖屏， 在此处添加额外的处理代码

	    }
	    //检测实体键盘的状态：推出或者合上

	    if (newConfig.hardKeyboardHidden 

	            == Configuration.HARDKEYBOARDHIDDEN_NO){ 

	        //实体键盘处于推出状态，在此处添加额外的处理代码

	    	
	    } 

	    else if (newConfig.hardKeyboardHidden

	            == Configuration.HARDKEYBOARDHIDDEN_YES){ 

	        //实体键盘处于合上状态，在此处添加额外的处理代码

	    }
	}
	/**
	 * 描述：实例化组件
	 */
	private void initView(){
		upButton = (Button)findViewById(R.id.camera_upButton);
		downButton = (Button)findViewById(R.id.camera_downButton);
		leftButton = (Button)findViewById(R.id.camera_leftButton);
		rightButton = (Button)findViewById(R.id.camera_rightButton);
		rurnButton = (Button)findViewById(R.id.camera_turnButton);
		defendButton = (Button)findViewById(R.id.homevideo_defendButton);
		defendText = (TextView)findViewById(R.id.homevideo_defendText);
		//wsd =(TextView)findViewById(R.id.wsd_TextView);
		backButton = (Button)findViewById(R.id.homevideo_backButton);
		backButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
			   Utils.showExitDialog(HomeVideoActivity.this);
			}
		});	
		//*****************************
		//ZDH
		queryWSD = (ToggleButton)findViewById(R.id.queryWSD);
		queryWSD.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(queryWSD.isChecked()){
					wsd.setVisibility(View.INVISIBLE);
				}else{
					wsd.setVisibility(View.VISIBLE);
					
				}
			}
			
		});
		
		///在这里！！！！！！！！！！！！！！！！！
		open = (ToggleButton)findViewById(R.id.open);
		open.setOnClickListener(new OnClickListener(){
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(open.isChecked()){
					//向终端发送打开报警设备
					UDPClientSocket.send("OPEN", com.cvee.utils.Configuration.ZD_IP, com.cvee.utils.Configuration.ZD_PORT);
				}else{
					//向终端发送关闭报警设备
					UDPClientSocket.send("CLOSE", com.cvee.utils.Configuration.ZD_IP, com.cvee.utils.Configuration.ZD_PORT);
				}
			}
			
		});
		//*****************************
				//ZDH
		upButton.setOnTouchListener(this);
		downButton.setOnTouchListener(this);
		leftButton.setOnTouchListener(this);
		rightButton.setOnTouchListener(this);
		rurnButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (isPlay) {
					camera.stop();
					isPlay = false;
					rurnButton.setBackgroundResource(R.drawable.play_selector);
				}else{
					camera.start();
					isPlay = true;
					rurnButton.setBackgroundResource(R.drawable.pause_selector);
				}
					
					
			}
		});
		defendButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (com.cvee.utils.Configuration.isDefend) {
					defendButton.setBackgroundResource(R.drawable.bufang_selector);
					com.cvee.utils.Configuration.isDefend = false;
					defendText.setText("已撤防");
					UDPClientSocket.send("isDefend=false", 
							com.cvee.utils.Configuration.ZD_IP, 
							com.cvee.utils.Configuration.ZD_PORT);
					
				}else{
					defendButton.setBackgroundResource(R.drawable.chefang_selector);
					com.cvee.utils.Configuration.isDefend = true;
					defendText.setText("已布防");
					UDPClientSocket.send("isDefend=true",
							com.cvee.utils.Configuration.ZD_IP, 
							com.cvee.utils.Configuration.ZD_PORT);
				}
			}
		});
	} 

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (v.getId() == upButton.getId()) {
				camera.ptz_control(PTZ_COMMAND.T_DOWN);
			}
			 if (v.getId() == downButton.getId()) {
				camera.ptz_control(PTZ_COMMAND.T_UP);
			}
			else if (v.getId() == leftButton.getId()) {
				camera.ptz_control(PTZ_COMMAND.P_RIGHT);
			}
			else if (v.getId() == rightButton.getId()) {
				camera.ptz_control(PTZ_COMMAND.P_LEFT);
			}
		}
		else if (event.getAction() == MotionEvent.ACTION_UP) {
			camera.ptz_control(PTZ_COMMAND.PT_STOP);
		}
		return false;
	}  
    /**
     * 描述：向数据库写报警信息
     */
    public void writeNewsToSql(String strNews){
    	mySqlite=new Mysqlite(HomeVideoActivity.this, "users.db", 1);
    	SQLiteDatabase db=mySqlite.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("time", Utils.getTime());
		values.put("news", strNews);
		db.insert("alerting_table", null, values);
		db.close();
    }
    /**
     * 描述：报警提示对话框
     */
    public void setDialog(String coutent,final String delectOrder){
		final Builder builder = new AlertDialog.Builder(HomeVideoActivity.this);		
		builder.setTitle("报警提示");
		builder.setMessage(coutent+"正在报警……");
		builder.setPositiveButton("关闭报警器", new AlertDialog.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				//向终端发送关闭报警设备
				UDPClientSocket.send(delectOrder, com.cvee.utils.Configuration.ZD_IP, com.cvee.utils.Configuration.ZD_PORT);
			}
		});
		builder.create().show();
	}
	/**
	 * 与终端或应用中心平台（仿真机）进行通信
	 */
    /**
     * 描述：UDP通信服务类
     * 主要作用：
     * 1.接收平板或应用中心平台（仿真机）传过来的数据并进行相应处理
     * @author Administrator
     *
     */
    class UDPServer extends Thread {
    	  
        private static final int PORT = 8080;
      
        private byte[] msg = new byte[100]; 
      
        private boolean life = true;      
        public UDPServer() {
        	
        }
      
        /** 
         * @return the life 
         */
        public boolean isLife() {
            return life; 
        } 
      
        /** 
         * @param life 
         *            the life to set 
         */
        public void setLife(boolean life) { 
            this.life = life; 
        } 
      
        @Override
        public void run() {
            DatagramSocket dSocket = null; 
            
            try { 
                dSocket = new DatagramSocket(PORT); 
                while (life) { 
                    try {
                    	DatagramPacket dPacket = new DatagramPacket(msg, msg.length);
                    	dSocket.setSoTimeout(1000);
                        dSocket.receive(dPacket); 
                        String str = new String(msg,0,dPacket.getLength());
                        Message msg = new Message();
    					msg.what = 0x123;
    					msg.obj = str;
    					UDPhandler.sendMessage(msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } 
                } 
            } catch (SocketException e) {
                e.printStackTrace();
            } 
        }

    }
    Handler UDPhandler = new Handler() {

        public void handleMessage(Message msg) {
        	if (msg.what == 0x123) {
        		String msgstr = msg.obj.toString();
        		if (msgstr.equals("isDefend=false")) {
        			defendButton.setBackgroundResource(R.drawable.bufang_selector);
        			com.cvee.utils.Configuration.isDefend = false;
        			defendText.setText("已撤防");
        			Toast.makeText(HomeVideoActivity.this, "进入撤防状态", Toast.LENGTH_SHORT).show();
				}
        		else if (msgstr.equals("isDefend=true")) {
        			defendButton.setBackgroundResource(R.drawable.chefang_selector);
        			com.cvee.utils.Configuration.isDefend = true;
        			defendText.setText("已布防");
        			Toast.makeText(HomeVideoActivity.this, "进入布防状态", Toast.LENGTH_SHORT).show();
				}
        		//-----------------------------注意的代码----------------------------------------
        		//无线报警按钮
        		else if (msgstr.equals("OPEN")) {
        			writeNewsToSql("无线报警按钮");
        			setDialog("无线报警按钮","CLOSE");
				}
        		//*****************************
        		//ZDH接收终端
        		////温度：25.6    湿度：25.7
        		else if(msgstr.contains("温度")){
        			wsd.setText(msgstr);
        		}
        		//-----------------------------注意的代码----------------------------------------
			}
        }      
    };
}