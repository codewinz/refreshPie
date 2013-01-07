package com.ztoday21.refreshPie;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

public class service_main extends Service implements OnTouchListener {
	
	// �� ����
	public static int		_interval = 0;
	public static int		_timeInterval = 0;
	//public static boolean 	_restart = false;
	
	// ���� ��
	public TextView		_tv = null;		
	public Intent		_refreshIntent = null;
	
	public InputMethodManager _ime = null;

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
			PackageManager pm = getPackageManager();
			_refreshIntent = pm.getLaunchIntentForPackage("com.nextpapyrus.Refresh2");
			
			if(null != _refreshIntent)
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
				
				// ime check
				_ime = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				 
				
				Toast.makeText(this, "���� ���۵�", Toast.LENGTH_SHORT).show();
			}
			else
			{
				// ���� ���� ����
				stopSelf(startId);		
				Toast.makeText(this, "���� ���� ���� Refresh2 ����", Toast.LENGTH_SHORT).show();
			}
		}
		else
		{
			//if( false ==_restart )
			{
				stopSelf(startId);
				Toast.makeText(this, "����� ��û ���� ����� ����", Toast.LENGTH_SHORT).show();
			}
		}
		
		return Service.START_NOT_STICKY;
	}
	
	//---------------------------------
	public int _touchCnt;

	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		// �̺�Ʈ�� ���� ��� Ȯ�� �ʿ� 
		// �� ���� �˾ƾ� �Ұ� ���� �... -_-;;;
		
		//if( MotionEvent.ACTION_DOWN == event.getActionMasked() )
		{
			if( null != _ime )
			{
				// Ű���尡 ��Ȱ�� �϶��� 
				if( false == _ime.isAcceptingText() )
				{
					_touchCnt++;
				}
			}
			else
			{
				_touchCnt++;
			}
			
			if( service_main._interval <= _touchCnt )
			{
				// ��ġ �ʱ�ȭ
				_touchCnt = 0;
		
				// �������� ���� ����
				if(null != _refreshIntent)
				{
					try
					{
						Thread.sleep(service_main._timeInterval);
					}
					catch( InterruptedException e )
					{
						Toast.makeText(service_main.this, e.toString(), Toast.LENGTH_LONG).show();
					}
					
					startActivity(_refreshIntent);
				}
			}
		}
		
		return false;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}
