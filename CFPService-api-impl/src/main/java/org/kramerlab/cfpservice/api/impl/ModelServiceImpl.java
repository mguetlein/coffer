package org.kramerlab.cfpservice.api.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Locale;

import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.kramerlab.cfpservice.api.ModelService;
import org.kramerlab.cfpservice.api.impl.html.AppDomainHtml;
import org.kramerlab.cfpservice.api.impl.html.DocHtml;
import org.kramerlab.cfpservice.api.impl.html.PredictionHtml.HideFragments;
import org.kramerlab.cfpservice.api.impl.objects.AbstractFragment;
import org.kramerlab.cfpservice.api.impl.objects.AbstractModel;
import org.kramerlab.cfpservice.api.impl.objects.AbstractPrediction;
import org.kramerlab.cfpservice.api.impl.ot.CompoundImpl;
import org.kramerlab.cfpservice.api.impl.ot.PredictionImpl;
import org.kramerlab.cfpservice.api.impl.util.CompoundInfo;
import org.kramerlab.cfpservice.api.impl.util.RESTUtil;
import org.kramerlab.cfpservice.api.objects.Compound;
import org.kramerlab.cfpservice.api.objects.Fragment;
import org.kramerlab.cfpservice.api.objects.Model;
import org.kramerlab.cfpservice.api.objects.Prediction;
import org.mg.cdklib.CDKConverter;
import org.mg.javalib.util.StopWatchUtil;
import org.mg.javalib.util.StringUtil;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.springframework.stereotype.Service;

@Service("modelService#default")
public class ModelServiceImpl implements ModelService
{
	public static String HOST;

	static
	{
		Locale.setDefault(Locale.US);
	}

	@Override
	public String getDocumentation()
	{
		return new DocHtml().build();
	}

	@Override
	public Model[] getModels()
	{
		return AbstractModel.listModels();
	}

	@Override
	public Model getModel(String id)
	{
		return AbstractModel.find(id);
	}

	@Override
	public Response predict(final String smiles)
	{
		try
		{
			CDKConverter.validateSmiles(smiles);
			final Model models[] = AbstractModel.listModels();
			AbstractPrediction.createPrediction(models[0], smiles, false);
			Thread th = new Thread(new Runnable()
			{
				public void run()
				{
					for (int i = 1; i < models.length; i++)
						AbstractPrediction.createPrediction(models[i], smiles, false);
				}
			});
			th.start();
			return Response
					.seeOther(new URI(
							"/prediction/" + StringUtil.getMD5(smiles) + "?wait=" + models.length))
					.build();
		}
		catch (InvalidSmilesException e)
		{
			throw new IllegalArgumentException(e.getMessage(), e);
		}
		catch (URISyntaxException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public Response predict(String id, String compoundSmiles, String compoundURI)
	{
		try
		{
			if ((compoundSmiles == null && compoundURI == null)
					|| (compoundSmiles != null && compoundURI != null))
				throw new IllegalArgumentException(
						"pls provide either 'compoundSmiles' or 'compoundURI'");
			if (compoundSmiles == null)
				compoundSmiles = RESTUtil.get(compoundURI, ModelService.MEDIA_TYPE_CHEMICAL_SMILES);
			CDKConverter.validateSmiles(compoundSmiles);
			Prediction p = AbstractPrediction.createPrediction(AbstractModel.find(id),
					compoundSmiles, true);
			return Response.seeOther(new URI(p.getLocalURI())).build();
		}
		catch (InvalidSmilesException e)
		{
			throw new IllegalArgumentException(e.getMessage(), e);
		}
		catch (URISyntaxException | IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public InputStream getValidationChart(String id)
	{
		return ((AbstractModel) AbstractModel.find(id)).getValidationChart();
	}

	@Override
	public Prediction getPrediction(String modelId, String predictionId, String hideFragments,
			String maxNumFragments)
	{
		int num = maxNumFragments == null ? ModelService.DEFAULT_NUM_ENTRIES
				: Integer.parseInt(maxNumFragments);
		return AbstractPrediction.find(modelId, predictionId,
				HideFragments.fromString(hideFragments), num);
	}

	@Override
	public Prediction[] getPredictions(String predictionId, String wait)
	{
		int num = wait == null ? -1 : Integer.parseInt(wait);
		Prediction[] res = AbstractPrediction.find(predictionId);
		if (res.length < num) // add trailing empty predictions
		{
			res = Arrays.copyOf(res, num);
			for (int i = 0; i < res.length; i++)
				if (res[i] == null)
					res[i] = new PredictionImpl();
		}
		return res;
	}

	@Override
	public Fragment getFragment(String modelId, String fragmentId, String maxNumFragments,
			String smiles)
	{
		int num = maxNumFragments == null ? ModelService.DEFAULT_NUM_ENTRIES
				: Integer.parseInt(maxNumFragments);
		return AbstractFragment.find(modelId, fragmentId, num, smiles);
	}

	@Override
	public String getCompoundInfo(String service, String smiles)
	{
		try
		{
			return CompoundInfo.getHTML(CompoundInfo.Service.valueOf(service), smiles);
		}
		catch (JSONException | IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public InputStream depict(String smiles, String size)
	{
		return DepictService.depict(smiles, size);
	}

	@Override
	public InputStream depictMatch(String smiles, String size, String atoms,
			String highlightOutgoingBonds, String activating, String crop)
	{
		return DepictService.depictMatch(smiles, size, atoms, highlightOutgoingBonds, activating,
				crop);
	}

	@Override
	public InputStream depictMultiMatch(String smiles, String size, String model)
	{
		return DepictService.depictMultiMatch(smiles, size, model);
	}

	public static void main(String[] args)
	{
		new ModelServiceImpl().predict("c1ccccc1");
		StopWatchUtil.print();
	}

	@Override
	public Compound getCompound(String smiles)
	{
		CompoundImpl c = new CompoundImpl();
		c.setSmiles(smiles);
		return c;
	}

	@Override
	public String getAppDomain(String modelId, String smiles, String maxNumNeighbors)
	{
		int num = maxNumNeighbors == null ? ModelService.DEFAULT_NUM_ENTRIES
				: Integer.parseInt(maxNumNeighbors);
		return new AppDomainHtml(AbstractModel.find(modelId), smiles, num).build();
	}

	@Override
	public Response predictAppDomain(String modelId, String smiles)
	{
		try
		{
			CDKConverter.validateSmiles(smiles);
			return Response.seeOther(new URI(
					"/" + modelId + "/appdomain?smiles=" + StringUtil.urlEncodeUTF8(smiles)))
					.build();
		}
		catch (InvalidSmilesException e)
		{
			throw new IllegalArgumentException(e.getMessage(), e);
		}
		catch (URISyntaxException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public InputStream depictAppDomain(String modelId, String smiles)
	{
		return DepictService.depictAppDomain(AbstractModel.find(modelId), smiles);
	}

	@Override
	public InputStream depictActiveIcon(String probability, String drawHelp)
	{
		return DepictService.depictActiveIcon(Double.valueOf(probability),
				drawHelp != null && Boolean.valueOf(drawHelp));
	}
}
