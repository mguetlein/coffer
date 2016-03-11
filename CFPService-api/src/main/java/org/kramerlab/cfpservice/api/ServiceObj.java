package org.kramerlab.cfpservice.api;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

public abstract class ServiceObj
{
	@XmlElement
	public String getUri()
	{
		return ModelService.SERVICE_HOME + getPath() + getId();
	}

	@XmlTransient
	public abstract String getPath();

	public abstract String getId();
}
