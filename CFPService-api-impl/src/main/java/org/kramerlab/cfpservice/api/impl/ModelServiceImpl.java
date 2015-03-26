package org.kramerlab.cfpservice.api.impl;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.core.Response;

import org.kramerlab.cfpservice.api.FragmentObj;
import org.kramerlab.cfpservice.api.ModelService;
import org.kramerlab.cfpservice.api.PredictionObj;
import org.mg.htmlreporting.HTMLReport;
import org.mg.javalib.datamining.ResultSet;
import org.mg.javalib.util.StringUtil;
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
			HTMLReport report = new HTMLReport();
			CFPServiceConfig.initReport(report);
			report.newSubsection("Make prediction");
			report.addForm("/", "compound", "Predict", "Please insert SMILES string");
			report.addGap();
			ResultSet set = new ResultSet();
			for (Model m : getModels())
			{
				int idx = set.addResult();
				set.setResultValue(idx, "List of models", HTMLReport.encodeLink(m.getId(), m.getId()));
			}
			report.addTable(set);
			return report.close();
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

	public String getModelHTML(String id)
	{
		return Model.find(id).getHTML();
	}

	public Response predict(String smiles)
	{
		try
		{
			for (Model m : Model.listModels())
				Prediction.createPrediction(m, smiles);
			return Response.seeOther(new URI("/prediction/" + StringUtil.getMD5(smiles))).build();
		}
		catch (URISyntaxException e)
		{
			throw new RuntimeException(e);
		}
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

	public String getPredictionHTML(String predictionId)
	{
		return Prediction.getHTML(predictionId);
	}

	public String getPredictionHTML(String modelId, String predictionId)
	{
		return Prediction.find(modelId, predictionId).getHTML();
	}

	public FragmentObj getFragment(String modelId, String fragmentId)
	{
		return Fragment.find(modelId, fragmentId);
	}

	public String getFragmentHTML(String modelId, String fragmentId)
	{
		return Fragment.find(modelId, fragmentId).getHTML();
	}
}
