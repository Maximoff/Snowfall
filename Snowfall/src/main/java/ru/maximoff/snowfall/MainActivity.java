package ru.maximoff.snowfall;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import ru.maximoff.snowfall.R;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		final boolean lightTheme = prefs.getBoolean("light", false);
		final boolean fromResources = prefs.getBoolean("res", false);
		ImageButton button = findViewById(R.id.button);
		if (lightTheme) {
			button.setImageResource(R.drawable.ic_invert);
		} else {
			button.setImageResource(R.drawable.ic_invert_dark);
		}
		button.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View p1) {
					prefs.edit().putBoolean("light", !lightTheme).commit();
					MainActivity.this.recreate();
				}
			});
		ImageButton button2 = findViewById(R.id.button2);
		if (fromResources) {
			button2.setImageResource(R.drawable.snowflake_d);
		} else {
			button2.setImageResource(R.drawable.snowflake);
		}
		button2.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View p1) {
					prefs.edit().putBoolean("res", !fromResources).commit();
					MainActivity.this.recreate();
				}
			});
    }

	@Override
	public Resources.Theme getTheme() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		Resources.Theme theme = super.getTheme();
		if (prefs.getBoolean("light", false)) {
			theme.applyStyle(R.style.AppThemeL, true);
		}
		return theme;
	}

	@Override
	public void onBackPressed() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			finishAndRemoveTask();
		} else {
			finish();
		}
	}
}
