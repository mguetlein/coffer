package org.kramerlab.cfpservice.api.impl;

import org.kramerlab.cfpservice.api.CompoundObj;
import org.kramerlab.cfpservice.api.impl.html.DefaultHtml;
import org.kramerlab.cfpservice.api.impl.provider.HTMLOwner;
import org.mg.javalib.util.StringUtil;

public class Compound extends CompoundObj implements HTMLOwner
{
	@Override
	public String getId()
	{
		return StringUtil.urlEncodeUTF8(smiles);
	}

	public static String getCompoundURI(String smiles)
	{
		Compound c = new Compound();
		c.setSmiles(smiles);
		return c.getURI();
	}

	class CompoundHTML extends DefaultHtml
	{
		public CompoundHTML()
		{
			super(null, null, "compound/" + getId(), "Compound");
			setPageTitle("Compound " + smiles);
		}

		public String build() throws Exception
		{
			addImage(getImage(depict(smiles, maxMolPicSize)));
			return close();
		}
	}

	@Override
	public String getHTML()
	{
		try
		{
			return new CompoundHTML().build();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

}
