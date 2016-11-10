package org.kramerlab.coffer.api.impl;

import java.io.File;
import java.util.List;
import java.util.Random;

import org.kramerlab.cfpminer.appdomain.KNNTanimotoCFPAppDomainModel;
import org.kramerlab.cfpminer.experiments.validation.InnerValidationResults;
import org.kramerlab.cfpminer.weka.eval2.CFPFeatureProvider;
import org.kramerlab.cfpminer.weka.eval2.CFPtoArff;
import org.kramerlab.coffer.api.impl.ot.ModelImpl;
import org.kramerlab.coffer.api.impl.persistance.PersistanceAdapter;
import org.mg.cdklib.cfp.CFPMiner;
import org.mg.cdklib.cfp.CFPType;
import org.mg.cdklib.cfp.FeatureSelection;
import org.mg.cdklib.data.DataLoader;
import org.mg.javalib.datamining.ResultSetIO;
import org.mg.javalib.util.ArrayUtil;
import org.mg.javalib.util.ListUtil;
import org.mg.wekalib.eval2.model.FeatureModel;

import weka.classifiers.Classifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;

public class BuildModels
{

	public static void main(String[] args) throws Exception
	{
		buildModelFromNestedCV(false);

		//		for (String dataset : DataLoader.INSTANCE.allDatasetsSorted())
		//			if (PersistanceAdapter.INSTANCE.modelExists(dataset))
		//				PersistanceAdapter.INSTANCE.deleteModel(dataset);

		//		buildModelFromNestedCV(true, "CPDBAS_Mouse");
		//		buildModelFromNestedCV(true, "NCTRER");

		//		for (String dataset : DataLoader.INSTANCE.allDatasetsSorted())
		//			//			//if (new Random().nextDouble() < 0.1)
		//			buildModelFromNestedCV(false, dataset);

		//buildModelWithoutValidation("LTKB");

		//buildModelFromNestedCV("NCTRER");
		//		buildModel("REID-3", false);
		//		buildModel("REID-4", false);
		//		buildModel("REID-11", false);

		//buildModel("DUD_vegfr2", false);
		//		buildModel("ChEMBL_61", true);
		//buildModel("NCTRER", false);
		//		buildModelFromNestedCV("CPDBAS_Mutagenicity");
		//		//				buildModel("READ", false);
		//		buildModel("CPDBAS_Rat", true);
		//		buildModelFromNestedCV("CPDBAS_Hamster");//, false);
		//		buildModel("ChEMBL_61");
		//		buildModelFromNestedCV("CPDBAS_Dog_Primates");
		//		buildModelFromNestedCV("DUD_vegfr2");
		//		buildModelFromNestedCV("MUV_859");

		//deleteAllModels();

		//buildModelFromNestedCV("AMES");
		//		buildModelFromNestedCV("CPDBAS_MultiCellCall");
		//		buildModelFromNestedCV("ChEMBL_61");
		//buildModelFromNestedCV("AMES");
		//		buildModel("ChEMBL_87");

		//		for (String dataset : new DataLoader("data").allDatasetsSorted())
		//			buildModelFromNestedCV(dataset);
		//		{
		//			//			//			if (!PersistanceAdapter.INSTANCE.modelExists(dataset))
		//			//			//            if (dataset.equals("AMES"))
		//			//			if (dataset.startsWith("ChEMBL") || dataset.startsWith("MUV"))
		//			buildModel(dataset, true);
		//			//			break;
		//		}

		//		buildModel("AMES", true);

		//		Model.find("CPDBAS_Mutagenicity").getValidationChart();

		//		Model m = Model.find("REID");
		//		CSVFile f = FileUtil.readCSV("/home/martin/data/reid/combined-2.csv", ",");
		//		for (String line[] : f.content)
		//		{
		//			if (line[0] == null)
		//			{
		//				System.out.println("");
		//				continue;
		//			}
		//			if (line[0].equals("SMILES"))
		//				continue;
		//			predict(m, line[0]);
		//		}

	}

	public static void buildModelFromNestedCV(boolean replaceExisting, String... datasets)
			throws Exception
	{
		InnerValidationResults val = new InnerValidationResults();

		for (String dataset : DataLoader.INSTANCE.allDatasetsSorted())
		{
			if (datasets != null && datasets.length > 0
					&& ArrayUtil.indexOf(datasets, dataset) == -1)
				continue;

			if (PersistanceAdapter.INSTANCE.modelExists(dataset) && replaceExisting)
				PersistanceAdapter.INSTANCE.deleteModel(dataset);

			if (PersistanceAdapter.INSTANCE.modelExists(dataset))
				System.out.println("model already exists: " + dataset);
			else
			{
				FeatureModel featureModel = val.getSelectedModel(dataset);
				CFPFeatureProvider featureSetting = (CFPFeatureProvider) featureModel
						.getFeatureProvider();
				org.mg.wekalib.eval2.model.Model algorithmSetting = featureModel.getModel();
				System.out.println("\nBuilding model with features: " + featureSetting.getName());
				System.out.println("and algorithm: " + algorithmSetting.getName());
				buildModel(dataset, featureSetting.getHashfoldSize(), featureSetting.getType(),
						featureSetting.getFeatureSelection(),
						((org.mg.wekalib.eval2.model.AbstractModel) algorithmSetting)
								.getWekaClassifer());
			}

			System.out.println("\nStoring validation results");
			String outfile = PersistanceAdapter.INSTANCE.getModelValidationResultsFile(dataset);
			ResultSetIO.writeToFile(new File(outfile), val.getValidationResults(dataset));

			//		CFPNestedCV.plotValidationResult(dataset, null);
		}
	}

