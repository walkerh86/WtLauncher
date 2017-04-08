package com.cj.wtlauncher;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

public class ScreenSensorService extends Service{
	private String TAG = ScreenSensorService.class.getSimpleName();
	
	private final SensorEventListener mAccelerometerListener = new SensorEventListener(){
		public void onAccuracyChanged(Sensor paramAnonymousSensor, int paramAnonymousInt) {}
		
		public void onSensorChanged(SensorEvent sensorEvent){
			if (sensorEvent.sensor.getType() != Sensor.TYPE_ACCELEROMETER) {
		        return;
		    }
			float f = sensorEvent.values[2];
			if (f < 7.0F) {
		        ScreenSensorService.this.goToSleep(true);
		    }
		    if (f > 7.0F) {
		        ScreenSensorService.this.goToWake();
		    }
		    Log.i(ScreenSensorService.this.TAG, "z " + f);
		}
	};
	
	private Sensor mAccelerometerSensor;
	private final Handler mHandler = new Handler();
	private final SensorEventListener mPickUpSensorListener = new SensorEventListener(){
		public void onAccuracyChanged(Sensor paramAnonymousSensor, int paramAnonymousInt){}
		public void onSensorChanged(SensorEvent sensorEvent){
			if (sensorEvent.sensor.getType() != Sensor.TYPE_PICK_UP_GESTURE) {
				return;
			}
			float f = sensorEvent.values[0];
			if (f == 1.0F) {
				ScreenSensorService.this.initAccelerometer();
			}
			Log.i(ScreenSensorService.this.TAG, "mPickUpListener  p =" + f);
		}
	};
	
	private PowerManager mPowerManager;
	private SensorManager mSensorManager;
	private final SensorEventListener mStepCounterListener = new SensorEventListener(){
		public void onAccuracyChanged(Sensor paramAnonymousSensor, int paramAnonymousInt) {}
		public void onSensorChanged(SensorEvent sensorEvent){
			if (sensorEvent.sensor.getType() != Sensor.TYPE_STEP_COUNTER) {
				return;
			}
			float f = sensorEvent.values[0];
			if ((f > 0.0F) && (ScreenSensorService.this.value_sensor_step.equals("1"))) {
				ScreenSensorService.this.goToSleep(false);
			}
			Log.i(ScreenSensorService.this.TAG, "mStepCounterListener currentStepCount = " + f);
		}
	};
	
	private final SensorEventListener mTileSensorListener = new SensorEventListener(){
		public void onAccuracyChanged(Sensor paramAnonymousSensor, int paramAnonymousInt) {}
		public void onSensorChanged(SensorEvent sensorEvent){
			if (sensorEvent.sensor.getType() != Sensor.TYPE_TILT_DETECTOR) {
				return;
			}
			if (sensorEvent.values[0] == 1.0F) {
				ScreenSensorService.this.initAccelerometer();
			}
			Log.i(ScreenSensorService.this.TAG, "mTileSensorListener  s = " + sensorEvent.values[0]);
		}
	};
	
	private String value_sensor_step;
	private PowerManager getPowerManager(){
		if (this.mPowerManager == null) {
		     this.mPowerManager = ((PowerManager)getSystemService("power"));
		}
		return this.mPowerManager;
	}
	
	private SensorManager getSensorManger()
	  {
	    if (this.mSensorManager == null) {
	      this.mSensorManager = ((SensorManager)getSystemService("sensor"));
	    }
	    return this.mSensorManager;
	  }
	  
	  private void goToSleep(boolean paramBoolean)
	  {
	    if (paramBoolean) {
	      stopAccelerometer();
	    }
	    if (!WatchApp.getTopActivityStatus()) {
	      return;
	    }
	    getPowerManager().goToSleep(SystemClock.uptimeMillis());
	    PowerManager.WakeLock localWakeLock = this.mPowerManager.newWakeLock(6, "goToSleep");
	    localWakeLock.acquire();
	    localWakeLock.release();
	    Log.d(this.TAG, "goToSleep");
	  }
	  
	  private void goToWake()
	  {
	    stopAccelerometer();
	    PowerManager.WakeLock localWakeLock = getPowerManager().newWakeLock(268435466, "wake");
	    localWakeLock.acquire();
	    localWakeLock.release();
	    Log.d(this.TAG, "goToWake");
	  }
	  
	  private void initAccelerometer()
	  {
	    Sensor localSensor = getSensorManger().getDefaultSensor(1);
	    this.mSensorManager.registerListener(this.mAccelerometerListener, localSensor, 0);
	    Log.d(this.TAG, "initAccelerometer");
	  }
	  
	  private void initSleepSensor()
	  {
	    Sensor localSensor = getSensorManger().getDefaultSensor(22);
	    this.mSensorManager.registerListener(this.mTileSensorListener, localSensor, 0);
	    Log.d(this.TAG, "initSensorTiltDetector");
	  }
	  
	  private void stopAccelerometer()
	  {
	    this.mAccelerometerSensor = getSensorManger().getDefaultSensor(1);
	    this.mSensorManager.unregisterListener(this.mAccelerometerListener, this.mAccelerometerSensor);
	    Log.d(this.TAG, "stopAccelerometer");
	  }
	  
	  private void stopSleepSensor()
	  {
	    Sensor localSensor = getSensorManger().getDefaultSensor(22);
	    this.mSensorManager.unregisterListener(this.mTileSensorListener, localSensor);
	    Log.d(this.TAG, "stopSleepSensor");
	  }

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	public void onDestroy(){
		super.onDestroy();
	    stopSleepSensor();
	    Log.d(this.TAG, "onDestroy");
	  }
	
	public int onStartCommand(Intent paramIntent, int paramInt1, int paramInt2){	  
	    initSleepSensor();
	    Log.d(this.TAG, "onStartCommand");
	    return super.onStartCommand(paramIntent, paramInt1, paramInt2);
	  }
}