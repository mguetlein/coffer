package org.kramerlab.cfpservice.api;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement
public class ModelObj implements Serializable
{
	private static final long serialVersionUID = 2L;

	protected String id;
	protected String classValues[];
	protected int activeClassIdx;

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	@XmlTransient
	public int getActiveClassIdx()
	{
		return activeClassIdx;
	}

	public void setActiveClassIdx(int activeClassIdx)
	{
		this.activeClassIdx = activeClassIdx;
	}

	public String[] getClassValues()
	{
		return classValues;
	}

	public void setClassValues(String[] classValues)
	{
		this.classValues = classValues;
	}
}