	public static void buildModelWithoutValidation(String dataset) throws Exception
	{
		if (PersistanceAdapter.INSTANCE.modelExists(dataset))
			PersistanceAdapter.INSTANCE.deleteModel(dataset);

		buildModel(dataset, 2048, CFPType.ecfp4, FeatureSelection.filt, new RandomForest());
	}

	public static void buildModel(String dataset, int hashfoldSize, CFPType type,
			FeatureSelection feats, Classifier classifier) throws Exception
	{
		ModelImpl model = new ModelImpl();
		model.setId(dataset);

		List<String> endpoints = PersistanceAdapter.INSTANCE.readDatasetEndpoints(dataset);
		List<String> smiles = PersistanceAdapter.INSTANCE.readDatasetSmiles(dataset);
		ListUtil.scramble(new Random(1), smiles, endpoints);
		model.setCFPMiner(new CFPMiner(endpoints));

		System.out.println("\nMining features " + hashfoldSize + " " + type + " " + feats);
		model.getCFPMiner().setHashfoldsize(hashfoldSize);
		model.getCFPMiner().setType(CFPType.ecfp4);
		model.getCFPMiner().setFeatureSelection(FeatureSelection.filt);
		model.getCFPMiner().mine(smiles);
		model.getCFPMiner().applyFilter();
		System.out.println(model.getCFPMiner());
		if (model.getCFPMiner().getNumCompounds() != endpoints.size())
			throw new IllegalStateException();

		Instances inst = CFPtoArff.getTrainingDataset(model.getCFPMiner(), dataset);
		inst.setClassIndex(inst.numAttributes() - 1);
		if (inst.size() != smiles.size())
			throw new IllegalStateException();
		if (inst.numAttributes() != model.getCFPMiner().getNumFragments() + 1)
			throw new IllegalStateException();
		if (!model.getCFPMiner().getClassValues()[0].equals(inst.classAttribute().value(0)))
			throw new IllegalArgumentException();
		if (!model.getCFPMiner().getClassValues()[1].equals(inst.classAttribute().value(1)))
			throw new IllegalArgumentException();

		System.out.println("Building algorithm " + classifier.getClass().getSimpleName());
		model.setClassifier(classifier);
		model.getClassifier().buildClassifier(inst);

		model.setActiveClassIdx(model.getCFPMiner().getActiveIdx());
		model.setClassValues(model.getCFPMiner().getClassValues());

		KNNTanimotoCFPAppDomainModel appDomain = new KNNTanimotoCFPAppDomainModel(3, true);
		appDomain.setCFPMiner(model.getCFPMiner());
		appDomain.build();
		model.setAppDomain(appDomain);

		model.saveModel();
	}

