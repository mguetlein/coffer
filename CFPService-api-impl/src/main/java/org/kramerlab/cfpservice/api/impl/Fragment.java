package org.kramerlab.cfpservice.api.impl;

import java.io.FileInputStream;

import javax.xml.bind.annotation.XmlRootElement;

import org.kramerlab.cfpservice.api.FragmentObj;
import org.kramerlab.cfpservice.api.impl.persistance.PersistanceAdapter;
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

	public FileInputStream getHTML()
	{
		String file = PersistanceAdapter.INSTANCE.getFragmentHTMLFile(modelId, id);
		try
		{
			//			if (!new File(file).exists())
			//			{
			Model m = Model.find(modelId);
			AttributeReport rep = new AttributeReport(m.getExtendedRandomForest(), m.getCFPMiner(),
					m.getTrainingDataSmiles());
			//			rep.setTestInstance(getSmiles(), getPredictedDistribution(), getPredictionAttributes());
			rep.setAttribute(Integer.parseInt(id) - 1);
			rep.setImageProvider(new DepictServiceImpl());
			rep.setReportTitles(CFPServiceConfig.title, CFPServiceConfig.header, CFPServiceConfig.css,
					CFPServiceConfig.footer);
			rep.setModel(modelId, "/" + modelId);
			rep.buildReport(file);
			//			}
			return new FileInputStream(file);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}
