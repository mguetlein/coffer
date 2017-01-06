package org.kramerlab.coffer.api.impl.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.kramerlab.cfpminer.appdomain.ADInfoModel;
import org.kramerlab.cfpminer.appdomain.ADPrediction;
import org.kramerlab.cfpminer.weka.eval2.CFPtoArff;
import org.kramerlab.coffer.api.ModelService;
import org.kramerlab.coffer.api.impl.html.PredictionHtml;
import org.kramerlab.coffer.api.impl.html.PredictionHtml.HideFragments;
import org.kramerlab.coffer.api.impl.html.PredictionsHtml;
import org.kramerlab.coffer.api.impl.ot.PredictionImpl;
import org.kramerlab.coffer.api.impl.persistance.PersistanceAdapter;
import org.kramerlab.coffer.api.impl.provider.HTMLOwner;
import org.kramerlab.coffer.api.objects.Model;
import org.kramerlab.coffer.api.objects.Prediction;
import org.kramerlab.coffer.api.objects.SubgraphPredictionAttribute;
import org.mg.cdklib.CDKConverter;
import org.mg.cdklib.cfp.CFPFragment;
import org.mg.cdklib.cfp.CFPMiner;
import org.mg.cdklib.depict.CDKDepict;
import org.mg.javalib.util.ArrayUtil;
import org.mg.javalib.util.ListUtil;
import org.mg.javalib.util.StringUtil;
import org.mg.wekalib.attribute_ranking.PredictionAttributeComputation;
import org.mg.wekalib.attribute_ranking.PredictionAttributeImpl;
import org.openscience.cdk.interfaces.IAtomContainer;

import weka.core.Instance;
import weka.core.Instances;

