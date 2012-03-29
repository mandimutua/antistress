package com.minimaldevelop.antistress;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class AntiStressExerciseActivity extends Activity {

	private Handler mHandler = new Handler();
	private TextView mTimeLabel;
	private TextView mActionLabel;
	private TextView mActionPrepareLabel;
	private Button mStartButton;
	private Button mStopButton;
	private ProgressBar mProgressBar;
	private int tick = 10;
	private int progress = 0;
	private final int PROGRESSMAX = 1005;
	private final int SPEED = 500; //need to be 1000, other values only use for testing
	
	private enum ExerciseState {
		Breath3, Keep10, BreathIn3Serie, BreathOut3Serie
	};
	
	private ExerciseState exerciseState = ExerciseState.Breath3;	
	private int currentExerciseTry = 0;
	private int currentBreath3Serie = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main1);

		mTimeLabel = (TextView) findViewById(R.id.textView1);
		mActionLabel = (TextView) findViewById(R.id.textView2);
		mStartButton = (Button) findViewById(R.id.button1);
		mStopButton = (Button) findViewById(R.id.button2);
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar1);
		mActionPrepareLabel = (TextView) findViewById(R.id.textView4);
		
		mProgressBar.setMax(PROGRESSMAX);

		mStartButton.setOnClickListener(mStartListener);
		mStopButton.setOnClickListener(mStopListener);

		// Test place START
		Context context = this;
		AlarmManager mgr = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);

		Calendar now = Calendar.getInstance();
		int hour = now.get(Calendar.HOUR_OF_DAY);
		int minute = now.get(Calendar.MINUTE);

		// TODO: For future check SharedPreferences for settings and use that
		// instead of default

		int nextAlarmHour = 10;

		// default settings
		int addMinutes = 60 - minute;
		int addHours = 0;
		if (hour < 10) {
			addHours = 10 - hour;
			nextAlarmHour = 15;
		} else if (hour > 10 && hour < 15) {
			addHours = 15 - hour;
			nextAlarmHour = 20;
		} else if (hour > 15 && hour < 20) {
			addHours = 20 - hour;
			nextAlarmHour = 10;
		} else if (hour == 10 || hour == 15 || hour == 20) {
			addHours = 4;
			switch (hour) {
			case 10:
				nextAlarmHour = 15;
				break;
			case 15:
				nextAlarmHour = 20;
				break;
			case 20:
				nextAlarmHour = 10;
				break;
			}
		} else if (hour > 20) {
			addHours = 24 - hour + 10;
			nextAlarmHour = 10;
		}

		// convert hours and minutes to seconds
		long addSeconds = addHours * 60 * 60;
		addSeconds += addMinutes * 60;
		// convert to mseconds
		long addMiliSeconds = addSeconds * 1000;

		Intent i = new Intent(context, OnAlarmReceiver.class);
		i.putExtra("NextAlarmHour", nextAlarmHour);
		i.putExtra("CurrentMSforAlarm", addMiliSeconds);
		i.putExtra("CurrentHourforAlarm", addHours);
		i.putExtra("CurrentMinuteforAlarm", addMinutes);
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
		Log.d("AntiStressExerciseActivity", "Poslato");
		mgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				SystemClock.elapsedRealtime() + 10000, pi);
		// Test place END
	}

	private Runnable mUpdateTimeTask = new Runnable() {
		public void run() {
			

			updateActionText();
			mTimeLabel.setText("Elapsed time: " + tick);
			Log.i("mUpdateTimeTask", "Elapsed time: " + tick);
			tick--;

			if (tick >= 0) {
				mHandler.postDelayed(this, SPEED);
			} else {
				goToNextState();
				doExercise();
			}
		}
	};

	OnClickListener mStartListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
//			mStartTime = System.currentTimeMillis();
			mHandler.removeCallbacks(mUpdateTimeTask);
//			mHandler.postDelayed(mUpdateTimeTask, 100);
			doExercise();
		}
	};

	OnClickListener mStopListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			mHandler.removeCallbacks(mUpdateTimeTask);
		}
	};
	
	private void doExercise() {
		//sve puta 3
		Log.d("doExcercise", "State=" + exerciseState.toString());
		if (currentExerciseTry < 3) {
			setAndReturnTick();			

			mHandler.removeCallbacks(mUpdateTimeTask);
			mHandler.postDelayed(mUpdateTimeTask, SPEED);
			progress = progress + 15;
			mProgressBar.setProgress(progress); 
		} else {
			//It is over			
			//Reset 
			exerciseState = ExerciseState.Breath3;
			currentExerciseTry = 0;
			currentBreath3Serie = 1;	
			mActionLabel.setText(R.string.Finish);
			mProgressBar.setProgress(PROGRESSMAX); 
			mActionPrepareLabel.setText("");
		}	
	}
	
	private int setAndReturnTick() {
		if (exerciseState == ExerciseState.Keep10) {
			tick = 10;
		} else {
			tick = 3;
		}
		return tick;
	}
	
	private void updateActionText() {
		Log.d("Update Text", "UpdateText State=" + exerciseState.toString());
		if (exerciseState == ExerciseState.Breath3) {
			mActionLabel.setText(getString(R.string.Breath3, tick));
			mActionPrepareLabel.setText(getString(R.string.Keep10, 10));
		} else if (exerciseState == ExerciseState.Keep10) {
			mActionLabel.setText(getString(R.string.Keep10, tick));
			mActionPrepareLabel.setText(getString(R.string.BreathOut3Serie, 3));
		} else if (exerciseState == ExerciseState.BreathIn3Serie) {
			mActionLabel.setText(getString(R.string.BreathIn3Serie, tick, currentBreath3Serie,10));
			if (currentBreath3Serie < 10) {
				mActionPrepareLabel.setText(getString(R.string.BreathOut3Serie, 3));
			} else {	
				mActionPrepareLabel.setText(getString(R.string.Keep10, 10));
			}
		} else if (exerciseState == ExerciseState.BreathOut3Serie) {
			mActionLabel.setText(getString(R.string.BreathOut3Serie, tick));
			Log.d("updateActionText()", "currentExerciseTry=" + currentExerciseTry);
			if (currentExerciseTry < 3 ){
				int serie = 1;
				if (currentBreath3Serie > 10) {
					serie = 1;
				} else {
					serie = currentBreath3Serie;
				}
				mActionPrepareLabel.setText(getString(R.string.BreathIn3Serie, 3, serie, 10));
			}
			if (currentExerciseTry == 2 && currentBreath3Serie == 11){
				mActionPrepareLabel.setText(R.string.endOfExercise);
			}
		}
	}
	
	private void goToNextState() {
		if (exerciseState == ExerciseState.Breath3) {
			exerciseState = ExerciseState.Keep10;
		} else if (exerciseState == ExerciseState.Keep10) {
			exerciseState = ExerciseState.BreathOut3Serie;
		} else if (exerciseState == ExerciseState.BreathIn3Serie) {							
			currentBreath3Serie++;
			if (currentBreath3Serie < 11) {
				exerciseState = ExerciseState.BreathOut3Serie;
			} else {
				exerciseState = ExerciseState.Keep10;					
			}

		} else if (exerciseState == ExerciseState.BreathOut3Serie) {
			exerciseState = ExerciseState.BreathIn3Serie;
			if (currentBreath3Serie == 11) {
				currentBreath3Serie = 1;
				currentExerciseTry++;
			}
		}
	}

}