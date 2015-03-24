package com.mygistics.routescout.vo;

public class FormattedID
{
	public String tag;

	public FormattedID(String name, int id)
	{
		tag = "\""+name+"\":\""+id+"\"";
	}

}
