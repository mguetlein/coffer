package org.kramerlab.cfpservice.api.impl.util;

import java.util.List;
import java.util.Random;
import java.util.Set;

import org.kramerlab.cfpminer.CFPtoArff;
import org.mg.cdklib.CDKConverter;
import org.mg.cdklib.cfp.CFPMiner;
import org.mg.cdklib.cfp.CFPType;
import org.mg.cdklib.cfp.FeatureSelection;
import org.mg.cdklib.data.CDKDataset;
import org.mg.cdklib.data.DataLoader;
import org.mg.javalib.util.ArrayUtil;
import org.mg.wekalib.attribute_ranking.AttributeProvidingClassifier;
import org.mg.wekalib.attribute_ranking.ExtendedRandomForest;
import org.mg.wekalib.attribute_ranking.PredictionAttribute;
import org.mg.wekalib.attribute_ranking.PredictionAttributeComputation;
import org.openscience.cdk.interfaces.IAtomContainer;

import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.Randomizable;

public class ExtendedRandomForestUtil
{
	public static void demo()
	{
		try
		{
			String name = "CPDBAS_Hamster";
			CDKDataset d = new DataLoader("/home/martin/workspace/CFPMiner/data/").getDataset(name);

			CFPMiner cfp = new CFPMiner(d.getEndpoints());
			cfp.setType(CFPType.ecfp6);
			cfp.setFeatureSelection(FeatureSelection.filt);
			cfp.setHashfoldsize(1024);
			cfp.mine(d.getSmiles());
			cfp.applyFilter();
			System.out.println(cfp);
			if (cfp.getNumCompounds() != d.getSmiles().size())
				throw new IllegalStateException();

			Instances inst = CFPtoArff.getTrainingDataset(cfp, name);
			if (inst.size() != d.getSmiles().size())
				throw new IllegalStateException();
			if (inst.numAttributes() != cfp.getNumFragments() + 1)
				throw new IllegalStateException();
			inst.setClassIndex(inst.numAttributes() - 1);
			int seed = 1;
			inst.randomize(new Random(seed));
			Classifier clazzy = new ExtendedRandomForest();
			if (clazzy instanceof Randomizable)
				((Randomizable) clazzy).setSeed(seed);
			clazzy.buildClassifier(inst);

			//			Evaluation eval = new Evaluation(inst);
			//			eval.crossValidateModel(new RandomForest(), inst, 10, new Random(1));
			//			System.out.println(eval.toSummaryString());
			//			System.out.println(eval.areaUnderROC(0));
			//			System.exit(1);

			for (int i = 0; i < 3; i++)
			{
				System.out.println("-------------------------\n");
				int test = new Random().nextInt(d.getSmiles().size() - 1);
				System.out.println("Test instance " + test);
				double dist[] = clazzy.distributionForInstance(inst.get(test));
				System.out.println("Prediction " + ArrayUtil.toString(dist));

				Set<Integer> atts = ((AttributeProvidingClassifier) clazzy)
						.getAttributesEmployedForPrediction(inst.get(test));
				List<PredictionAttribute> pAtt = PredictionAttributeComputation.compute(clazzy,
						inst.get(test), dist, atts, null);
				System.out.println("Num prediction attributes: " + pAtt.size() + "\n");

				int idx = 0;
				for (PredictionAttribute a : pAtt)
				{
					System.out.println(a + "\n");
					idx++;
					if (idx > 3)
						break;
				}

				IAtomContainer mol = CDKConverter.parseSmiles(d.getSmiles().get(test));
				String outfile = "/tmp/test" + i + ".png";
				CFPDepictUtil.depictMultiMatchToPNG(outfile, mol, cfp, dist, pAtt, -1);
				System.err.println(outfile);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void main(String[] args)
	{
		demo();
	}
}
