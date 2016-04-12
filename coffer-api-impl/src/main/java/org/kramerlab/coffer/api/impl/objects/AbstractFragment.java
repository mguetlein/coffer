package org.kramerlab.coffer.api.impl.objects;

import org.kramerlab.coffer.api.ModelService;
import org.kramerlab.coffer.api.impl.html.FragmentHtml;
import org.kramerlab.coffer.api.impl.ot.FragmentImpl;
import org.kramerlab.coffer.api.impl.provider.HTMLOwner;
import org.kramerlab.coffer.api.objects.Fragment;

public abstract class AbstractFragment extends AbstractServiceObject implements HTMLOwner, Fragment
{
	protected String modelId;

	protected String id;

	protected int maxNumFragments;

	protected String smiles;

	public static Fragment find(String modelId, String fragmentId)
	{
		return find(modelId, fragmentId, ModelService.DEFAULT_NUM_ENTRIES, null);
	}

	public static Fragment find(String modelId, String fragmentId, int maxNumFragments,
			String smiles)
	{
		AbstractFragment f = new FragmentImpl();
		f.modelId = modelId;
		f.id = fragmentId;
		f.maxNumFragments = maxNumFragments;
		f.smiles = smiles;
		return f;
	}

	@Override
	public String getId()
	{
		return id;
	}

	@Override
	public String getModelId()
	{
		return modelId;
	}

	@Override
	public String getHTML()
	{
		return new FragmentHtml(this, smiles, maxNumFragments).build();
	}

	@Override
	public String getLocalURI()
	{
		return modelId + "/fragment/" + id;
	}
}
