package gladyshev.msdf.lr6;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.fonts.Font;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import gladyshev.msdf.lr6.databinding.DateWidgetConfigureActivityLayoutBinding;

public class DateWidgetConfigureActivity extends Activity
{
    public static final String PREFS_NAME = "gladyshev.msdf.lr6.NewAppWidget";
    public static final String PREF_PREFIX_KEY = "appwidget_";
    public static final String PREF_COLOR = "color";
    public static final String PREF_FONT = "font";
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    public static final Map<String, Integer> colors = new HashMap<String, Integer>() {{
        put("Красный", Color.RED);
        put("Желтый", Color.YELLOW);
        put("Зеленый", Color.GREEN);
        put("Лиловый", Color.MAGENTA);
        put("Синий", Color.BLUE);
        put("Серый", Color.GRAY);
    }};

    public static final Map<String, Integer> fonts = new HashMap<String, Integer>() {{
        put("sans-serif", R.id.date_tv1);
        put("sans-serif-light", R.id.date_tv2);
        put("sans-serif-medium", R.id.date_tv3);
        put("sans-serif-condensed", R.id.date_tv4);
        put("sans-serif-black", R.id.date_tv5);
        put("serif", R.id.date_tv6);
    }};

    View.OnClickListener mOnClickListener = new View.OnClickListener()
    {
        public void onClick(View v)
        {
            final Context context = DateWidgetConfigureActivity.this;
            savePrefs(context, mAppWidgetId,
                    colors.get(binding.colorSpinner.getSelectedItem()),
                    fonts.get(binding.fontSpinner.getSelectedItem()));

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            DateWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId);

            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    };
    private DateWidgetConfigureActivityLayoutBinding binding;

    public DateWidgetConfigureActivity()
    {
        super();
    }

    static void savePrefs(Context context, int appWidgetId, Integer color, Integer font)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        prefs.putInt(PREF_PREFIX_KEY + appWidgetId + PREF_COLOR, color);
        prefs.putInt(PREF_PREFIX_KEY + appWidgetId + PREF_FONT, font);
        prefs.commit();
    }

    static Integer getPrefs(Context context, int appWidgetId, String param)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(PREF_PREFIX_KEY + appWidgetId + param, -1);
    }

    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);

        setResult(RESULT_CANCELED);

        binding = DateWidgetConfigureActivityLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.addButton.setOnClickListener(mOnClickListener);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null)
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID)
        {
            finish();
            return;
        }

        ArrayAdapter colorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, colors.keySet().toArray());
        ArrayAdapter fontAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fonts.keySet().toArray());

        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fontAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        binding.colorSpinner.setAdapter(colorAdapter);
        binding.fontSpinner.setAdapter(fontAdapter);

        int currentColor = getPrefs(this, mAppWidgetId, PREF_COLOR);
        int currentFont = getPrefs(this, mAppWidgetId, PREF_FONT);

        binding.colorSpinner.setSelection( currentColor == -1 ? 0 : new ArrayList<>(colors.values()).indexOf(currentColor));

        binding.fontSpinner.setSelection( currentFont == -1 ? 0 : new ArrayList<>(fonts.values()).indexOf(currentFont));

    }
}