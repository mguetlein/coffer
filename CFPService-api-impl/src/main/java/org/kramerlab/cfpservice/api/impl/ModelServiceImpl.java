package org.kramerlab.cfpservice.api.impl;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.core.Response;

import org.kramerlab.cfpservice.api.FragmentObj;
import org.kramerlab.cfpservice.api.ModelService;
import org.kramerlab.cfpservice.api.PredictionObj;
import org.mg.htmlreporting.HTMLReport;
import org.springframework.stereotype.Service;

@Service("modelService#default")
public class ModelServiceImpl implements ModelService
{
	public Model[] getModels()
	{
		return Model.listModels();
	}

	public String getModelsHTML()
	{
		try
		{
			HTMLReport report = new HTMLReport(CFPServiceConfig.title, CFPServiceConfig.header, null,
					CFPServiceConfig.css, false);
			report.newSubsection("Make prediction");
			report.addForm("/", "compound", "Predict");
			report.addGap();
			report.newSubsection("List of models");
			for (Model m : getModels())
				report.addParagraph(HTMLReport.encodeLink(m.getId(), m.getId()));
			return report.close(CFPServiceConfig.footer);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public Model getModel(String id)
	{
		return Model.find(id);
	}

	public InputStream getModelHTML(String id)
	{
		return Model.find(id).getHTML();
	}

	public Response predict(String id, String smiles)
	{
		try
		{
			Prediction p = Prediction.createPrediction(Model.find(id), smiles);
			return Response.seeOther(new URI(id + "/prediction/" + p.getId())).build();
		}
		catch (URISyntaxException e)
		{
			throw new RuntimeException(e);
		}
	}

	public InputStream getValidationChart(String id)
	{
		return Model.find(id).getValidationChart();
	}

	public PredictionObj getPrediction(String modelId, String predictionId)
	{
		return Prediction.find(modelId, predictionId);
	}

	public InputStream getPredictionHTML(String modelId, String predictionId)
	{
		return Prediction.find(modelId, predictionId).getHTML();
	}

	public FragmentObj getFragment(String modelId, String fragmentId)
	{
		return Fragment.find(modelId, fragmentId);
	}

	public InputStream getFragmentHTML(String modelId, String fragmentId)
	{
		return Fragment.find(modelId, fragmentId).getHTML();
	}
}
