package org.kramerlab.cfpservice.api.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

import org.kramerlab.cfpminer.CFPtoArff;
import org.kramerlab.cfpservice.api.PredictionObj;
import org.kramerlab.cfpservice.api.impl.html.PredictionHtml;
import org.kramerlab.cfpservice.api.impl.html.PredictionsHtml;
import org.kramerlab.cfpservice.api.impl.persistance.PersistanceAdapter;
import org.mg.cdklib.CDKConverter;
import org.mg.javalib.util.ArrayUtil;
import org.mg.javalib.util.StringUtil;
import org.mg.wekalib.attribute_ranking.AttributeProvidingClassifier;
import org.mg.wekalib.attribute_ranking.PredictionAttribute;
import org.mg.wekalib.attribute_ranking.PredictionAttributeComputation;
import org.openscience.cdk.interfaces.IAtomContainer;

import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

@SuppressWarnings("restriction")
@XmlRootElement
public class Prediction extends PredictionObj
{
	private static final long serialVersionUID = 3L;

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
			return CDKConverter.parseSmiles(smiles);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public static String[] findAllPredictions(String... modelIds)
	{
		return PersistanceAdapter.INSTANCE.findAllPredictions(modelIds);
	}

	public static Prediction[] find(String predictionId)
	{
		List<Prediction> p = new ArrayList<Prediction>();
		for (Model m : Model.listModels())
			if (Prediction.exists(m.getId(), predictionId))
				p.add(Prediction.find(m.getId(), predictionId));
		return ArrayUtil.toArray(p);
	}

	public static Prediction find(String modelId, String predictionId)
	{
		return PersistanceAdapter.INSTANCE.readPrediction(modelId, predictionId);
	}

	public static boolean exists(String modelId, String predictionId)
	{
		return PersistanceAdapter.INSTANCE.predictionExists(modelId, predictionId);
	}

	public Date getDate()
	{
		return PersistanceAdapter.INSTANCE.getPredictionDate(modelId, id);
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
			{
				PersistanceAdapter.INSTANCE.updateDate(m.getId(), predictionId);
				return Prediction.find(m.getId(), predictionId);
			}

			Prediction p = new Prediction();
			p.smiles = smiles;

			Instances data = CFPtoArff.getTestDataset(m.getCFPMiner(), "DUD_vegfr2",
					p.getMolecule());
			Instance inst = data.get(0);
			data.setClassIndex(data.numAttributes() - 1);
			double dist[] = ((Classifier) m.getClassifier()).distributionForInstance(inst);
			int maxIdx = ArrayUtil.getMaxIndex(dist);

			p.id = predictionId;
			p.modelId = m.getId();
			p.predictedIdx = maxIdx;
			p.predictedDistribution = dist;
			p.trainingActivity = m.getCFPMiner().getTrainingActivity(smiles);

			Set<Integer> atts;
			if (m.getClassifier() instanceof AttributeProvidingClassifier)
				atts = ((AttributeProvidingClassifier) m.getClassifier())
						.getAttributesEmployedForPrediction(inst);
			else
				atts = PredictionAttributeComputation.allAttributes(inst);

			p.setPredictionAttributes(PredictionAttributeComputation.compute(
					(Classifier) m.getClassifier(), inst, dist, atts));

			PersistanceAdapter.INSTANCE.savePrediction(p);

			return p;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public static String getHTML(String predictionId, int wait)
	{
		try
		{
			return new PredictionsHtml(find(predictionId), wait).build();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public String getHTML(String maxNumFragments)
	{
		try
		{
			return new PredictionHtml(this, maxNumFragments).build();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

}
