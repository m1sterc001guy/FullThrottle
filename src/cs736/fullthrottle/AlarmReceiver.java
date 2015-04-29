package cs736.fullthrottle;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {
	//never release the wakelock
	boolean running = false;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("FullThrottleApp", "Alarm Received!");
		noSleepBug(context);
		scheduleAlarm(context, intent);
	}
	
	private void scheduleAlarm(Context context, Intent intentAlarm){
		int millis = 10000;
		Long time = new GregorianCalendar().getTimeInMillis() + millis;
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.set(AlarmManager.RTC_WAKEUP, time, PendingIntent.getBroadcast(context, 1, intentAlarm, 0));
		Log.d("Alarm", "Alarm Set!");
	}
	
	private void noSleepBug(Context context){
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock w1 = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My partial wakelock");
		w1.acquire();
		if(!running){
			running = true;
			ForeverThread f = new ForeverThread();
			f.start();
			HttpRequest request = new HttpRequest();
			request.start();
		}
	}
	
	class HttpRequest extends Thread{
		
		public void run(){
			while(running){
				try{
					HttpClient httpClient = new DefaultHttpClient();
					HttpResponse response = httpClient.execute(new HttpGet("http://www.gamespot.com"));
					StatusLine statusLine = response.getStatusLine();
					if(statusLine.getStatusCode() == HttpStatus.SC_OK){
						ByteArrayOutputStream out = new ByteArrayOutputStream();
						response.getEntity().writeTo(out);
						String responseString = out.toString();
						out.close();
						Log.d("HTTP", responseString);
					}
					else{
						Log.e("HTTP", "Status returned was not 200");
					}
				}catch(Exception e){
					Log.e("HTTP", "Error occurred when executing http request");
				}
			}
		}
		
	}
	
	class ForeverThread extends Thread{
		
		
		public void run(){
			
			
			try{
				while(running){		
					ArrayList<HashMap<String, Long>> prevStats = getCPUStats();
					
					double currSeconds = System.currentTimeMillis() / 1000.0;
					while((System.currentTimeMillis() / 1000.0) - currSeconds < 10.0){
						Random rand = new Random();
						double value = rand.nextDouble() * rand.nextDouble();
						Log.d("ForeverThread", "Value: " + value);
					}
					
					ArrayList<HashMap<String, Long>> currStats = getCPUStats();
					
					//final double totalCpuPercentage = getCPUPercentage(prevStats, currStats, 0);
					//final double cpu1Percentage = getCPUPercentage(prevStats, currStats, 1);
					//final double cpu2Percentage = getCPUPercentage(prevStats, currStats, 2);
					//final double cpu3Percentage = getCPUPercentage(prevStats, currStats, 3);
					//final double cpu4Percentage = getCPUPercentage(prevStats, currStats, 4);
						
				}
			}catch(FileNotFoundException e){
				Log.e("OPEN FILE", "FILE NOT FOUND EXCEPTION");
			}catch(Exception e){
				e.printStackTrace();
			}
			

		}
		
		private double getCPUPercentage(ArrayList<HashMap<String, Long>> prevStats, ArrayList<HashMap<String, Long>> currStats, int cpuNumber){
			HashMap<String, Long> prevTotalStats = prevStats.get(cpuNumber);
			HashMap<String, Long> currTotalStats = currStats.get(cpuNumber);
			
			long prevIdle = prevTotalStats.get("idle") + prevTotalStats.get("iowait");
			long idle = currTotalStats.get("idle") + currTotalStats.get("iowait");
			long prevNonIdle = prevTotalStats.get("user") + prevTotalStats.get("nice") + prevTotalStats.get("system") + prevTotalStats.get("irq") + prevTotalStats.get("softirq") + prevTotalStats.get("steal");
			long nonIdle = currTotalStats.get("user") + currTotalStats.get("nice") + currTotalStats.get("system") + currTotalStats.get("irq") + currTotalStats.get("softirq") + currTotalStats.get("steal");
			long prevTotal = prevIdle + prevNonIdle;
			long total = idle + nonIdle;
			
			double cpuPercentage = ((total-prevTotal)-(idle-prevIdle))/(double)(total-prevTotal);
			return cpuPercentage;
		}
		
		private ArrayList<HashMap<String, Long>> getCPUStats() throws FileNotFoundException{
			File file = new File("/proc/stat");
			Scanner scan = new Scanner(file);
			ArrayList<HashMap<String, Long>> cpuStats = new ArrayList<HashMap<String, Long>>();
			
			int numLines = 5;
			int currLine = 0;
			while(currLine < numLines){
				String stringStats = scan.nextLine();
				//Log.d("LINE", stringStats);
				HashMap<String, Long> stats = new HashMap<String, Long>();
				Scanner lineScanner = new Scanner(stringStats);
				int i = 0;
				while(lineScanner.hasNext()){
					try{
						Long value = Long.parseLong(lineScanner.next());
						switch(i){
						case 0:
							stats.put("user", value);
							break;
						case 1:
							stats.put("nice", value);
							break;
						case 2:
							stats.put("system", value);
							break;
						case 3:
							stats.put("idle", value);
							break;
						case 4:
							stats.put("iowait", value);
							break;
						case 5:
							stats.put("irq", value);
							break;
						case 6:
							stats.put("softirq", value);
							break;
						case 7:
							stats.put("steal", value);
							break;
						case 8:
							stats.put("guest", value);
							break;
						case 9:
							stats.put("guest_nice", value);
							break;
						}
						i++;
					}catch(NumberFormatException e){
						//do nothing
					}
				}
				cpuStats.add(stats);
				currLine++;
			}	
			return cpuStats;
		}
	}

}
