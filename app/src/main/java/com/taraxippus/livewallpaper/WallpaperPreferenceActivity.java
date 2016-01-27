package com.taraxippus.livewallpaper;
import android.app.*;
import android.content.*;
import android.os.*;
import android.preference.*;
import android.view.*;
import android.widget.*;
import android.graphics.*;
import android.view.inputmethod.*;
import android.graphics.drawable.*;

public class WallpaperPreferenceActivity extends PreferenceActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(com.taraxippus.livewallpaper.R.xml.preference);
	
		Preference setWallpaper = findPreference("setWallpaper");
		setWallpaper.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() 
			{
				@Override
				public boolean onPreferenceClick(Preference preference)
				{
					try
					{
						Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
						intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new ComponentName(WallpaperPreferenceActivity.this, com.taraxippus.livewallpaper.OpenGLESWallpaperService.class));
						startActivity(intent);
					}
					catch(Exception e)
					{
						Intent intent = new Intent();
						intent.setAction(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER);
						startActivity(intent);
					}
					
					return true;
				}
			});

		Preference waves = findPreference("waves");
		waves.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() 
			{
				@Override
				public boolean onPreferenceClick(Preference preference)
				{
					editWaves();
					return true;
				}
			});
			
			
		setSummary("Modify wave animation speed", "timeFactor");
		setSummary("Modify wave animation speed", "touchTimeFactor");
		setSummary("Choose between different wallpaper modes", "mode");
		
		chooseColor("colorWave1", "#ffff00");
		chooseColor("colorWave2", "#ff4400");
		chooseColor("colorBackground", "#ff8800");
		
		Preference crash = findPreference("crash");
		crash.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() 
			{
				@Override
				public boolean onPreferenceClick(Preference preference)
				{
					throw new RuntimeException("Debug crash");
				}
			});
	}
	
	public void chooseColor(final String sharedPreference, final String def)
	{
		final Preference p = findPreference(sharedPreference);
		p.setIcon(getIcon(0xFF000000 | Color.parseColor(PreferenceManager.getDefaultSharedPreferences(this).getString(sharedPreference, def))));
		
		p.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
			{
				@Override
				public boolean onPreferenceClick(Preference p1)
				{
					int colorInt = Color.parseColor(PreferenceManager.getDefaultSharedPreferences(WallpaperPreferenceActivity.this).getString(sharedPreference, def));
					
					final AlertDialog alertDialog = new AlertDialog.Builder(WallpaperPreferenceActivity.this).create();
					alertDialog.setTitle("Choose color");
					final View v = getLayoutInflater().inflate(R.layout.color, null);
					alertDialog.setView(v);
					final View color = v.findViewById(R.id.color);
					color.getBackground().setColorFilter(0xFF000000 | colorInt, PorterDuff.Mode.MULTIPLY);
					final SeekBar red = (SeekBar) v.findViewById(R.id.red);
					red.setProgress(Color.red(colorInt));
					final SeekBar green = (SeekBar) v.findViewById(R.id.green);
					green.setProgress(Color.green(colorInt));
					final SeekBar blue = (SeekBar) v.findViewById(R.id.blue);
					blue.setProgress(Color.blue(colorInt));
					final EditText hex = (EditText) v.findViewById(R.id.hex);
					hex.setText(Integer.toHexString(colorInt).toUpperCase());
					SeekBar.OnSeekBarChangeListener listener = new SeekBar.OnSeekBarChangeListener()
					{
						@Override
						public void onProgressChanged(SeekBar p1, int p2, boolean p3)
						{
							int colorInt = fromRGB(red.getProgress(), green.getProgress(), blue.getProgress());
							color.getBackground().setColorFilter(colorInt,PorterDuff.Mode.MULTIPLY);
							hex.setText(Integer.toHexString(colorInt).substring(2).toUpperCase());
						}

						@Override
						public void onStartTrackingTouch(SeekBar p1)
						{

						}

						@Override
						public void onStopTrackingTouch(SeekBar p1)
						{

						}
					};
					red.setOnSeekBarChangeListener(listener);
					green.setOnSeekBarChangeListener(listener);
					blue.setOnSeekBarChangeListener(listener);

					hex.setOnEditorActionListener(new EditText.OnEditorActionListener()
						{
							@Override
							public boolean onEditorAction(TextView p1, int p2, KeyEvent p3)
							{
								if (p2 == EditorInfo.IME_ACTION_GO)
								{
	
									int colorInt = Integer.parseInt(p1.getText().toString(), 16);
									color.getBackground().setColorFilter(0xFF000000 | colorInt, PorterDuff.Mode.MULTIPLY);
									red.setProgress(Color.red(colorInt));
									green.setProgress(Color.green(colorInt));
									blue.setProgress(Color.blue(colorInt));

									return true;
								}
								return false;
							}	
						});

					alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Choose", new AlertDialog.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface p1, int p2)
							{
								PreferenceManager.getDefaultSharedPreferences(WallpaperPreferenceActivity.this).edit().putString(sharedPreference, "#" + hex.getText().toString()).commit();
								p.setIcon(getIcon(0xFF000000 | Color.parseColor(PreferenceManager.getDefaultSharedPreferences(WallpaperPreferenceActivity.this).getString(sharedPreference, def))));
								
								alertDialog.dismiss();
							}
						});
					alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new AlertDialog.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface p1, int p2)
							{
								alertDialog.cancel();
							}
						});

					alertDialog.show();
					
					return true;
				}
		});
	}
	
	public Drawable getIcon(int color)
	{
//		Bitmap bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888);
//		Paint paint = new Paint();
//		paint.setColor(color);
//		new Canvas(bitmap).drawCircle(256, 256, 256 * 3 / 4F, paint);
//		return new BitmapDrawable(getResources(), bitmap);
		
		Drawable circle = getDrawable(R.drawable.circle);
		circle.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
		return circle;
	}
	
	public static int fromRGB(int red, int green, int blue)
	{
		red = (red << 16) & 0x00FF0000;
		green = (green << 8) & 0x0000FF00;
		blue = blue & 0x000000FF;
		return 0xFF000000 | red | blue | green;
	}
	
	public void setSummary(final String summary, final String key)
	{
		final Preference preference = findPreference(key);
		preference.setSummary(summary + "\nCurrent: " + PreferenceManager.getDefaultSharedPreferences(this).getString(key, "null"));
		preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
			{
				@Override
				public boolean onPreferenceChange(Preference p1, Object p2)
				{
					preference.setSummary(summary + "\nCurrent: " + p2.toString());
					return true;
				}	
			});
	}
	
	public void editWaves()
	{
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle("Edit waves");
		
		View v = getLayoutInflater().inflate(R.layout.edit_waves, null);
		
		//TODO: edit waves
		
		alertDialog.setView(v);
		alertDialog.show();
		
	}
}
