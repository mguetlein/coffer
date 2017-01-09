package org.kramerlab.coffer.api.impl.objects;

import org.kramerlab.coffer.api.impl.html.DefaultHtml;
import org.kramerlab.coffer.api.impl.provider.HTMLOwner;
import org.kramerlab.coffer.api.objects.Compound;
import org.mg.javalib.util.StringUtil;

public abstract class AbstractCompound extends AbstractServiceObject implements HTMLOwner, Compound
{
	protected String smiles;

	public void setSmiles(String smiles)
	{
		this.smiles = smiles;
	}

	public String getSmiles()
	{
		return smiles;
	}

	class CompoundHTML extends DefaultHtml
	{
		public CompoundHTML()
		{
			super("Compound", getLocalURI(), "Compound", null, null);
			setPageTitle("Compound " + smiles);
		}

		public String build()
		{
			addImage(getImage(depict(smiles, maxMolSizeLarge)));
			return close();
		}
	}

	@Override
	public String getHTML()
	{
		return new CompoundHTML().build();
	}

	@Override
	public String getLocalURI()
	{
		return "compound/" + StringUtil.urlEncodeUTF8(smiles);
	}

}
