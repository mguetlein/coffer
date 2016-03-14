package org.kramerlab.cfpservice.api;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlType(name = "Model", namespace = ModelService.OPENTOX_API)
public class ModelObj extends ServiceObj implements Serializable
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

	@Override
	public String getPath()
	{
		return "/";
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

	// ot-api fields 

	@XmlAttribute(namespace = ModelService.OPENTOX_API)
	public String getDependentVariables()
	{
		return getURI() + "/measured";
	}

	@XmlAttribute(namespace = ModelService.OPENTOX_API)
	public String[] getPredictedVariables()
	{
		return new String[] { getURI() + "/predicted", getURI() + "/probability" };
	}
}
