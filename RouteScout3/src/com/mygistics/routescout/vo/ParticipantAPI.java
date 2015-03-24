package com.mygistics.routescout.vo;


public class ParticipantAPI
{
	public int participantID;
	public FormattedID internalID;
	public ContactAPI contactInfo;

	public ParticipantAPI()
	{}

	public String getFormatedID()
	{
		return internalID.tag;
	}

}
