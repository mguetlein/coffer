package org.kramerlab.cfpservice.api.impl;

import javax.xml.bind.annotation.XmlRootElement;

import org.kramerlab.cfpservice.api.FragmentObj;
import org.kramerlab.cfpservice.api.ModelService;
import org.kramerlab.cfpservice.api.impl.html.FragmentHtml;
import org.kramerlab.cfpservice.api.impl.provider.HTMLOwner;

@XmlRootElement
public class Fragment extends FragmentObj implements HTMLOwner
{
	private static final long serialVersionUID = 1L;

	protected transient int maxNumFragments;

	protected transient String smiles;

	public Fragment()
	{
	}

	public static Fragment find(String modelId, String fragmentId)
	{
		return find(modelId, fragmentId, ModelService.DEFAULT_NUM_ENTRIES, null);
	}

	public static Fragment find(String modelId, String fragmentId, int maxNumFragments,
			String smiles)
	{
		Fragment f = new Fragment();
		f.setModelId(modelId);
		f.setId(fragmentId);
		f.maxNumFragments = maxNumFragments;
		f.smiles = smiles;
		return f;
	}

	public String getSmiles()
	{
		return smiles;
	}

	public int getMaxNumFragments()
	{
		return maxNumFragments;
	}

	@Override
	public String getHTML()
	{
		try
		{
			return new FragmentHtml(this).build();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}
