package org.kramerlab.cfpservice.api.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.kramerlab.cfpminer.weka.eval2.CFPtoArff;
import org.kramerlab.cfpservice.api.ModelService;
import org.kramerlab.cfpservice.api.PredictionObj;
import org.kramerlab.cfpservice.api.impl.html.PredictionHtml;
import org.kramerlab.cfpservice.api.impl.html.PredictionHtml.HideFragments;
import org.kramerlab.cfpservice.api.impl.html.PredictionsHtml;
import org.kramerlab.cfpservice.api.impl.persistance.PersistanceAdapter;
import org.kramerlab.cfpservice.api.impl.provider.HTMLOwner;
import org.mg.cdklib.CDKConverter;
import org.mg.cdklib.cfp.CFPFragment;
import org.mg.cdklib.cfp.CFPMiner;
import org.mg.javalib.util.ArrayUtil;
import org.mg.javalib.util.StringUtil;
import org.mg.wekalib.attribute_ranking.PredictionAttribute;
import org.mg.wekalib.attribute_ranking.PredictionAttributeComputation;
import org.openscience.cdk.interfaces.IAtomContainer;

import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

@XmlRootElement
@XmlType(name = "Prediction")
public class Prediction extends PredictionObj implements HTMLOwner
{
	private static final long serialVersionUID = 9L;

	protected List<SubgraphPredictionAttribute> predictionAttributes;

	protected transient HideFragments hideFragments;

	protected transient int maxNumFragments;

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

	public HideFragments getHideFragments()
	{
		return hideFragments;
	}

	public int getMaxNumFragments()
	{
		return maxNumFragments;
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
		return find(modelId, predictionId, HideFragments.NONE, ModelService.DEFAULT_NUM_ENTRIES);
	}

	public static Prediction find(String modelId, String predictionId, HideFragments hideFragments,
			int maxNumFragments)
	{
		Prediction p = PersistanceAdapter.INSTANCE.readPrediction(modelId, predictionId);
		p.hideFragments = hideFragments;
		p.maxNumFragments = maxNumFragments;
		return p;
	}

	public static boolean exists(String modelId, String predictionId)
	{
		return PersistanceAdapter.INSTANCE.predictionExists(modelId, predictionId);
	}

