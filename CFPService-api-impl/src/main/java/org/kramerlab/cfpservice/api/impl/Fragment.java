package org.kramerlab.cfpservice.api.impl;

import javax.xml.bind.annotation.XmlRootElement;

import org.kramerlab.cfpservice.api.FragmentObj;
import org.kramerlab.cfpservice.api.impl.html.FragmentHtml;

@SuppressWarnings("restriction")
@XmlRootElement
public class Fragment extends FragmentObj
{
	public Fragment()
	{
	}

	public static Fragment find(String modelId, String fragmentId)
	{
		Fragment f = new Fragment();
		f.setModelId(modelId);
		f.setId(fragmentId);
		return f;
	}

	public String getHTML(String maxNumCompounds, String smiles)
	{
		try
		{
			return new FragmentHtml(this, maxNumCompounds, smiles).build();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}
