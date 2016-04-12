package org.kramerlab.coffer.api.impl.ot;

import org.kramerlab.coffer.api.ModelService;
import org.kramerlab.coffer.api.impl.objects.AbstractFragment;
import org.kramerlab.coffer.api.ot.OpenToxFragment;

public class FragmentImpl extends AbstractFragment implements OpenToxFragment
{
	@Override
	public String[] getType()
	{
		return new String[] { ModelService.OPENTOX_API_PREFIX + ".Feature",
				ModelService.OPENTOX_API_PREFIX + ".NominalFeature" };
	}

	@Override
	public String getLocalURI()
	{
		return getModelId() + "/fragment/" + getId();
	}
}