	//	public static void trainModel(String dataset) throws Exception
	//	{
	//		List<String> endpoints = PersistanceAdapter.INSTANCE.readDatasetEndpoints(dataset);
	//		List<String> smiles = PersistanceAdapter.INSTANCE.readDatasetSmiles(dataset);
	//		ListUtil.scramble(new Random(1), smiles, endpoints);
	//
	//		CFPMiner miner = new CFPMiner(endpoints);
	//		miner.setType(CFPType.ecfp4);
	//		//		miner.setHashfoldsize(1024);
	//		miner.setFeatureSelection(FeatureSelection.none);
	//		miner.mine(smiles);
	//		//		miner.applyFilter();
	//		System.out.println(miner);
	//
	//		Instances inst = CFPtoArff.getTrainingDataset(miner, dataset);
	//		inst.setClassIndex(inst.numAttributes() - 1);
	//
	//		Classifier classifier = new SMO();
	//		//		((SMO) classifier).setBuildLogisticModels(true);
	//		((SMO) classifier).setC(100.0);
	//		classifier.buildClassifier(inst);
	//	}
	//
	//	public static void buildModel(String dataset) throws Exception
	//	{
	//		String algorithm = null;
	//
	//		if (PersistanceAdapter.INSTANCE.modelExists(dataset))
	//			PersistanceAdapter.INSTANCE.deleteModel(dataset);
	//
	//		AbstractModel model = new ModelImpl();
	//		model.id = dataset;
	//
	//		List<String> endpoints = PersistanceAdapter.INSTANCE.readDatasetEndpoints(dataset);
	//		List<String> smiles = PersistanceAdapter.INSTANCE.readDatasetSmiles(dataset);
	//		ListUtil.scramble(new Random(1), smiles, endpoints);
	//
	//		model.miner = new CFPMiner(endpoints);
	//
	//		algorithm = "SMO";
	//		model.miner.setType(CFPType.ecfp4);
	//		model.miner.setHashfoldsize(1024);
	//		model.miner.setFeatureSelection(FeatureSelection.filt);
	//		model.miner.mine(smiles);
	//		model.miner.applyFilter();
	//		System.out.println(model.miner);
	//
	//		if (model.miner.getNumCompounds() != endpoints.size())
	//			throw new IllegalStateException();
	//
	//		//			Instances inst = new Instances(new FileReader("/home/martin/workspace/external/weka-3-7-10/data/vote.arff"));
	//		///home/martin/data/arffs/breast-cancer.arff
	//		Instances inst = CFPtoArff.getTrainingDataset(model.miner, dataset);
	//		inst.setClassIndex(inst.numAttributes() - 1);
	//
	//		if (inst.size() != smiles.size())
	//			throw new IllegalStateException();
	//		if (inst.numAttributes() != model.miner.getNumFragments() + 1)
	//			throw new IllegalStateException();
	//		if (!model.miner.getClassValues()[0].equals(inst.classAttribute().value(0)))
	//			throw new IllegalArgumentException();
	//		if (!model.miner.getClassValues()[1].equals(inst.classAttribute().value(1)))
	//			throw new IllegalArgumentException();
	//
	//		int seed = 1;
	//		if (algorithm.equals("RnF"))
	//			model.classifier = new RandomForest();
	//		else if (algorithm.equals("NBy"))
	//			model.classifier = new NaiveBayes();
	//		else if (algorithm.equals("SMO"))
	//		{
	//			model.classifier = new SMO();
	//			((SMO) model.classifier).setBuildLogisticModels(true);
	//		}
	//		else
	//			throw new IllegalStateException();
	//		if (model.classifier instanceof Randomizable)
	//			((Randomizable) model.classifier).setSeed(seed);
	//		((Classifier) model.classifier).buildClassifier(inst);
	//
	//		//		model.erf.printTrees();
	//
	//		//		System.out.println(model.miner.getHashcodeViaIdx(57));
	//		//		System.out.println(ListUtil.toString(new ArrayList<Integer>(model.miner.getCompoundsForHashcode(model.miner
	//		//				.getHashcodeViaIdx(57)))));
	//
	//		//		System.out.println(ListUtil.toString(new ArrayList<Integer>(model.miner.getCompoundsForHashcode(model.miner
	//		//				.getHashcodeViaIdx(0)))));
	//		int idx = 1;
	//		System.out.println("idx " + idx);
	//		System.out.println("hash-code " + model.miner.getFragmentViaIdx(idx));
	//		List<String> endpoint = new ArrayList<String>();
	//		for (Integer c : model.miner.getCompoundsForFragment(model.miner.getFragmentViaIdx(idx)))
	//			endpoint.add(model.miner.getEndpoints().get(c));
	//		System.out.println(CountedSet.create(endpoint));
	//
	//		model.activeClassIdx = model.miner.getActiveIdx();
	//		model.classValues = model.miner.getClassValues();
	//		model.saveModel();
	//
	//		//		{
	//		//			for (String smi : smiles)
	//		//			{
	//		//				Instances data = CFPtoArff.getTestDataset(model.getCFPMiner(), "DUD_vegfr2",
	//		//						CDKConverter.parseSmiles(smi));
	//		//				Instance testInstance = data.get(0);
	//		//				data.setClassIndex(data.numAttributes() - 1);
	//		//				System.err.println(ArrayUtil.toString(((Classifier) model.getClassifier())
	//		//						.distributionForInstance(testInstance)) + " " + smi);
	//		//			}
	//		//		}
	//
	//		//		{
	//		//			Model m = Model.find(dataset);
	//		//			System.out.println("hash-code " + m.getCFPMiner().getFragmentViaIdx(idx));
	//		//			CFPMiner miner = m.getCFPMiner();
	//		//			for (int i = 0; i < miner.getNumFragments(); i++)
	//		//			{
	//		//				String smi = miner.getTrainingDataSmiles()
	//		//						.get(miner.getCompoundsForFragment(miner.getFragmentViaIdx(i)).iterator()
	//		//								.next());
	//		//				if (miner.getAtoms(smi, miner.getFragmentViaIdx(i)) == null)
	//		//					throw new IllegalStateException("no matching atoms in training intance");
	//		//				//			else
	//		//				//				System.out.println("attidx: " + i + ", hashcode " + miner.getHashcodeViaIdx(i) + ", smi " + smi
	//		//				//						+ ", atoms " + ArrayUtil.toString(miner.getAtoms(smi, miner.getHashcodeViaIdx(i))));
	//		//			}
	//		//		}
	//	}

}
