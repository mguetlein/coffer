package org.kramerlab.cfpservice.api.impl;

import javax.xml.bind.annotation.XmlRootElement;

import org.kramerlab.cfpservice.api.FragmentObj;
import org.kramerlab.extendedrandomforests.html.AttributeReport;

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

	public String getHTML()
	{
		try
		{
			Model m = Model.find(modelId);
			AttributeReport rep = new AttributeReport(Integer.parseInt(id) - 1, m.getExtendedRandomForest(),
					m.getCFPMiner(), m.getTrainingDataSmiles());
			CFPServiceConfig.initFragmentReport(rep, modelId, id);
			return rep.buildReport();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}
