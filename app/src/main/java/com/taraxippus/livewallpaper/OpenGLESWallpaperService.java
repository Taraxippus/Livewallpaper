package com.taraxippus.livewallpaper;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.media.audiofx.*;
import android.net.*;
import android.opengl.*;
import android.provider.*;
import android.view.*;
import java.nio.*;
import javax.microedition.khronos.opengles.*;

import android.opengl.Matrix;
import java.io.*;
import javax.microedition.khronos.egl.*;
import android.os.*;
import android.preference.*;
import java.util.*;

public class OpenGLESWallpaperService extends GLWallpaperService 
{
    @Override
    public Engine onCreateEngine()
	{
        return new OpenGLES2Engine();
    }
	
    public class OpenGLES2Engine extends GLWallpaperService.GLEngine
	{
        @Override
        public void onCreate(SurfaceHolder surfaceHolder)
		{
            super.onCreate(surfaceHolder);
			
            final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
            final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;

            if (supportsEs2)
            {			
                setEGLContextClientVersion(2);
                setPreserveEGLContextOnPause(true);
				setEGLConfigChooser(new ConfigChooser());
				
				OpenGLESWallpaperService.GLRenderer renderer = new GLRenderer(OpenGLESWallpaperService.this);
				setRenderer(renderer);
            }
        }
    }
	
	public class ConfigChooser implements GLSurfaceView.EGLConfigChooser
	{
		@Override
		public javax.microedition.khronos.egl.EGLConfig chooseConfig(EGL10 p1, javax.microedition.khronos.egl.EGLDisplay p2)
		{
			int attribs[] = 
			{
				EGL10.EGL_LEVEL, 0,
				EGL10.EGL_RENDERABLE_TYPE, 4,
				EGL10.EGL_COLOR_BUFFER_TYPE, EGL10.EGL_RGB_BUFFER,
				EGL10.EGL_RED_SIZE, 8,
				EGL10.EGL_GREEN_SIZE, 8,
				EGL10.EGL_BLUE_SIZE, 8,
				EGL10.EGL_DEPTH_SIZE, 0,
				EGL10.EGL_SAMPLE_BUFFERS, 1,
				EGL10.EGL_SAMPLES, 1, 
				EGL10.EGL_NONE
			};
			
			javax.microedition.khronos.egl.EGLConfig[] configs = new  javax.microedition.khronos.egl.EGLConfig[1];
			int[] configCounts = new int[1];
			p1.eglChooseConfig(p2, attribs, configs, 1, configCounts);

			if (configCounts[0] == 0) 
			{
				throw new RuntimeException("Couln't set up opengl es");
			} 
			else
			{
				return configs[0];
			}
			
		}
	}
	
	public class GLRenderer implements GLSurfaceView.Renderer, SharedPreferences.OnSharedPreferenceChangeListener
	{

		boolean update;
		
		@Override
		public void onSharedPreferenceChanged(SharedPreferences p1, String p2)
		{
			update = true;
		}
		
		private final String vertexShader_wave =
		"#version 100\n" +
		"uniform mat4 u_MVP;" +
		"attribute vec4 a_Position;" +
		"varying vec3 v_Position;" +
		"void main() {" +
		"  v_Position = vec3(a_Position);" + 
		"  gl_Position = u_MVP * vec4(a_Position.xyz, 1.0);" +
		"}";

        private final String fragmentShader_wave_default =
		"#version 100\n" +
		"precision mediump float;" +
		"uniform vec4 u_Color;" +
		"uniform vec4 u_Wave;" +
		"varying vec3 v_Position;" + 
		"void main() {" +
		"  float height = cos(v_Position.x * u_Wave.y * 0.068 + u_Wave.x * 0.5 + 2.467) * u_Wave.z * 0.67" +
		"+ cos(v_Position.x * u_Wave.y * 0.356 + u_Wave.x * 3.0 + 1.4678) * u_Wave.z * 0.013255" + 
		"+ cos(v_Position.x * u_Wave.y + u_Wave.x) * u_Wave.z" +
		"+ cos(v_Position.x * u_Wave.y * 0.5 + u_Wave.x + 1.467) * u_Wave.z * 2.0" +
		"+ cos(v_Position.x * u_Wave.y * 0.25 + u_Wave.x + 2.467) * u_Wave.z * 0.5" +
		"+ u_Wave.w;" +
		"  if (v_Position.y > height)" +
		"    discard;" +
		"  gl_FragColor = vec4(u_Color.rgb, 0.25 + ((v_Position.y + 1.0) / (height + 1.0)) * 0.25);" +
		"}";

		
        private final String fragmentShader_wave_double =
		"#version 100\n" +
		"precision mediump float;" +
		"uniform vec4 u_Color;" +
		"uniform vec4 u_Wave;" +
		"varying vec3 v_Position;" + 
		"void main() {" +
		"  float height = cos(v_Position.x * u_Wave.y * 0.068 + u_Wave.x * 0.5 + 2.467) * u_Wave.z * 0.67" +
		"+ cos(v_Position.x * u_Wave.y * 0.356 + u_Wave.x * 3.0 + 1.4678) * u_Wave.z * 0.013255" + 
		"+ cos(v_Position.x * u_Wave.y + u_Wave.x) * u_Wave.z" +
		"+ cos(v_Position.x * u_Wave.y * 0.5 + u_Wave.x + 1.467) * u_Wave.z * 2.0" +
		"+ cos(v_Position.x * u_Wave.y * 0.25 + u_Wave.x + 2.467) * u_Wave.z * 0.5" +
		"+ u_Wave.w;" +
		"  if (v_Position.y > height || v_Position.y < -height)" +
		"    discard;" +
		"  gl_FragColor = vec4(u_Color.rgb, 0.25 + abs(v_Position.y / height) * 0.25);" +
		"}";
		
