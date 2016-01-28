package com.taraxippus.livewallpaper;

import android.support.v7.widget.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import android.widget.FrameLayout.*;
import java.text.*;
import java.util.*;

public class WaveAdapter extends RecyclerView.Adapter<WaveAdapter.ViewHolder> implements View.OnClickListener
{
	@Override
	public void onClick(View v)
	{
	
	}

    public static class ViewHolder extends RecyclerView.ViewHolder 
	{
        public ViewHolder(View v) 
		{
            super(v);
        }
    }

    @Override
    public WaveAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		View v;

		if (viewType == 0)
		{
			v = LayoutInflater.from(parent.getContext()).inflate(R.layout.edit_waves_item, parent, false);
			v.setOnClickListener(this);
		}
		else
		{
			v = LayoutInflater.from(parent.getContext()).inflate(R.layout.edit_waves_item, parent, false);
			v.setOnClickListener(this);
		}

		ViewHolder vh = new ViewHolder(v);
		return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) 
	{
	
	}

	
    @Override
    public int getItemCount() 
	{
        return 10;
    }

	@Override
	public int getItemViewType(int position)
	{ 
		if (position == 0)
			return 0;

		return 1;
	}
}
