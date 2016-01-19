package org.kramerlab.cfpservice.api.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

import org.kramerlab.cfpminer.CFPtoArff;
import org.kramerlab.cfpservice.api.PredictionObj;
import org.kramerlab.cfpservice.api.impl.html.PredictionHtml;
import org.kramerlab.cfpservice.api.impl.html.PredictionsHtml;
import org.kramerlab.cfpservice.api.impl.persistance.PersistanceAdapter;
import org.mg.cdklib.CDKConverter;
import org.mg.cdklib.cfp.CFPFragment;
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
	private static final long serialVersionUID = 6L;

	protected List<SubgraphPredictionAttribute> predictionAttributes;

	public Prediction()
	{
	}

	public void setPredictionAttributes(List<SubgraphPredictionAttribute> predictionAttributes)
	{
		this.predictionAttributes = predictionAttributes;
	}

	public List<SubgraphPredictionAttribute> getPredictionAttributes()
	{
		if (predictionAttributes == null)
		{
			initPrediction(true);
			PersistanceAdapter.INSTANCE.savePrediction(this);
		}
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
		createPrediction(Model.find("DUD_vegfr2"), "c1c(CC(=O)O)cncc1", true);
	}

	public static Prediction createPrediction(Model m, String smiles,
			boolean createPredictionAttributes)
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
			p.id = predictionId;
			p.modelId = m.getId();

			p.initPrediction(createPredictionAttributes);
			PersistanceAdapter.INSTANCE.savePrediction(p);
			return p;

		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	private void initPrediction(boolean createPredictionAttributes)
	{
		try
		{
			Model m = Model.find(modelId);

			Instances data = CFPtoArff.getTestDataset(m.getCFPMiner(), "DUD_vegfr2", getMolecule());
			Instance inst = data.get(0);
			data.setClassIndex(data.numAttributes() - 1);
			double dist[] = ((Classifier) m.getClassifier()).distributionForInstance(inst);
			int maxIdx = ArrayUtil.getMaxIndex(dist);

			predictedIdx = maxIdx;
			predictedDistribution = dist;
			trainingActivity = m.getCFPMiner().getTrainingActivity(smiles);

			if (createPredictionAttributes)
			{
				//				System.out.println("creating prediction attributes");

				Set<Integer> atts;
				if (m.getClassifier() instanceof AttributeProvidingClassifier)
					atts = ((AttributeProvidingClassifier) m.getClassifier())
							.getAttributesEmployedForPrediction(inst);
				else
					atts = PredictionAttributeComputation.allAttributes(inst);

				//				System.out.println(atts);
				//				for (int i = 0; i < m.getCFPMiner().getNumFragments(); i++)
				//					System.out.println((i + 1) + " " + m.getCFPMiner().getFragmentViaIdx(i));

				HashMap<Integer, Set<Integer>> subAndSuperAtts = new HashMap<Integer, Set<Integer>>();
				for (Integer a : atts)
				{
					CFPFragment f = m.getCFPMiner().getFragmentViaIdx(a);
					//					System.out.println("\nfragment " + (a + 1) + " " + f);
					Set<CFPFragment> fs;
					if (m.getCFPMiner().getFragmentsForTestCompound(smiles).contains(f))
					{
						//						System.out.println(
						//								"present in test compound\nalso disable super fragments: ");
						fs = m.getCFPMiner().getSuperFragments(f);
					}
					else
					{
						//						System.out.println(
						//								"NOT present in test compound\nalso enable sub fragments: ");
						fs = m.getCFPMiner().getSubFragments(f);
					}
					Set<Integer> fIdx = new HashSet<Integer>();
					if (fs != null)
						for (CFPFragment frag : fs)
						{
							fIdx.add(m.getCFPMiner().getIdxForFragment(frag));
							//							System.out.println(
							//									(m.getCFPMiner().getIdxForFragment(frag) + 1) + " " + frag);
						}
					//					System.out.println();
					subAndSuperAtts.put(a, fIdx);
				}

				List<PredictionAttribute> pAtts = PredictionAttributeComputation
						.compute((Classifier) m.getClassifier(), inst, dist, atts, subAndSuperAtts);
				List<SubgraphPredictionAttribute> l = new ArrayList<SubgraphPredictionAttribute>();
				for (PredictionAttribute pa : pAtts)
				{
					int a = pa.getAttribute();
					CFPFragment f = m.getCFPMiner().getFragmentViaIdx(a);
					Set<CFPFragment> fs = m.getCFPMiner().getSubFragments(f);
					boolean isSuper = (fs != null && fs.size() > 0);
					l.add(new SubgraphPredictionAttribute(pa.getAttribute(),
							pa.getAlternativeDistributionForInstance(), pa.getDiffToOrigProp(),
							isSuper));
				}
				predictionAttributes = l;
			}
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

	public String getHTML(boolean showSuper, String maxNumFragments)
	{
		try
		{
			return new PredictionHtml(this, showSuper, maxNumFragments).build();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

}
