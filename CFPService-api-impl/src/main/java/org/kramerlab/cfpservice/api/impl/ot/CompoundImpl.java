package org.kramerlab.cfpservice.api.impl.ot;

import org.kramerlab.cfpservice.api.ModelService;
import org.kramerlab.cfpservice.api.impl.objects.AbstractCompound;
import org.kramerlab.cfpservice.api.ot.OpenToxCompound;

public class CompoundImpl extends AbstractCompound implements OpenToxCompound
{

	public String[] getType()
	{
		return new String[] { ModelService.OPENTOX_API_PREFIX + ".Compound" };
	}
}
