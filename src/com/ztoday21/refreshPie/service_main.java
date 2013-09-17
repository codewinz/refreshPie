package com.ztoday21.refreshPie;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
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
	
	// �� ����
	public static int		_interval = 0;
	public static int		_timeInterval = 0;
	public static boolean	_isRunning = false;
	
	// ���� ���
	public TextView		_tv = null;		
	public Intent		_refreshIntent = null;
	
	@SuppressLint("HandlerLeak")
	public Handler _handler = new Handler() 
	{
		public void handleMessage(Message msg)
		{
			// �������� ���� ����
			if(null != _refreshIntent)
			{
				startActivity(_refreshIntent);
			}
			else
			{
				// ũ���� ���� 1.2.10 ������ ����� ���´�.
				// ������ �ٸ� ������ ���� ����. ����
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
    }
 
	@Override
    public void onDestroy() 
    {
        super.onDestroy();
        Toast.makeText(this, "���� ������", Toast.LENGTH_SHORT).show();
        _isRunning = false;
        
        // window manager ���� view ����
        // ���񽺰� ���絵 �̺κ��� ���Ű� �ȵǾ���
        WindowManager winmgr = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        winmgr.removeView(_tv);
        
        // �Ŀ� �� �� ��Ȯ�� ���� ��� �˾ƾ� ��
        
    }
    
	@Override
	public int onStartCommand(Intent intent, int flags, int startId )
	{
		if( 0 != (Service.START_FLAG_RETRY & flags) )
		{
			// ���ϴ� �۾��� ����
			// �������þ��� ã��
			
			// ũ���� ��ġ
			_refreshIntent = getPackageManager().getLaunchIntentForPackage("com.nextpapyrus.Refresh2");

			// ũ���� ���� -- �̹� ���� ���ʹ� ��� ����
			
			if(true == _isRunning)
			{
				Toast.makeText(this, "�̹� ���񽺰� �������Դϴ�.", Toast.LENGTH_SHORT).show();
			}
			else
			{
				// text view �� window manager �� ��� �� touch event ���� 
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
				
				Toast.makeText(this, "���� ���۵�", Toast.LENGTH_SHORT).show();
				_isRunning = true;
			}
		}
		
		return Service.START_STICKY;
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
				// ��ġ �ʱ�ȭ
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
