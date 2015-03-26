package org.kramerlab.cfpservice.api.impl;

import java.util.Comparator;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.kramerlab.cfpminer.CDKUtil;
import org.kramerlab.cfpminer.CFPtoArff;
import org.kramerlab.cfpservice.api.PredictionObj;
import org.kramerlab.cfpservice.api.impl.persistance.PersistanceAdapter;
import org.kramerlab.extendedrandomforests.html.ExtendedHtmlReport;
import org.kramerlab.extendedrandomforests.html.PredictionReport;
import org.kramerlab.extendedrandomforests.weka.PredictionAttribute;
import org.mg.htmlreporting.HTMLReport;
import org.mg.javalib.datamining.ResultSet;
import org.mg.javalib.util.ArrayUtil;
import org.mg.javalib.util.StringUtil;
import org.openscience.cdk.interfaces.IAtomContainer;

import weka.core.Instance;
import weka.core.Instances;

@SuppressWarnings("restriction")
@XmlRootElement
public class Prediction extends PredictionObj
{
	private static final long serialVersionUID = 1L;

	@XmlAttribute
	protected List<PredictionAttribute> predictionAttributes;

	public Prediction()
	{
	}

	public void setPredictionAttributes(List<PredictionAttribute> predictionAttributes)
	{
		this.predictionAttributes = predictionAttributes;
	}

	public List<PredictionAttribute> getPredictionAttributes()
	{
		return predictionAttributes;
	}

	public IAtomContainer getMolecule()
	{
		try
		{
			return CDKUtil.parseSmiles(smiles);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public static String[] findLastPredictions(final String modelId)
	{
		return PersistanceAdapter.INSTANCE.findLastPredictions(modelId);
	}

	public static Prediction find(String modelId, String predictionId)
	{
		return PersistanceAdapter.INSTANCE.readPrediction(modelId, predictionId);
	}

	public static boolean exists(String modelId, String predictionId)
	{
		return PersistanceAdapter.INSTANCE.predictionExists(modelId, predictionId);
	}

	public static void main(String[] args) throws Exception
	{
		createPrediction(Model.find("DUD_vegfr2"), "c1c(CC(=O)O)cncc1");
	}

	public static Prediction createPrediction(Model m, String smiles)
	{
		try
		{
			String predictionId = StringUtil.getMD5(smiles);
			if (exists(m.getId(), predictionId))
				return Prediction.find(m.getId(), predictionId);

			Prediction p = new Prediction();
			p.smiles = smiles;

			Instances data = CFPtoArff.getTestDataset(m.getCFPMiner(), "DUD_vegfr2", p.getMolecule());
			Instance inst = data.get(0);
			data.setClassIndex(data.numAttributes() - 1);
			double dist[] = m.getExtendedRandomForest().distributionForInstance(inst);
			int maxIdx = ArrayUtil.getMaxIndex(dist);

			p.id = predictionId;
			p.modelId = m.getId();
			p.predictedIdx = maxIdx;
			p.predictedDistribution = dist;
			p.setPredictionAttributes(m.getExtendedRandomForest().getPredictionAttributes(inst, dist));

			PersistanceAdapter.INSTANCE.savePrediction(p);

			return p;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public static String getHTML(String predictionId)
	{
		try
		{
			String smiles = null;
			ResultSet res = new ResultSet();
			for (Model m : Model.listModels())
			{
				if (Prediction.exists(m.getId(), predictionId))
				{
					int idx = res.addResult();
					res.setResultValue(idx, "Model",
							HTMLReport.encodeLink("/" + m.getId() + "/prediction/" + predictionId, m.getId()));
					Prediction p = Prediction.find(m.getId(), predictionId);
					smiles = p.getSmiles();
					res.setResultValue(
							idx,
							"Prediction",
							HTMLReport.getHTMLCode(PredictionReport.getPredictionString(p.predictedDistribution,
									m.getClassValues(), p.getPredictedIdx(), true)));
					res.setResultValue(idx, "p", p.predictedDistribution[m.getActiveClassIdx()]);
					//							HTMLReport.encodeLink("/" + m.getId() + "/prediction/" + predictionId,
					//									p.getPredictedClass())
				}
			}
			res.sortResults("p", new Comparator<Object>()
			{
				public int compare(Object o1, Object o2)
				{
					return ((Double) o2).compareTo((Double) o1);
				}
			});
			res.removePropery("p");

			HTMLReport rep = new HTMLReport("Prediction of compound " + smiles);
			CFPServiceConfig.initPredictionReport(rep, predictionId);
			DepictServiceImpl imageProvider = new DepictServiceImpl();
			rep.addImage(rep.getImage(imageProvider.drawCompound(smiles, ExtendedHtmlReport.molPicSize),
					imageProvider.hrefCompound(smiles), true));
			rep.addGap();
			rep.addTable(res);
			return rep.close();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public String getHTML()
	{
		try
		{
			Model m = Model.find(modelId);
			PredictionReport rep = new PredictionReport(getSmiles(), getPredictedDistribution(),
					getPredictionAttributes(), modelId, m.getExtendedRandomForest(), m.getCFPMiner(),
					m.getTrainingDataSmiles());
			CFPServiceConfig.initPredictionReport(rep, modelId, id);
			rep.setImageProvider(new DepictServiceImpl());
			return rep.buildReport();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}
