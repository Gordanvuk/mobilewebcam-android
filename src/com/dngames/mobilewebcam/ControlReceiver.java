/* Copyright 2012 Michael Haar

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package com.dngames.mobilewebcam;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.widget.Toast;

public class ControlReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
		SharedPreferences prefs = context.getSharedPreferences(MobileWebCam.SHARED_PREFS_NAME, 0);
		if(intent.getAction().equals("com.dngames.mobilewebcam.START"))
		{
			Start(context, prefs);
		}
		else if(intent.getAction().equals("com.dngames.mobilewebcam.STOP"))
		{
			Stop(context, prefs);
		}
    }

	public static void Start(Context context, SharedPreferences prefs)
	{
		SharedPreferences.Editor edit = prefs.edit();
		edit.putBoolean("mobilewebcam_enabled", true);
		edit.commit();

		String v = prefs.getString("camera_mode", "1");
		if(v.length() < 1 || v.length() > 9)
	        v = "1";
		switch(PhotoSettings.Mode.values()[Integer.parseInt(v)])
		{
		case HIDDEN:
		case BACKGROUND:
			{
				AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
				Intent i = new Intent(context, PhotoAlarmReceiver.class);
				PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, i, 0);
				alarmMgr.cancel(pendingIntent);
				Calendar time = Calendar.getInstance();
				time.setTimeInMillis(System.currentTimeMillis());
				time.add(Calendar.SECOND, 1);
				alarmMgr.set(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), pendingIntent);
			}
			break;
		case BROADCASTRECEIVER:
			CustomReceiverService.start(context);
            break;
		case MANUAL:
		case NORMAL:
		default:
			Intent i = new Intent(context, MobileWebCam.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			i.putExtra("command", "start");
			context.startActivity(i);
			break;
		}
	}

	public static void Stop(Context context, SharedPreferences prefs)
	{
		SharedPreferences.Editor edit = prefs.edit();
		edit.putBoolean("mobilewebcam_enabled", false);
		edit.commit();
		
		String v = prefs.getString("camera_mode", "1");
		if(v.length() < 1 || v.length() > 9)
	        v = "1";
		switch(PhotoSettings.Mode.values()[Integer.parseInt(v)])
		{
		case HIDDEN:
		case BACKGROUND:
			{
				AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
				Intent i = new Intent(context, PhotoAlarmReceiver.class);
				PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, i, 0);
				alarmMgr.cancel(pendingIntent);
				PhotoAlarmReceiver.StopNotification(context);
			}
			break;
		case BROADCASTRECEIVER:
			CustomReceiverService.stop(context);
			break;
		case MANUAL:
		case NORMAL:
		default:
			Intent i = new Intent(context, MobileWebCam.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			i.putExtra("command", "stop");
			context.startActivity(i);
			break;
		}
	}
}