        private final String fragmentShader_wave_double_rotated =
		"#version 100\n" +
		"precision mediump float;" +
		"uniform vec4 u_Color;" +
		"uniform vec4 u_Wave;" +
		"varying vec3 v_Position;" + 
		"void main() {" +
		"  float height = cos(v_Position.x * u_Wave.y * 0.068 + u_Wave.x * 0.5 + 2.467) * u_Wave.z * 0.67" +
		"+ cos(v_Position.x * u_Wave.y * 0.356 + u_Wave.x * 3.0 + 1.4678) * u_Wave.z * 0.013255" + 
		"+ cos(v_Position.x * u_Wave.y + u_Wave.x) * u_Wave.z" +
		"+ cos(v_Position.x * u_Wave.y * 0.5 + u_Wave.x + 1.467) * u_Wave.z * 2.0" +
		"+ cos(v_Position.x * u_Wave.y * 0.25 + u_Wave.x + 2.467) * u_Wave.z * 0.5" +
		"+ u_Wave.w;" +
		"  float height1 = cos(v_Position.x * -u_Wave.y * 0.068 + u_Wave.x * 0.5 + 2.467) * u_Wave.z * 0.67" +
		"+ cos(v_Position.x * -u_Wave.y * 0.356 + u_Wave.x * 3.0 + 1.4678) * u_Wave.z * 0.013255" + 
		"+ cos(v_Position.x * -u_Wave.y + u_Wave.x) * u_Wave.z" +
		"+ cos(v_Position.x * -u_Wave.y * 0.5 + u_Wave.x + 1.467) * u_Wave.z * 2.0" +
		"+ cos(v_Position.x * -u_Wave.y * 0.25 + u_Wave.x + 2.467) * u_Wave.z * 0.5" +
		"+ u_Wave.w;" +
		"  if (v_Position.y > height || v_Position.y < -height1)" +
		"    discard;" +
		"  gl_FragColor = vec4(u_Color.rgb, 0.25 + (v_Position.y > 0.0 ? v_Position.y / height : v_Position.y / -height1) * 0.25);" +
		"}";
		
		final float[] projection_wave = new float[16];
		final float[] view_wave = new float[16];
		final float[] model = new float[16];
		
		final float[] mvp = new float[16];
		
		final Program program_wave = new Program();
		final Shape wave = new Shape();
		
		int mvpHandle_wave;
		int waveHandle_wave;
		int colorHandle_wave;
		
		final Context context;
		final ArrayList<Wave> waves = new ArrayList<>();
		
		public GLRenderer(Context context)
		{
			super();	
			
			this.context = context;
			

			this.waves.add(new Wave(0.0625F, 6F, 0.255F, 1 / 1000000F, 1));
			this.waves.add(new Wave(0.0375F, 5F, 0.1965F, 1 / 750000F, 0.75F));
			this.waves.add(new Wave(0.235F, 4F, 0.235F, 1 / 800000F, 0.5F));
			this.waves.add(new Wave(0.1975F, 7F, 0.1975F, 1 / 900000F, 0.25F));
			this.waves.add(new Wave(0.3775F, 3F, 0.1965F, 1 / 1200000F, 0.65F));
			this.waves.add(new Wave(0.13F, 2F, 0.1965F, 1 / 700000F, 0));
		}
		
		public String getFragmentShader(String preference)
		{
			switch (preference)
			{
				case "double":
					return fragmentShader_wave_double;
					
				case "double_rotated":
					return fragmentShader_wave_double_rotated;
				
				default:
					return fragmentShader_wave_default;
			}
		}
		
		boolean defaultMode;
		boolean colorVariation;
		int colorWave1, colorWave2, colorBackground;
		
