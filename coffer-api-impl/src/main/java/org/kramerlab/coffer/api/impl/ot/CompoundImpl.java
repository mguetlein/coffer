package org.kramerlab.coffer.api.impl.ot;

import org.kramerlab.coffer.api.ModelService;
import org.kramerlab.coffer.api.impl.objects.AbstractCompound;
import org.kramerlab.coffer.api.ot.OpenToxCompound;

public class CompoundImpl extends AbstractCompound implements OpenToxCompound
{

	public String[] getType()
	{
		return new String[] { ModelService.OPENTOX_API_PREFIX + ".Compound" };
	}
}
