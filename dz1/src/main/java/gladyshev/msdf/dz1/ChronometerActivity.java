package gladyshev.msdf.dz1;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Consumer;

public class ChronometerActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chronometer);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) ->
        {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setup();
    }

    private void setup()
    {
        TimerManager.loadState(this, new HashMap<Integer, Consumer<Integer>>()
        {{
            for(int i = 1; i<TimerManager.DEFAULT_TIMER_COUNT; i++)
            {
                put(i, createTextViewUpdater(getTextViewId(i), ChronometerActivity.this));
            }
        }});
        setupViewsStates();
    }

    private int getTimerId(int switchId) throws IllegalArgumentException
    {
        if(Arrays.asList(new Integer[] { R.id.first_on_off_switch, R.id.first_pause_switch, R.id.first_timer_tv } ).contains(switchId)) return 1;
        if(Arrays.asList(new Integer[] { R.id.second_on_off_switch, R.id.second_pause_switch, R.id.second_timer_tv } ).contains(switchId)) return 2;
        if(Arrays.asList(new Integer[] { R.id.third_on_off_switch, R.id.third_pause_switch, R.id.third_timer_tv } ).contains(switchId)) return 3;
        if(Arrays.asList(new Integer[] { R.id.fourth_on_off_switch, R.id.fourth_pause_switch, R.id.fourth_timer_tv } ).contains(switchId)) return 4;
        if(Arrays.asList(new Integer[] { R.id.fifth_on_off_switch, R.id.fifth_pause_switch, R.id.fifth_timer_tv } ).contains(switchId)) return 5;
        throw new IllegalArgumentException("Некорректный идентификатор переключателя!");
    }

    private int getPauseSwitchId(int timerId) throws IllegalArgumentException
    {
        switch(timerId)
        {
            case 1: return R.id.first_pause_switch;
            case 2: return R.id.second_pause_switch;
            case 3: return R.id.third_pause_switch;
            case 4: return R.id.fourth_pause_switch;
            case 5: return R.id.fifth_pause_switch;
            default: throw new IllegalArgumentException("Неверный идентификатор таймера!");
        }
    }

    private int getTextViewId(int timerId) throws IllegalArgumentException
    {
        switch(timerId)
        {
            case 1: return R.id.first_timer_tv;
            case 2: return R.id.second_timer_tv;
            case 3: return R.id.third_timer_tv;
            case 4: return R.id.fourth_timer_tv;
            case 5: return R.id.fifth_timer_tv;
            default: throw new IllegalArgumentException("Неверный идентификатор таймера!");
        }
    }

    public static Consumer<Integer> createTextViewUpdater(int textViewId, final android.app.Activity activity)
    {
        return value ->
        {
            int hours = value / 3600;
            int minutes = (value % 3600) / 60;
            int seconds = value % 60;
            String formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);
            TextView textView = activity.findViewById(textViewId);
            if (textView != null) textView.setText(formattedTime);

        };
    }

    private final CompoundButton.OnCheckedChangeListener switchesHandler = (compoundButton, b) ->
    {
        int timerId = getTimerId(compoundButton.getId());
        if(Arrays.asList(new Integer[] { R.id.first_on_off_switch, R.id.second_on_off_switch, R.id.third_on_off_switch, R.id.fourth_on_off_switch, R.id.fifth_on_off_switch } ).contains(compoundButton.getId()))
        {
            if(b) TimerManager.startTimer(timerId, createTextViewUpdater(getTextViewId(timerId), this));
            else TimerManager.stopTimer(timerId);
            Switch pauseSwitch = findViewById(getPauseSwitchId(timerId));
            pauseSwitch.setOnCheckedChangeListener(null);
            pauseSwitch.setEnabled(b);
            pauseSwitch.setChecked(false);
            pauseSwitch.setOnCheckedChangeListener(this.switchesHandler);
            createTextViewUpdater(getTextViewId(timerId), this).accept(TimerManager.timers.get(timerId).value);
        }
        else
        {
            if(b) TimerManager.pauseTimer(timerId);
            else TimerManager.startTimer(timerId, createTextViewUpdater(getTextViewId(timerId), this));
        }
        Log.d("TIMER_TEST", TimerManager.timers.get(timerId).isActive + " " + TimerManager.timers.get(timerId).isPaused);
    };

    private void setupViewsStates()
    {
        final int[] onOffSwitchesIDs = { R.id.first_on_off_switch,
                R.id.second_on_off_switch,
                R.id.third_on_off_switch,
                R.id.fourth_on_off_switch,
                R.id.fifth_on_off_switch };
        final int[] pauseSwitchesIDs = { R.id.first_pause_switch,
                R.id.second_pause_switch,
                R.id.third_pause_switch,
                R.id.fourth_pause_switch,
                R.id.fifth_pause_switch };
        final int[] tvIDs = { R.id.first_timer_tv,
                R.id.second_timer_tv,
                R.id.third_timer_tv,
                R.id.fourth_timer_tv,
                R.id.fifth_timer_tv };
        for(int id : onOffSwitchesIDs)
        {
            Switch sw = (Switch) findViewById(id);
            sw.setOnCheckedChangeListener(null);
            if(TimerManager.timers.get(getTimerId(id)).isActive || TimerManager.timers.get(getTimerId(id)).isPaused) sw.setChecked(true);
            else sw.setChecked(false);
            sw.setOnCheckedChangeListener(switchesHandler);
        }
        for(int id : pauseSwitchesIDs)
        {
            Switch sw = (Switch) findViewById(id);
            sw.setOnCheckedChangeListener(null);
            if(TimerManager.timers.get(getTimerId(id)).isPaused) sw.setChecked(true);
            else sw.setChecked(false);
            if(!TimerManager.timers.get(getTimerId(id)).isActive && !TimerManager.timers.get(getTimerId(id)).isPaused) sw.setEnabled(false);
            else sw.setEnabled(true);
            sw.setOnCheckedChangeListener(switchesHandler);
        }
        for(int id : tvIDs)
        {
            createTextViewUpdater(id, this).accept(TimerManager.timers.get(getTimerId(id)).value);
        }
    }

    private BroadcastReceiver serviceStoppedReceiver;

    @Override
    protected void onStart()
    {
        Log.d("TIMER_TEST", "onStart()");
        super.onStart();
        if (serviceStoppedReceiver == null)
        {
            serviceStoppedReceiver = new BroadcastReceiver()
            {
                @Override
                public void onReceive(Context context, Intent intent)
                {
                    setup();
                }
            };
            IntentFilter filter = new IntentFilter("SERVICE_STOPPED");
            registerReceiver(serviceStoppedReceiver, filter);
        }

        Intent serviceIntent = new Intent(this, ChronometerService.class);
        stopService(serviceIntent);
    }


    @Override
    protected void onStop()
    {
        Log.d("TIMER_TEST", "onStop()");
        super.onStop();
        TimerManager.saveState(this);
        if(TimerManager.isAnyTimerActive())
        {
            Intent serviceIntent = new Intent(this, ChronometerService.class);
            startForegroundService(serviceIntent);
        }
        if (serviceStoppedReceiver != null)
        {
            unregisterReceiver(serviceStoppedReceiver);
            serviceStoppedReceiver = null;
        }
    }

}