public abstract class AbstractPrediction extends AbstractServiceObject
		implements Prediction, HTMLOwner, Serializable
{
	private static final long serialVersionUID = 12L;

	protected String id;
	protected String smiles;
	protected String modelId;
	protected int predictedIdx;
	protected double predictedDistribution[];
	protected String trainingActivity;
	protected List<SubgraphPredictionAttribute> predictionAttributes;
	protected ADPrediction insideAppDomain;

	protected transient HideFragments hideFragments;
	protected transient int maxNumFragments;

	@Override
	public String getLocalURI()
	{
		return modelId + "/prediction/" + id;
	}

	@Override
	public String getId()
	{
		return id;
	}

	@Override
	public String getModelId()
	{
		return modelId;
	}

	@Override
	public String getSmiles()
	{
		return smiles;
	}

	@Override
	public double[] getPredictedDistribution()
	{
		return predictedDistribution;
	}

	@Override
	public int getPredictedIdx()
	{
		return predictedIdx;
	}

	@Override
	public String getTrainingActivity()
	{
		return trainingActivity;
	}

	@Override
	public ADPrediction getADPrediction()
	{
		return insideAppDomain;
	}

	//	public void setPredictionAttributes(List<SubgraphPredictionAttribute> predictionAttributes)
	//	{
	//		this.predictionAttributes = predictionAttributes;
	//	}

	public void computePredictionAttributesComputed()
	{
		if (predictionAttributes == null)
		{
			initPrediction(true);
			PersistanceAdapter.INSTANCE.savePrediction(this);
		}
	}

	public List<SubgraphPredictionAttribute> getPredictionAttributes()
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
		for (Model m : AbstractModel.listModels())
			if (AbstractPrediction.exists(m.getId(), predictionId))
				p.add(AbstractPrediction.find(m.getId(), predictionId));
		return ArrayUtil.toArray(p);
	}

	public static Prediction find(String modelId, String predictionId)
	{
		return find(modelId, predictionId, HideFragments.NONE, ModelService.DEFAULT_NUM_ENTRIES);
	}

	public static Prediction find(String modelId, String predictionId, HideFragments hideFragments,
			int maxNumFragments)
	{
		AbstractPrediction p = (AbstractPrediction) PersistanceAdapter.INSTANCE
				.readPrediction(modelId, predictionId);
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
				return AbstractPrediction.find(m.getId(), predictionId);
			}

			AbstractPrediction p = new PredictionImpl();
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
			Model m = AbstractModel.find(modelId);
			CFPMiner miner = ((AbstractModel) m).getCFPMiner();

			Instances data = CFPtoArff.getTestDataset(miner, "cfp test data", getMolecule());
			Instance inst = data.get(0);
			data.setClassIndex(data.numAttributes() - 1);

			double dist[] = ((AbstractModel) m).getClassifier().distributionForInstance(inst);

			int maxIdx = ArrayUtil.getMaxIndex(dist);
			predictedIdx = maxIdx;
			predictedDistribution = dist;
			trainingActivity = miner.getTrainingActivity(smiles);

			ADInfoModel ad = ((AbstractModel) m).getAppDomain();
			ad.setCFPMiner(miner);
			insideAppDomain = ad.isInsideAppdomain(smiles);

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

				List<PredictionAttributeImpl> pAtts = PredictionAttributeComputation
						.compute(((AbstractModel) m).getClassifier(), inst, dist, subAndSuperAtts);

				List<SubgraphPredictionAttribute> l = new ArrayList<SubgraphPredictionAttribute>();
				for (PredictionAttributeImpl pa : pAtts)
				{
					int a = pa.getAttribute();
					l.add(new SubgraphPredictionAttributeImpl(pa.getAttribute(),
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

	public static String getPredictionListHTML(AbstractPrediction p[])
	{
		return new PredictionsHtml(p).build();
	}

	@Override
	public String getHTML()
	{
		return new PredictionHtml(this, hideFragments, maxNumFragments).build();
	}

	public static void main(String[] args) throws Exception
	{
		//		{
		//			String smiles = "CCCC([C@@H]1[C@H]([C@@H]([C@H]([C@@H](O1)O/C(=C(\\CC(=O)O)/C=C\\C(=O)O)/C#N)O)O)O)O";
		//			IAtomContainer mol = new SmilesParser(SilentChemObjectBuilder.getInstance())
		//					.parseSmiles(smiles);
		//			//new DepictionGenerator().depict(mol);
		//			new CircularFingerprinter().getBitFingerprint(mol);
		//			System.exit(1);
		//		}

		//		String modelId = "AMES";
		//		String smiles[] = { "C1=CC(=CC=C1NC(=O)C2=CSC(=C2)[N+](=O)[O-])Cl",
		//				"C1=CC=C(C=C1)S(=O)(=O)N(C2=CC=CC=N2)[N+](=O)[O-]",
		//				"C1=CC=NC(=C1)NS(=O)(=O)C2=CC=C(C=C2)[N+](=O)[O-]" };

		//		String modelId = "NCTRER";
		//		String smiles[] = { "C[Si](O[Si](Cc1ccccc1)(C)C)(Cc1ccccc1)C" };
		//		//		String smiles[] = FileUtil.readStringFromFile("/home/martin/tmp/nctrer_no_phenolic.smi")
		//		//				.split("\n");
		//		//List<String> smiles = new DataLoader("data").getDataset("NCTRER").getSmiles();

		String modelIds[] = { //"AMES", "CPDBAS_Mouse", "NCTRER", "ChEMBL_93", "MUV_733",
				//"ChEMBL_100", "MUV_644", "DUD_vegfr2", 
				"ChEMBL_17045" };
		//String smiles[] = { "CC1(C2CCC(O1)(CC2)C)C" };
		List<String> smiles = ListUtil.createList(//"c1ccccc1", "c1cccnc1", "c1cccnc1", "CCC",
				//"CCCC=O", "CCCCCCC", "Br", "Cl", "CCCC", 
				//"C1=CC(=CC=C1C2=C(C(=O)C3=C(C=C(C=C3O2)O)O)O[C@H]4[C@@H]([C@H]([C@@H]([C@H](O4)CO)O)O)O)O",
				//"C[C@H]1CC[C@@]2([C@H]([C@H]3[C@@H](O2)C[C@@H]4[C@@]3(CC[C@H]5[C@H]4CC[C@H]6[C@@]5(CC[C@@H](C6)O[C@H]7[C@@H]([C@H]([C@@H]([C@H](O7)CO)O[C@H]8[C@@H]([C@@H]([C@H]([C@@H](O8)C)O)O)O)O)O[C@H]9[C@@H]([C@H]([C@@H]([C@H](O9)CO)O)O)O)C)C)C)OC1",
				"C([C@@H]1[C@H]([C@@H]([C@H]([C@@H](O1)O/C(=C(\\CC(=O)O)/C=C\\C(=O)O)/C#N)O)O)O)O",
				"CC([C@@H]1[C@H]([C@@H]([C@H]([C@@H](O1)O/C(=C(\\CC(=O)O)/C=C\\C(=O)O)/C#N)O)O)O)O",
				"CCC([C@@H]1[C@H]([C@@H]([C@H]([C@@H](O1)O/C(=C(\\CC(=O)O)/C=C\\C(=O)O)/C#N)O)O)O)O",
				"CCCC([C@@H]1[C@H]([C@@H]([C@H]([C@@H](O1)O/C(=C(\\CC(=O)O)/C=C\\C(=O)O)/C#N)O)O)O)O");

		for (String modelId : modelIds)
		{
			Model m = AbstractModel.find(modelId);

			//SerializationTest.test((AbstractClassifier) ((AbstractModel) m).getClassifier());
			//SerializationTest.test(((AbstractModel) m).getCFPMiner());
			//			SerializationTest
			//					.test((KNNTanimotoCFPAppDomainModel) ((AbstractModel) m).getAppDomain());

			//				CFPMiner miner = ((AbstractModel) m).getCFPMiner();
			//				StopWatchUtil.start("read-ad");
			//				ADInfoModel ad = ((AbstractModel) m).getAppDomain();
			//				ad.setCFPMiner(miner);
			//				StopWatchUtil.stop("read-ad");

			for (String smi : smiles)
			{
				//					StopWatchUtil.start("predict-ad");
				//					ad.isInsideAppdomain(smi);
				//					StopWatchUtil.stop("predict-ad");

				for (int i = 0; i < CDKConverter.parseSmiles(smi).getAtomCount(); i++)
				{
					System.out.println(CDKConverter.parseSmiles(smi).getAtom(i).getPoint2d());
				}
				CDKDepict.depict(CDKConverter.parseSmiles(smi), 10);
				for (int i = 0; i < CDKConverter.parseSmiles(smi).getAtomCount(); i++)
				{
					System.out.println(CDKConverter.parseSmiles(smi).getAtom(i).getPoint2d());
				}

				String predictionId = StringUtil.getMD5(smi);
				if (exists(m.getId(), predictionId))
					PersistanceAdapter.INSTANCE.deletePrediction(m.getId(), predictionId);
				{
					Prediction p = AbstractPrediction.createPrediction(m, smi, false);
					System.out.println(
							p.getSmiles() + " " + ArrayUtil.toString(p.getPredictedDistribution()));
				}
				{
					Prediction p = AbstractPrediction.find(m.getId(), predictionId);
					p.computePredictionAttributesComputed();
					System.out.println(
							p.getSmiles() + " " + ArrayUtil.toString(p.getPredictedDistribution()));
				}
			}

			//				System.out.println();
			//				StopWatchUtil.print();
		}
	}

}
