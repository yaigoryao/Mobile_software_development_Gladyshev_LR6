package gladyshev.msdf.dz1;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class TimerManager {

    public static final HashMap<Integer, Timer> timers = new HashMap<>();
    private static final String PREFS_NAME = "TimerPreferences";
    private static final String TIMERS_KEY = "Timers";
    public static final int DEFAULT_TIMER_COUNT = 5;

    public static void startTimer(int id, Consumer<Integer> onTick) {
        Timer timer = timers.getOrDefault(id, new Timer(id));

        if (timer.executorService != null && !timer.executorService.isShutdown())
        {
            timer.executorService.shutdownNow();
        }

        timer.isActive = true;
        timer.isPaused = false;
        timer.executorService = Executors.newSingleThreadScheduledExecutor();
        timers.put(id, timer);
        Log.d("TIMER_TEST", String.valueOf(timer.id) + " таймер запущен!");
        timer.executorService.scheduleWithFixedDelay(() ->
        {
            if (timer.isActive && !timer.isPaused)
            {
                timer.value++;

                Log.d("TIMER_TEST", String.format("Таймер %d: %d секунд", timer.id, timer.value));

                if (onTick != null) {
                    onTick.accept(timer.value);
                }
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    public static void stopTimer(int id)
    {
        modifyTimer(id, true);
    }

    public static void pauseTimer(int id)
    {
        modifyTimer(id, false);
    }

    private static void modifyTimer(int id, boolean resetValue)
    {
        Timer timer = timers.get(id);
        if (timer != null)
        {
            timer.isActive = false;
            timer.isPaused = true;
            if (resetValue)
            {
                timer.value = 0;
                timer.isPaused = false;
            }
            Log.d("TIMER_TEST", String.valueOf(timer.id) + " попытка остановки...");
            if (timer.executorService != null && !timer.executorService.isShutdown())
            {
                timer.executorService.shutdownNow();
                Log.d("TIMER_TEST", String.valueOf(timer.id) + " таймер остановлен");
            }
        }
    }

    public static void saveState(Context context)
    {
        Log.d("TIMER_TEST", "=========================================");
        Log.d("TIMER_TEST", "Saving state...");

        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        StringBuilder serializedTimers = new StringBuilder();
        for (Map.Entry<Integer, Timer> entry : timers.entrySet()) {
            serializedTimers.append(entry.getValue().toString()).append(";");
            pauseTimer(entry.getValue().id);
        }

        editor.putString(TIMERS_KEY, serializedTimers.toString());
        editor.commit();

        for(Timer t : timers.values())
        {
            Log.d("TIMER_TEST", t.toString());
        }
        Log.d("TIMER_TEST", "Saved!");

    }

    public static void loadState(Context context, Map<Integer, Consumer<Integer>> consumerMap)
    {
        Log.d("TIMER_TEST", "=========================================");
        Log.d("TIMER_TEST", "Loading state...");

        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String serializedTimers = sharedPreferences.getString(TIMERS_KEY, "");

        timers.clear();
        if (serializedTimers != null && !serializedTimers.isEmpty())
        {
            String[] timerEntries = serializedTimers.split(";");
            for (String entry : timerEntries)
            {
                Timer timer = new Timer(entry);
                timers.put(timer.id, timer);
                if (timer.isActive && !timer.isPaused)
                {
                    if(consumerMap != null) startTimer(timer.id, consumerMap.get(timer.id));
                    else startTimer(timer.id, null);
                }
            }
        }
        else initializeDefaultTimers();


        for(Timer t : timers.values())
        {
            Log.d("TIMER_TEST", t.toString());
        }
        Log.d("TIMER_TEST", "Loaded!");
    }

    private static void initializeDefaultTimers() {
        for (int i = 1; i <= DEFAULT_TIMER_COUNT; i++) {
            timers.put(i, new Timer(i));
        }
    }

    public static class Timer
    {
        int id;
        int value = 0;
        boolean isActive = false;
        boolean isPaused = false;
        ScheduledExecutorService executorService;

        Timer(int id)
        {
            this.id = id;
        }

        Timer(String str)
        {
            String[] parts = str.split(",");
            if (parts.length == 4)
            {
                this.id = Integer.parseInt(parts[0]);
                this.value = Integer.parseInt(parts[1]);
                this.isActive = Integer.parseInt(parts[2]) == 1;
                this.isPaused = Integer.parseInt(parts[3]) == 1;
            }
        }

        @Override
        public String toString() {
            return id + "," + value + "," + (isActive ? 1 : 0) + "," + (isPaused ? 1 : 0);
        }
    }
}