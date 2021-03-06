package com.ztoday21.refreshPie;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class service_main extends Service implements OnTouchListener {
	
	// 값 공유
	public static int		_interval = 0;
	public static int		_timeInterval = 0;
	public static boolean	_isRunning = false;
	
	// 내부 사용
	public TextView		_tv = null;		
	public Intent		_refreshIntent = null;

	SharedPreferences prefs;
	private ArrayList<String> classNames;

	@SuppressLint("HandlerLeak")
	public Handler _handler = new Handler() 
	{
		private void logToFile(String activityClassName) {
			final String filePath =Environment.getExternalStorageDirectory() + "/frontactivity.txt";


			try {
				@SuppressWarnings("resource")
				RandomAccessFile file = new RandomAccessFile(filePath, "rw");
				file.seek(file.length());
				file.writeBytes("\r\n" + activityClassName);
			}
			catch (IOException e) {
				Toast.makeText(service_main.this, e.toString(), Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}

		}

		public void handleMessage(Message msg)
		{

			ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

			// get the info from the currently running task
			List< ActivityManager.RunningTaskInfo > taskInfo = am.getRunningTasks(1);

			ComponentName componentInfo = taskInfo.get(0).topActivity;

			String frontActivityClassName =   componentInfo.getClassName();

			if (prefs.getBoolean(Setting.keyLogFrontActivityClassname, false)) {
				String log =   "class : " + frontActivityClassName;
//				Toast.makeText(service_main.this, log, Toast.LENGTH_LONG).show();
				logToFile(log);
			}

			//최상단 화면 Classname 필터링
			if (prefs.getBoolean(Setting.keyActivityFilter, false)) {
				if (classNames.indexOf(frontActivityClassName) == -1)
					return;
			}


			// 리프레시 어플 실행
			if(null != _refreshIntent)
			{
				startActivity(_refreshIntent);
			}
			else
			{
				// 크레마 샤인 1.2.10 버전은 여기로 들어온다.
				// 하지만 다른 어플은 구분 못함. 후훗
				try
				{
					Process process = Runtime.getRuntime().exec("/system/bin/epdblk 10");
					process.getInputStream().close();
				    process.getOutputStream().close();
				    process.getErrorStream().close();
				    process.waitFor();
				}
				catch(IOException e)
				{
					Toast.makeText(service_main.this, e.toString(), Toast.LENGTH_LONG).show();
					e.printStackTrace();
				}
				catch( InterruptedException e )
				{
					Toast.makeText(service_main.this, e.toString(), Toast.LENGTH_LONG).show();
					e.printStackTrace();
				}
			}
		}	
	};

	@Override
	public void onCreate() 
	{
        super.onCreate();

		prefs = getSharedPreferences(main._saveName, MODE_PRIVATE);

    }
 
	@Override
    public void onDestroy() 
    {
        super.onDestroy();
        Toast.makeText(this, "서비스 중지됨", Toast.LENGTH_SHORT).show();
        _isRunning = false;
        
        // window manager 에서 view 제거
        // 서비스가 멈춰도 이부분이 제거가 안되었음
        WindowManager winmgr = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        winmgr.removeView(_tv);
        
        // 후에 좀 더 정확한 서비스 기능 알아야 함
        
    }
    
	@Override
	public int onStartCommand(Intent intent, int flags, int startId )
	{
		if( 0 != (Service.START_FLAG_RETRY & flags) )
		{
			// 원하는 작업을 하자
			// 리프레시어플 찾기
			
			// 크레마 터치
			_refreshIntent = getPackageManager().getLaunchIntentForPackage("com.nextpapyrus.Refresh2");

			// 크레마 샤인 -- 이번 버전 부터는 사용 안함
			
			if(true == _isRunning)
			{
				Toast.makeText(this, "이미 서비스가 실행중입니다.", Toast.LENGTH_SHORT).show();
			}
			else
			{
				// text view 를 window manager 에 등록 후 touch event 연결 
				_tv = new TextView(this);
				_tv.setOnTouchListener(this);
				
				WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
				            WindowManager.LayoutParams.WRAP_CONTENT,
				            WindowManager.LayoutParams.WRAP_CONTENT,
				            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
				            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				                    | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
				            PixelFormat.TRANSLUCENT);
				
				WindowManager winmgr = (WindowManager)getSystemService(Context.WINDOW_SERVICE);

				winmgr.addView(_tv, lp);

				loadSetting();

				Toast.makeText(this, "서비스 시작됨", Toast.LENGTH_SHORT).show();
				_isRunning = true;
			}
		}
		
		return Service.START_STICKY;
	}

	@SuppressLint("NewApi")
	private void loadSetting() {
		Set<String> setClassNames = prefs.getStringSet(Setting.keyClassNames, new HashSet<String>());
		classNames = new ArrayList<String>(setClassNames);

		_timeInterval = Integer.parseInt(prefs.getString("time_interval", main.defaultTimeInterval));
		_interval = Integer.parseInt(prefs.getString("interval", main.defaultInterval));
	}

	public Intent getIntentByLabel(String pkg, String cls) {
		Intent i = new Intent();
		i.setComponent(new ComponentName(pkg, cls));
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TOP);
		return i;
	}
	
	//---------------------------------
	public int _touchCnt;

	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		{
			_touchCnt++;
			
			if( service_main._interval <= _touchCnt )
			{
				// 터치 초기화
				_touchCnt = 0;
				
				_handler.sendEmptyMessageDelayed(0, service_main._timeInterval);
			}
		}
		
		return false;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
