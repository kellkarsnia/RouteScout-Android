package com.mygistics.routescout.adapters;

import java.util.ArrayList;

import com.mygistics.routescout.R;
import com.mygistics.routescout.vo.*;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

public class MembersArrayAdapter extends ArrayAdapter<HouseholdMember> implements Filterable
{
	private ArrayList<HouseholdMember> values;
	private ArrayList<HouseholdMember> originalValues;
	private LayoutInflater inflater;
	private Filter filter;

	
	public MembersArrayAdapter(Context context, ArrayList<HouseholdMember> values)
	{
		super(context, R.layout.member_list_item, values);
		this.values = values;
		this.originalValues = this.values;
		
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public void add(HouseholdMember object)
	{
		super.add(object);
	}

	@Override
	public void clear()
	{
		super.clear();
	}
	
	@Override
	public void remove(HouseholdMember object)
	{
		super.remove(object);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View itemView = convertView;
        if(convertView == null)
        {
        	itemView = inflater.inflate(R.layout.member_list_item, parent, false);
        }

        HouseholdMember member = values.get(position);
        
		TextView tv = (TextView)itemView.findViewById(R.id.memberName);
		tv.setText(member.name);
		return itemView;
	}
	
	@Override
	public int getCount()
	{
		return values.size();
	}
	
	@Override
	public HouseholdMember getItem(int position)
	{
		return values.get(position);
	}

	@Override
	public Filter getFilter()
	{
		if(filter == null)
			filter = new MembersFilter();
		return filter;
	}
	
	private class MembersFilter extends Filter
	{
		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence prefix, FilterResults results)
		{
			values =  (ArrayList<HouseholdMember>)results.values;
			notifyDataSetChanged();
		}

		@Override
		protected FilterResults performFiltering(CharSequence prefix)
		{
			Log.v("Performing filter", prefix.toString());
			
			FilterResults results = new FilterResults();
			ArrayList<HouseholdMember> filteredMembers = new ArrayList<HouseholdMember>();

			if (prefix!= null && prefix.toString().length() > 0)
			{
				for (int index = 0; index < originalValues.size(); index++)
				{
					HouseholdMember member = originalValues.get(index);
					
					if(member.name.contains(prefix.toString()))
					{
						filteredMembers.add(member);
					}
				}
				results.values = filteredMembers;
				results.count = filteredMembers.size();      
			}
			else
			{
				synchronized (originalValues)
				{
					results.values = originalValues;
					results.count = originalValues.size();
				}
			}
			return results;
		}
	}

}
