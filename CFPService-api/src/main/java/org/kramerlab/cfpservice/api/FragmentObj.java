package org.kramerlab.cfpservice.api;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlType(name = "Fragment")
public class FragmentObj extends ServiceObj implements Serializable
{
	private static final long serialVersionUID = 1L;

	/**
	 * fragment id = attribute-idx + 1
	 */
	protected String id;
	protected String modelId;

	public String getId()
	{
		return id;
	}

	@Override
	public String getPath()
	{
		return "/" + modelId + "/fragment/";
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getModelId()
	{
		return modelId;
	}

	public void setModelId(String modelId)
	{
		this.modelId = modelId;
	}
}
