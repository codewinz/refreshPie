package com.ztoday21.refreshPie;

import com.ztoday21.refreshPie.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class main extends Activity {
	
	public static String _saveName = "refreshPie";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
	    super.onCreate(savedInstanceState);
	
	    setContentView(R.layout.main);
	    
	    // ��ư ������ ���
	    findViewById(R.id.btStart).setOnClickListener(mClickListener);
	    findViewById(R.id.btStop).setOnClickListener(mClickListener);
	    findViewById(R.id.btBind).setOnClickListener(mClickListener);
	    findViewById(R.id.btUnbind).setOnClickListener(mClickListener);
	    findViewById(R.id.btExit).setOnClickListener(mClickListener);
	    
		// ��Ʈ�� �б�
		try
		{
			SharedPreferences prefs = getSharedPreferences(_saveName, MODE_PRIVATE);

			EditText etInterval = (EditText)findViewById(R.id.etInterval);
			etInterval.setText( prefs.getString("interval", "3") );
			
			EditText etTimeInterval = (EditText)findViewById(R.id.etTimeInterval);
			etTimeInterval.setText( prefs.getString("time_interval", "300") );
		}
		catch(Exception e) 
		{
			Toast.makeText(main.this, e.toString(), Toast.LENGTH_LONG).show();
		}

	}

	private Button.OnClickListener mClickListener = new View.OnClickListener()
	{
		public void onClick(View v)
		{
			switch(v.getId())
			{
			case R.id.btStart:
				{
					EditText etInterval = (EditText)findViewById(R.id.etInterval);
					service_main._interval = Integer.parseInt(etInterval.getText().toString());
					
					EditText etTimeInterval = (EditText)findViewById(R.id.etTimeInterval);
					service_main._timeInterval = Integer.parseInt(etTimeInterval.getText().toString());
					
					 Intent bindIntent = new Intent(main.this, service_main.class);
					 startService(bindIntent);
				}
				break;
					
			case R.id.btStop:
				{
					Intent bindIntent = new Intent(main.this, service_main.class);
	                stopService(bindIntent);
				}
				break;
					
			case R.id.btExit:
				{
					// ���� ����
					// ��Ʈ�� ����
					try
					{
						SharedPreferences prefs = getSharedPreferences(_saveName, MODE_PRIVATE);
						SharedPreferences.Editor ed = prefs.edit();

						EditText etInterval = (EditText)findViewById(R.id.etInterval);
						ed.putString("interval", etInterval.getText().toString());
						
						EditText etTimeInterval = (EditText)findViewById(R.id.etTimeInterval);
						ed.putString("time_interval", etTimeInterval.getText().toString());
						
						ed.commit();
					}
					catch(Exception e) 
					{
						Toast.makeText(main.this, e.toString(), Toast.LENGTH_LONG).show();
					}
					
					
					finish();
				}
				break;
			}
		}
	};
}