	public Date getDate()
	{
		return PersistanceAdapter.INSTANCE.getPredictionDate(modelId, id);
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
			CFPMiner miner = m.getCFPMiner();

			Instances data = CFPtoArff.getTestDataset(miner, "DUD_vegfr2", getMolecule());
			Instance inst = data.get(0);
			data.setClassIndex(data.numAttributes() - 1);

			double dist[] = ((Classifier) m.getClassifier()).distributionForInstance(inst);

			int maxIdx = ArrayUtil.getMaxIndex(dist);
			predictedIdx = maxIdx;
			predictedDistribution = dist;
			trainingActivity = miner.getTrainingActivity(smiles);

			if (createPredictionAttributes)
			{
				// for each attribute, create a set of attributes that will be toggled with the attribute
				HashMap<Integer, Set<Integer>> subAndSuperAtts = new HashMap<Integer, Set<Integer>>();
				for (int a = 0; a < data.numAttributes(); a++)
				{
					if (data.classIndex() == a)
						continue;
					CFPFragment f = miner.getFragmentViaIdx(a);
					//					System.out.println("\nfragment " + (a + 1) + " " + f);
					Set<CFPFragment> fs;
					if (miner.getFragmentsForTestCompound(smiles).contains(f))
					{
						//	System.out.println("fragment is present in test compound\nalso disable super fragments: ");
						fs = miner.getSuperFragments(f);

						//	System.out.println("fragment is present in test compound\nalso disable included sub fragments within test compound: ");
						Set<CFPFragment> fs2 = miner.getIncludedFragments(f, smiles);
						//System.out.println("fs2: " + f + " includes " + fs2);
						if (fs2 != null)
						{
							if (fs == null)
								fs = fs2;
							else
								fs.addAll(fs2);
						}
					}
					else
					{
						//	System.out.println("fragment is NOT present in test compound\nalso enable sub fragments: ");
						fs = miner.getSubFragments(f);
					}
					Set<Integer> fIdx = new HashSet<Integer>();
					if (fs != null)
						for (CFPFragment frag : fs)
						{
							fIdx.add(miner.getIdxForFragment(frag));
							//	System.out.println((miner.getIdxForFragment(frag) + 1) + " " + frag);
						}
					//					System.out.println();
					subAndSuperAtts.put(a, fIdx);
				}

				Set<Integer> hasSuper = new HashSet<Integer>();
				Set<Integer> hasSub = new HashSet<Integer>();
				for (int a = 0; a < data.numAttributes(); a++)
				{
					if (data.classIndex() == a)
						continue;
					CFPFragment f = miner.getFragmentViaIdx(a);
					Set<CFPFragment> fs = miner.getSubFragments(f);
					if (fs != null)
					{
						hasSub.add(a);
						for (CFPFragment cfpFragment : fs)
							hasSuper.add(miner.getIdxForFragment(cfpFragment));
					}
				}

				List<PredictionAttribute> pAtts = PredictionAttributeComputation
						.compute((Classifier) m.getClassifier(), inst, dist, subAndSuperAtts);

				List<SubgraphPredictionAttribute> l = new ArrayList<SubgraphPredictionAttribute>();
				for (PredictionAttribute pa : pAtts)
				{
					int a = pa.getAttribute();
					l.add(new SubgraphPredictionAttribute(pa.getAttribute(),
							pa.getAlternativeDistributionForInstance(), pa.getDiffToOrigProp(),
							hasSuper.contains(a), hasSub.contains(a)));
				}
				predictionAttributes = l;
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public static String getPredictionListHTML(Prediction p[])
	{
		try
		{
			return new PredictionsHtml(p).build();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getHTML()
	{
		try
		{
			return new PredictionHtml(this).build();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	// opentox stuff

	public DataEntry getDataEntry()
	{
		return new DataEntry()
		{
			@Override
			public FeatureValue[] getValues()
			{
				return new FeatureValue[] { new FeatureValue()
						{
							@Override
							public Object getValue()
							{
								return Model.find(modelId).getClassValues()[predictedIdx];
							}

							@Override
							public String getFeature()
							{
								return Model.find(modelId).getPredictedVariables()[0];
							}
						}, new FeatureValue()
						{
							@Override
							public Object getValue()
							{
								return predictedDistribution[predictedIdx];
							}

							@Override
							public String getFeature()
							{
								return Model.find(modelId).getPredictedVariables()[1];
							}
						} };
			}

			@Override
			public String getCompound()
			{
				return Compound.getCompoundURI(smiles);
			}
		};
	};

	//

	public static void main(String[] args)
	{
		//		String modelId = "AMES";
		//		String smiles[] = { "C1=CC(=CC=C1NC(=O)C2=CSC(=C2)[N+](=O)[O-])Cl",
		//				"C1=CC=C(C=C1)S(=O)(=O)N(C2=CC=CC=N2)[N+](=O)[O-]",
		//				"C1=CC=NC(=C1)NS(=O)(=O)C2=CC=C(C=C2)[N+](=O)[O-]" };

		//		String modelId = "NCTRER";
		//		String smiles[] = { "C[Si](O[Si](Cc1ccccc1)(C)C)(Cc1ccccc1)C" };
		//		//		String smiles[] = FileUtil.readStringFromFile("/home/martin/tmp/nctrer_no_phenolic.smi")
		//		//				.split("\n");
		//		//List<String> smiles = new DataLoader("data").getDataset("NCTRER").getSmiles();

		String modelIds[] = { "AMES", "ChEMBL_93", "MUV_733" };
		String smiles[] = { "CC1(C2CCC(O1)(CC2)C)C" };

		for (String modelId : modelIds)
		{
			Model m = Model.find(modelId);
			for (String smi : smiles)
			{
				String predictionId = StringUtil.getMD5(smi);
				if (exists(m.getId(), predictionId))
					PersistanceAdapter.INSTANCE.deletePrediction(m.getId(), predictionId);

				Prediction p = Prediction.createPrediction(m, smi, true);
				System.out.println(
						p.getSmiles() + " " + ArrayUtil.toString(p.getPredictedDistribution()));
			}
		}

	}

}
