package org.kramerlab.cfpservice.api;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlType(name = "Compound", namespace = ModelService.OPENTOX_API)
public abstract class CompoundObj extends ServiceObj
{
	protected String smiles;

	@Override
	public abstract String getId();

	@Override
	public String getPath()
	{
		return "/compound/";
	}

	public void setSmiles(String smiles)
	{
		this.smiles = smiles;
	}

	public String getSmiles()
	{
		return smiles;
	}
}
