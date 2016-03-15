package org.kramerlab.cfpservice.api.impl.objects;

import org.kramerlab.cfpservice.api.impl.ModelServiceImpl;
import org.kramerlab.cfpservice.api.objects.ServiceResource;

public abstract class AbstractServiceObject implements ServiceResource
{
	@Override
	public String getURI()
	{
		return ModelServiceImpl.HOST + "/" + getLocalURI();
	}
}