		@Override
		public void onSurfaceCreated(GL10 p1, javax.microedition.khronos.egl.EGLConfig p2)
		{
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
			
			colorWave1 = Color.parseColor(preferences.getString("colorWave1", "#ffff00"));
			colorWave2 = Color.parseColor(preferences.getString("colorWave2", "#ff4400"));
			colorBackground = Color.parseColor(preferences.getString("colorBackground", "#ff8800"));

			timeFactor = Float.parseFloat(preferences.getString("timeFactor", "1"));
			touchTimeFactor = Float.parseFloat(preferences.getString("touchTimeFactor", "1"));
			
			defaultMode = preferences.getString("mode", "default").equals("default");
			
			FloatBuffer vertices_wave = FloatBuffer.allocate(4 * 5);
			
			vertices_wave.put(-ratio);
			vertices_wave.put(-1);
			vertices_wave.put(0);

			vertices_wave.put(ratio);
			vertices_wave.put(-1);
			vertices_wave.put(0);

			vertices_wave.put(-ratio);
			vertices_wave.put(1);
			vertices_wave.put(0);
	
			vertices_wave.put(ratio);
			vertices_wave.put(1);
			vertices_wave.put(0);

			ShortBuffer indices_wave = ShortBuffer.allocate(6);

			indices_wave.put((short) 0);
			indices_wave.put((short) 1);
			indices_wave.put((short) 2);

			indices_wave.put((short) 1);
			indices_wave.put((short) 2);
			indices_wave.put((short) 3);

			wave.init(GLES20.GL_TRIANGLES, vertices_wave, indices_wave);
			
			program_wave.init(vertexShader_wave, getFragmentShader(preferences.getString("mode", "default")), "a_Position");
	
			program_wave.use();
			colorHandle_wave = GLES20.glGetUniformLocation(program_wave.program, "u_Color");
			mvpHandle_wave = GLES20.glGetUniformLocation(program_wave.program, "u_MVP");
			waveHandle_wave = GLES20.glGetUniformLocation(program_wave.program, "u_Wave");
			
			if (!(colorVariation = preferences.getBoolean("colorVariation", false)))
				GLES20.glUniform4f(colorHandle_wave, Color.red(colorWave1) / 255F, Color.green(colorWave1) / 255F, Color.blue(colorWave1) / 255F, 1);
			
			GLES20.glClearColor(Color.red(colorBackground) / 255F, Color.green(colorBackground) / 255F, Color.blue(colorBackground) / 255F, 1);
			GLES20.glEnable(GLES20.GL_BLEND);
			GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
			
			Matrix.setLookAtM(view_wave, 0, 0F, 0F, 1.5F, 0, 0, 0, 0, 1, 0);
			
			update = false;
		}	
		
		float ratio;
		
		@Override
		public void onSurfaceChanged(GL10 p1, int width, int height)
		{
			GLES20.glViewport(0, 0, width, height);

			ratio = (float) width / height;
			Matrix.orthoM(projection_wave, 0, -ratio, ratio, -1, 1, 1, 2);
			
			update = true;
		}

		float time;
		float velocity;
		long lastTime;
		float delta;
		
		float timeFactor;
		float touchTimeFactor;
		
		static final int maxFPS = 30;
		
		@Override
		public void onDrawFrame(GL10 p1)
		{	
			if (lastTime == 0)
				lastTime = SystemClock.elapsedRealtimeNanos() / 1000;
		
			delta = 1F / (SystemClock.elapsedRealtimeNanos() / 1000 - lastTime);
			
			if (1 / delta / 10000 < 1000F / maxFPS)
				try
				{
					Thread.sleep((int)(1000F / maxFPS - 1 / delta / 10000));

					delta = 1F / (SystemClock.elapsedRealtimeNanos() / 1000 - lastTime);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}

			time += SystemClock.elapsedRealtimeNanos() / 1000 - lastTime;
			time += velocity * 100 * delta * touchTimeFactor;
			velocity *= 0.995F;
				
			lastTime = SystemClock.elapsedRealtimeNanos() / 1000;

			if (update)
			{
				onSurfaceCreated(p1, null);
			}
		
			if (!program_wave.initialized()
				|| !wave.initialized())
			{
				System.err.println("Not initialized!");
				return;
			}
	
			GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
		
			program_wave.use();

			Matrix.setIdentityM(model, 0);

			Matrix.multiplyMM(mvp, 0, view_wave, 0, model, 0);
			Matrix.multiplyMM(mvp, 0, projection_wave, 0, mvp, 0);
			
			GLES20.glUniformMatrix4fv(mvpHandle_wave, 1, false, mvp, 0);
			
			for (Wave wave : waves)
			{
				wave.uniform(this);
				this.wave.draw(3);
			}
		}

		public void uniformColor(float percent)
		{

			if (colorVariation)
				GLES20.glUniform4f(colorHandle_wave, 
				(Color.red(colorWave1) * percent + Color.red(colorWave2) * (1 - percent)) / 255F, 
				(Color.green(colorWave1) * percent + Color.green(colorWave2) * (1 - percent)) / 255F, 
				(Color.blue(colorWave1) * percent + Color.blue(colorWave2) * (1 - percent)) / 255F, 1);
		}
	}
}
