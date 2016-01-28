package com.taraxippus.livewallpaper;

import android.opengl.*;

public class Wave
{
	public float amplitude = 0.05F;
	public float frequenz = 5F;
	public float offset = 0.15F;
	public float timeFactor = 1F / 1000000F;
	public float colorPercentage = 0.5F;
	
	public Wave()
	{
		
	}
	
	public Wave(float amplitude, float frequenz, float offset, float timeFactor, float colorPercentage)
	{
		this.amplitude = amplitude;
		this.frequenz = frequenz;
		this.offset = offset;
		this.timeFactor = timeFactor;
		this.colorPercentage = colorPercentage;
	}
	
	public void uniform(OpenGLESWallpaperService.GLRenderer renderer)
	{
		renderer.uniformColor(colorPercentage);
		GLES20.glUniform4f(renderer.waveHandle_wave, (renderer.time * timeFactor * renderer.timeFactor) % ((float) Math.PI * 2 * 2), frequenz, amplitude, renderer.defaultMode ? offset / 2F : offset);
	}
}
