package org.kramerlab.cfpservice.api;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@SuppressWarnings("restriction")
@XmlRootElement
public class FragmentObj implements Serializable
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
