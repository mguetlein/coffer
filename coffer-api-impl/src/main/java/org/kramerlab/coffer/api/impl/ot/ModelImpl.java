package org.kramerlab.coffer.api.impl.ot;

import org.kramerlab.coffer.api.ModelService;
import org.kramerlab.coffer.api.impl.objects.AbstractModel;
import org.kramerlab.coffer.api.ot.OpenToxModel;

public class ModelImpl extends AbstractModel implements OpenToxModel
{
	private static final long serialVersionUID = 1L;

	@Override
	public String[] getType()
	{
		return new String[] { ModelService.OPENTOX_API_PREFIX + ".Model" };
	}

	public String getDependentVariables()
	{
		return getURI() + "/feature/measured";
	}

	public String[] getPredictedVariables()
	{
		return new String[] { getURI() + "/feature/predicted", getURI() + "/feature/probability" };
	}
}
