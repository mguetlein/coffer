package org.kramerlab.cfpservice.api;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@SuppressWarnings("restriction")
@XmlRootElement
public class ModelObj implements Serializable
{
	private static final long serialVersionUID = 2L;

	@XmlAttribute
	protected String id;
	@XmlAttribute
	protected String classValues[];
	@XmlAttribute
	protected int activeClassIdx;

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

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
