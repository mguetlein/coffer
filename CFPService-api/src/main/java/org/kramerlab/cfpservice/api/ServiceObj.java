package org.kramerlab.cfpservice.api;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

public abstract class ServiceObj
{
	public static String HOST;

	@XmlElement(name = "identifier", namespace = ModelService.DC_NAMESPACE)
	public String getURI()
	{
		return HOST + getPath() + getId();
	}

	@XmlTransient
	public abstract String getPath();

	public abstract String getId();
}
