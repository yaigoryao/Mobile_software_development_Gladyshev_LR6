package gladyshev.msdf.lr6;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.fonts.Font;
import android.os.Bundle;
import android.util.Log;
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

    Spinner colorSpinner;
    Spinner fontSpinner;

    public static final Map<Integer, String> colors = new HashMap<Integer, String>() {{
        put(1, "Красный");
        put(2, "Желтый");
        put(3, "Зеленый");
        put(4, "Лиловый");
        put(5, "Синий");
        put(6, "Серый");
    }};

    public static final Map<Integer, String> fonts = new HashMap<Integer, String>() {{
        put(1, "sans-serif");
        put(2, "sans-serif-light");
        put(3, "sans-serif-medium");
        put(4, "sans-serif-condensed");
        put(5, "sans-serif-black");
        put(6, "serif");
    }};

    View.OnClickListener mOnClickListener = new View.OnClickListener()
    {
        public void onClick(View v)
        {
            final Context context = DateWidgetConfigureActivity.this;

            Integer selectedColor = (int)binding.colorSpinner.getSelectedItemId();
            Integer selectedFont = (int)binding.fontSpinner.getSelectedItemId();
            savePrefs(context, mAppWidgetId,
                    new ArrayList<Integer>(colors.keySet()).get(selectedColor),
                    new ArrayList<Integer>(fonts.keySet()).get(selectedFont));

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
        Integer i = prefs.getInt(PREF_PREFIX_KEY + appWidgetId + param, -1);
        return i;
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

        ArrayAdapter colorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, colors.values().toArray());
        ArrayAdapter fontAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fonts.values().toArray());

        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fontAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        binding.colorSpinner.setAdapter(colorAdapter);
        binding.fontSpinner.setAdapter(fontAdapter);

        Integer currentColor = getPrefs(this, mAppWidgetId, PREF_COLOR);
        Integer currentFont = getPrefs(this, mAppWidgetId, PREF_FONT);

        binding.colorSpinner.setSelection(Arrays.asList(colors.values()).indexOf(currentColor));
        binding.colorSpinner.setSelection(Arrays.asList(fonts.values()).indexOf(currentFont));

    }
}