package cs736.fullthrottle;

import java.util.GregorianCalendar;

import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		scheduleInitialAlarm(900);
	}
	
	
	private void scheduleInitialAlarm(int milliseconds){
		Long time = new GregorianCalendar().getTimeInMillis() + milliseconds;
		Intent intentAlarm = new Intent(MainActivity.this, AlarmReceiver.class);
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		alarmManager.set(AlarmManager.RTC_WAKEUP, time, PendingIntent.getBroadcast(this, 1, intentAlarm, 0));
		Log.d("FullThrottleApp", "Initial Alarm Set!");
	}


}
