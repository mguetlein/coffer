package org.kramerlab.cfpservice.api.impl;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

import javax.ws.rs.core.Response;

import org.kramerlab.cfpservice.api.FragmentObj;
import org.kramerlab.cfpservice.api.ModelService;
import org.kramerlab.cfpservice.api.PredictionObj;
import org.kramerlab.cfpservice.api.impl.util.ServiceCompound;
import org.mg.javalib.util.StopWatchUtil;
import org.mg.javalib.util.StringUtil;
import org.springframework.stereotype.Service;

@Service("modelService#default")
public class ModelServiceImpl implements ModelService
{
	static
	{
		Locale.setDefault(Locale.US);
	}

	public Model[] getModels()
	{
		return Model.listModels();
	}

	public String getModelsHTML()
	{
		return Model.getModelListHTML();
	}

	public Model getModel(String id)
	{
		return Model.find(id);
	}

	public String getModelHTML(String id)
	{
		return Model.find(id).getHTML();
	}

	public Response predict(final String smiles)
	{
		try
		{
			final Model models[] = Model.listModels();
			Prediction.createPrediction(models[0], smiles);
			Thread th = new Thread(new Runnable()
			{
				public void run()
				{
					for (int i = 1; i < models.length; i++)
						Prediction.createPrediction(models[i], smiles);
				}
			});
			th.start();
			return Response.seeOther(new URI("/prediction/" + StringUtil.getMD5(smiles) + "?wait=" + models.length))
					.build();
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

	public String getPredictionsHTML(String predictionId, String wait)
	{
		return Prediction.getHTML(predictionId, wait != null ? Integer.parseInt(wait) : -1);
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

	public String getExternal(String service, String smiles)
	{
		try
		{
			return ServiceCompound.get(ServiceCompound.Service.valueOf(service), smiles);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args)
	{
		new ModelServiceImpl().predict("c1ccccc1");
		StopWatchUtil.print();
	}

}
