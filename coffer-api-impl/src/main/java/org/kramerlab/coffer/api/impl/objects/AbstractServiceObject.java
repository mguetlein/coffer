package org.kramerlab.coffer.api.impl.objects;

import org.kramerlab.coffer.api.impl.ModelServiceImpl;
import org.kramerlab.coffer.api.objects.ServiceResource;

public abstract class AbstractServiceObject implements ServiceResource
{
	@Override
	public String getURI()
	{
		return ModelServiceImpl.HOST + "/" + getLocalURI();
	}
}
