package com.taraxippus.livewallpaper;
import android.content.*;
import android.opengl.*;
import android.preference.*;
import android.service.wallpaper.*;
import android.view.*;

public abstract class GLWallpaperService extends WallpaperService
{
	public static SurfaceHolder holder;
	
	public class GLEngine extends Engine
	{
		private WallpaperGLSurfaceView glSurfaceView;
		private boolean rendererHasBeenSet;
		
		@Override
		public void onCreate(SurfaceHolder surfaceHolder) 
		{
			super.onCreate(surfaceHolder);
			
			holder = surfaceHolder;
			
			glSurfaceView = new WallpaperGLSurfaceView(GLWallpaperService.this);
		}
		
		@Override
		public void onVisibilityChanged(boolean visible) 
		{
			super.onVisibilityChanged(visible);
			
			if (rendererHasBeenSet) 
			{
				if (visible) 
				{
					glSurfaceView.onResume();
				}
				else 
				{
					glSurfaceView.onPause();            
				}
			}
		}
		
		@Override
		public void onDestroy() 
		{
			super.onDestroy();
			glSurfaceView.onDestroy();
		}
		
		GLSurfaceView.Renderer renderer;
		
		protected void setRenderer(GLSurfaceView.Renderer renderer) 
		{
			glSurfaceView.setRenderer(renderer);
			this.renderer = renderer;
			if (renderer instanceof OpenGLESWallpaperService.GLRenderer)
			{
				PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).registerOnSharedPreferenceChangeListener((OpenGLESWallpaperService.GLRenderer)renderer);
			}
			rendererHasBeenSet = true;
		}

		protected void setEGLContextClientVersion(int version)
		{
			glSurfaceView.setEGLContextClientVersion(version);
		}

		protected void setPreserveEGLContextOnPause(boolean preserve)
		{
			glSurfaceView.setPreserveEGLContextOnPause(preserve);
		}
		

		protected void setEGLConfigChooser(GLSurfaceView.EGLConfigChooser chooser)
		{
			glSurfaceView.setEGLConfigChooser(chooser);
		}
		
		float pixelOffset;
		
		@Override
		public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset)
		{
			super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep, xPixelOffset, yPixelOffset);
			
			if (renderer instanceof OpenGLESWallpaperService.GLRenderer)
			{
				((OpenGLESWallpaperService.GLRenderer)renderer).velocity += ((xPixelOffset - pixelOffset) / (float)this.getDesiredMinimumWidth()) * 250000000;
				pixelOffset = xPixelOffset;
			}
		}
		
		public class WallpaperGLSurfaceView extends GLSurfaceView
		{		
			WallpaperGLSurfaceView(Context context)
			{
				super(context);
			}

			@Override
			public SurfaceHolder getHolder()
			{
				return GLEngine.this == null ? holder : GLEngine.this.getSurfaceHolder();
			}

			public void onDestroy()
			{
				super.onDetachedFromWindow();
			}
		}
	}
}

