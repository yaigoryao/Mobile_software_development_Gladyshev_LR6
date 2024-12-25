package gladyshev.msdf.lr6;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DateWidget extends AppWidgetProvider
{
    private static String currentDate = "";

    private static void setUpCurrentDate()
    {
        HashMap<DayOfWeek, String> dayOfWeekTranslation = new HashMap<>();
        dayOfWeekTranslation.put(DayOfWeek.MONDAY, "Понедельник");
        dayOfWeekTranslation.put(DayOfWeek.TUESDAY, "Вторник");
        dayOfWeekTranslation.put(DayOfWeek.WEDNESDAY, "Среда");
        dayOfWeekTranslation.put(DayOfWeek.THURSDAY, "Четверг");
        dayOfWeekTranslation.put(DayOfWeek.FRIDAY, "Пятница");
        dayOfWeekTranslation.put(DayOfWeek.SATURDAY, "Суббота");
        dayOfWeekTranslation.put(DayOfWeek.SUNDAY, "Воскресенье");

        HashMap<Integer, String> monthMap = new HashMap<>();
        monthMap.put(1, "Января");
        monthMap.put(2, "Февраля");
        monthMap.put(3, "Марта");
        monthMap.put(4, "Апреля");
        monthMap.put(5, "Мая");
        monthMap.put(6, "Июня");
        monthMap.put(7, "Июля");
        monthMap.put(8, "Августа");
        monthMap.put(9, "Сентября");
        monthMap.put(10, "Октября");
        monthMap.put(11, "Ноября");
        monthMap.put(12, "Декабря");

        Date c = Calendar.getInstance().getTime();
        OffsetDateTime offset = OffsetDateTime.now();
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat df = new SimpleDateFormat("dd MM yyyy", Locale.getDefault());
        String[] dateParts = df.format(c).split(" ");
        sb.append(dayOfWeekTranslation.get(offset.getDayOfWeek()));
        sb.append(", ");
        sb.append(dateParts[0]);
        sb.append(" ");
        sb.append(monthMap.get(Integer.parseInt(dateParts[1])));
        sb.append(" ");
        sb.append(dateParts[2]);
        sb.append(" г.");
        currentDate = sb.toString();
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId)
    {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.date_widget_layout);
        final int[] ids = new int []{R.id.date_tv1, R.id.date_tv2, R.id.date_tv3, R.id.date_tv4,R.id.date_tv5,R.id.date_tv6};
        for(int id : ids) views.setTextViewText(id, currentDate);
        for(int id : ids) views.setOnClickPendingIntent(id, PendingIntent.getActivity(context, id + 100 * appWidgetId, new Intent(context, DateWidgetConfigureActivity.class).putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId), PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_CANCEL_CURRENT));
        SharedPreferences prefs = context.getSharedPreferences(DateWidgetConfigureActivity.PREFS_NAME, Context.MODE_PRIVATE);
        Integer color = prefs.getInt(DateWidgetConfigureActivity.PREF_PREFIX_KEY + appWidgetId + DateWidgetConfigureActivity.PREF_COLOR, -1);
        final Map<Integer, Integer> colors = new HashMap<Integer, Integer>() {{
            put(1, Color.RED);
            put(2, Color.YELLOW);
            put(3, Color.GREEN);
            put(4, Color.MAGENTA);
            put(5, Color.BLUE);
            put(6, Color.GRAY);
            put(-1, Color.GRAY);
        }};
        if(color > 0)
            for (int id : ids) views.setTextColor(id, colors.get(color));

        final Map<Integer, Integer> fonts = new HashMap<Integer, Integer>() {{
            put(1, ids[0]);
            put(2, ids[1]);
            put(3, ids[2]);
            put(4, ids[3]);
            put(5, ids[4]);
            put(6, ids[5]);
            put(-1, ids[0]);
        }};
        Integer font = prefs.getInt(DateWidgetConfigureActivity.PREF_PREFIX_KEY + appWidgetId + DateWidgetConfigureActivity.PREF_FONT, -1);
        if(font > 0)
        {
            for (int id : ids) views.setViewVisibility(id, View.INVISIBLE);
            views.setViewVisibility(fonts.get(font), View.VISIBLE);
        }

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        setUpCurrentDate();
        for (int appWidgetId : appWidgetIds) updateAppWidget(context, appWidgetManager, appWidgetId);

    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) { }

    @Override
    public void onEnabled(Context context)
    {
        setUpCurrentDate();
        Log.d("LOG", "onEnabled: " + currentDate);
    }

    @Override
    public void onDisabled(Context context) { }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (Intent.ACTION_DATE_CHANGED.equalsIgnoreCase(intent.getAction())) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

            ComponentName thisWidget = new ComponentName(context, DateWidget.class);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
            setUpCurrentDate();
            for (int appWidgetId : appWidgetIds)
                updateAppWidget(context, AppWidgetManager.getInstance(context), appWidgetId);

        }
    }
}