package org.kramerlab.cfpservice.api.impl.util;

import java.util.List;
import java.util.Random;

import org.kramerlab.cfpminer.CFPDataLoader;
import org.kramerlab.cfpminer.CFPDataLoader.Dataset;
import org.kramerlab.cfpminer.CFPMiner;
import org.kramerlab.cfpminer.CFPtoArff;
import org.kramerlab.cfpminer.cdk.CDKUtil;
import org.kramerlab.extendedrandomforests.weka.ExtendedRandomForest;
import org.kramerlab.extendedrandomforests.weka.PredictionAttribute;
import org.mg.javalib.util.ArrayUtil;
import org.openscience.cdk.interfaces.IAtomContainer;

import weka.core.Instances;

public class ExtendedRandomForestUtil
{
	public static void demo()
	{
		try
		{
			String name = "CPDBAS_Hamster";
			Dataset d = new CFPDataLoader("/home/martin/workspace/CFPMiner/data/").getDataset(name);

			CFPMiner cfp = new CFPMiner(d.getEndpoints());
			cfp.setType(CFPMiner.CFPType.ecfp6);
			cfp.setFeatureSelection(CFPMiner.FeatureSelection.filt);
			cfp.setHashfoldsize(1024);
			cfp.mine(d.getSmiles());
			cfp.applyFilter();
			System.out.println(cfp);
			if (cfp.getNumCompounds() != d.getSmiles().size())
				throw new IllegalStateException();

			Instances inst = CFPtoArff.getTrainingDataset(cfp, name);
			if (inst.size() != d.getSmiles().size())
				throw new IllegalStateException();
			if (inst.numAttributes() != cfp.getNumAttributes() + 1)
				throw new IllegalStateException();
			inst.setClassIndex(inst.numAttributes() - 1);
			int seed = 1;
			inst.randomize(new Random(seed));
			ExtendedRandomForest erf = new ExtendedRandomForest();
			erf.setSeed(seed);
			erf.buildClassifier(inst);

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
				double dist[] = erf.distributionForInstance(inst.get(test));
				System.out.println("Prediction " + ArrayUtil.toString(dist));

				List<PredictionAttribute> pAtt = erf.getPredictionAttributes(inst.get(test), dist);
				System.out.println("Num prediction attributes: " + pAtt.size() + "\n");

				int idx = 0;
				for (PredictionAttribute a : pAtt)
				{
					System.out.println(a + "\n");
					idx++;
					if (idx > 3)
						break;
				}

				IAtomContainer mol = CDKUtil.parseSmiles(d.getSmiles().get(test));
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
