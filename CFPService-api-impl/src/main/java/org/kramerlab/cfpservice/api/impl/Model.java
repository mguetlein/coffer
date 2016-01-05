package org.kramerlab.cfpservice.api.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

import org.kramerlab.cfpminer.CFPtoArff;
import org.kramerlab.cfpminer.weka.ValidationResultsProvider;
import org.kramerlab.cfpservice.api.ModelObj;
import org.kramerlab.cfpservice.api.impl.html.ModelHtml;
import org.kramerlab.cfpservice.api.impl.html.ModelsHtml;
import org.kramerlab.cfpservice.api.impl.persistance.PersistanceAdapter;
import org.mg.cdklib.cfp.CFPMiner;
import org.mg.cdklib.cfp.CFPType;
import org.mg.cdklib.cfp.FeatureSelection;
import org.mg.javalib.datamining.ResultSet;
import org.mg.javalib.datamining.ResultSetIO;
import org.mg.javalib.util.CountedSet;
import org.mg.javalib.util.FileUtil;
import org.mg.javalib.util.ListUtil;
import org.mg.wekalib.attribute_ranking.ExtendedNaiveBayes;
import org.mg.wekalib.attribute_ranking.ExtendedRandomForest;

import weka.classifiers.Classifier;
import weka.classifiers.functions.SMO;
import weka.core.Instances;
import weka.core.Randomizable;

@SuppressWarnings("restriction")
@XmlRootElement
public class Model extends ModelObj
{
	private static final long serialVersionUID = 5L;

	protected transient CFPMiner miner;
	protected transient Classifier erf;

	public Model()
	{
	}

	@Override
	public String toString()
	{
		StringBuffer s = new StringBuffer();
		s.append(this.getClass().getSimpleName() + "\n");
		s.append("id: " + id + "\n");
		return s.toString();
	}

	public static Model find(String id)
	{
		return PersistanceAdapter.INSTANCE.readModel(id);
	}

	public String getHTML()
	{
		try
		{
			return new ModelHtml(this).build();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public static String getModelListHTML()
	{
		try
		{
			return new ModelsHtml().build();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public Classifier getClassifier()
	{
		if (erf == null)
			erf = PersistanceAdapter.INSTANCE.readClassifier(id);
		return erf;
	}

	public CFPMiner getCFPMiner()
	{
		if (miner == null)
			miner = PersistanceAdapter.INSTANCE.readCFPMiner(id);
		return miner;
	}

	public void saveModel()
	{
		PersistanceAdapter.INSTANCE.saveModel(this);
	}

	public static Model[] listModels()
	{
		return PersistanceAdapter.INSTANCE.readModels();
	}

	public InputStream getValidationChart()
	{
		try
		{
			ValidationResultsProvider val = new ValidationResultsProvider(
					PersistanceAdapter.INSTANCE.getModelValidationResultsFile(id));
			String valPng = PersistanceAdapter.INSTANCE.getModelValidationImageFile(id);
			val.plot(valPng);
			return new FileInputStream(valPng);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public String getTarget()
	{
		return getTarget(id);
	}

	public String getName()
	{
		return getName(id);
	}

	public static String getName(String modelId)
	{
		return modelId.replaceAll("_", " ");
	}

	public Map<String, String> getDatasetCitations()
	{
		return PersistanceAdapter.INSTANCE.getModelDatasetCitations(id);
	}

	public static String getTarget(String modelId)
	{
		return PersistanceAdapter.INSTANCE.getModelEndpoint(modelId) + "";
	}

	public static Set<String> getDatasetURLs(String modelId)
	{
		return PersistanceAdapter.INSTANCE.getModelDatasetURLs(modelId);
	}

	public static void main(String[] args) throws Exception
	{
		//		buildModel("REID-3", false);
		//		buildModel("REID-4", false);
		//		buildModel("REID-11", false);

		//buildModel("DUD_vegfr2", false);
		//		buildModel("ChEMBL_61", true);
		//buildModel("NCTRER", false);
		//buildModel("CPDBAS_Mutagenicity", false);
		//		//				buildModel("READ", false);
		//		buildModel("CPDBAS_Rat", true);
		//buildModel("CPDBAS_Hamster", false);
		//		buildModel("ChEMBL_61");
		//		buildModel("MUV_859");
		buildModel("CPDBAS_MultiCellCall", false);
		//		buildModel("ChEMBL_87");

		//		for (String dataset : new CFPDataLoader("persistance/data").allDatasets())
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

	public static void predict(Model model, String smiles) throws Exception
	{
		Prediction p = Prediction.createPrediction(model, smiles);
		System.out.println(p.getPredictedDistribution()[model.getActiveClassIdx()]);
	}

	@SuppressWarnings("unchecked")
	public static void buildModel(String dataset, boolean forceExistingValidation) throws Exception
	{
		String algorithm = null;

		if (PersistanceAdapter.INSTANCE.modelExists(dataset))
			PersistanceAdapter.INSTANCE.deleteModel(dataset);

		Model model = new Model();
		model.id = dataset;

		List<String> endpoints = PersistanceAdapter.INSTANCE.readDatasetEndpoints(dataset);
		List<String> smiles = PersistanceAdapter.INSTANCE.readDatasetSmiles(dataset);
		ListUtil.scramble(new Random(1), smiles, endpoints);

		model.miner = new CFPMiner(endpoints);

		File f = new File(ValidationResultsProvider.RESULTS_MERGED_FOLDER + "AUP.best");
		if (f.exists())
		{
			ResultSet r = ResultSetIO.parseFromTxtFile(f);
			for (int i = 0; i < r.getNumResults(); i++)
			{
				if (r.getResultValue(i, "Dataset").toString().equals(dataset))
				{
					algorithm = r.getResultValue(i, "Algorithm").toString();
					CFPType type = CFPType.valueOf(r.getResultValue(i, "CFPType").toString());
					FeatureSelection sel = FeatureSelection.valueOf(r.getResultValue(i,
							"FeatureSelection").toString());
					if (sel != FeatureSelection.none)
					{
						int hashfoldsize = Double.valueOf(
								r.getResultValue(i, "hashfoldSize").toString()).intValue();
						model.miner.setHashfoldsize(hashfoldsize);
					}
					model.miner.setType(type);
					model.miner.setFeatureSelection(sel);
					break;
				}
			}
		}
		if (algorithm == null)
		{
			algorithm = "SMO";
			model.miner.setType(CFPType.ecfp4);
			model.miner.setHashfoldsize(1024);
			model.miner.setFeatureSelection(FeatureSelection.filt);
		}

		model.miner.mine(smiles);

		String outfile = PersistanceAdapter.INSTANCE.getModelValidationResultsFile(dataset);
		if (ValidationResultsProvider.resultsExist(dataset, model.miner, algorithm))
			FileUtil.copy(
					ValidationResultsProvider.getResultsFile(dataset, model.miner, algorithm),
					outfile);
		else
		{
			if (forceExistingValidation)
				throw new IllegalStateException("validation missing");
			//			CFPValidate.validate(dataset, 1, outfile, new String[] { algorithm }, endpoints,
			//					model.miner);
		}

		model.miner.applyFilter();
		System.out.println(model.miner);

		if (model.miner.getNumCompounds() != endpoints.size())
			throw new IllegalStateException();

		//			Instances inst = new Instances(new FileReader("/home/martin/workspace/external/weka-3-7-10/data/vote.arff"));
		///home/martin/data/arffs/breast-cancer.arff
		Instances inst = CFPtoArff.getTrainingDataset(model.miner, dataset);
		inst.setClassIndex(inst.numAttributes() - 1);

		if (inst.size() != smiles.size())
			throw new IllegalStateException();
		if (inst.numAttributes() != model.miner.getNumFragments() + 1)
			throw new IllegalStateException();
		if (!model.miner.getClassValues()[0].equals(inst.classAttribute().value(0)))
			throw new IllegalArgumentException();
		if (!model.miner.getClassValues()[1].equals(inst.classAttribute().value(1)))
			throw new IllegalArgumentException();

		int seed = 1;
		if (algorithm.equals("RnF"))
			model.erf = new ExtendedRandomForest();
		else if (algorithm.equals("NBy"))
			model.erf = new ExtendedNaiveBayes(); //NaiveBayes();
		else if (algorithm.equals("SMO"))
		{
			model.erf = new SMO();
			((SMO) model.erf).setBuildLogisticModels(true);
		}
		else
			throw new IllegalStateException();
		if (model.erf instanceof Randomizable)
			((Randomizable) model.erf).setSeed(seed);
		((Classifier) model.erf).buildClassifier(inst);

		//		model.erf.printTrees();

		//		System.out.println(model.miner.getHashcodeViaIdx(57));
		//		System.out.println(ListUtil.toString(new ArrayList<Integer>(model.miner.getCompoundsForHashcode(model.miner
		//				.getHashcodeViaIdx(57)))));

		//		System.out.println(ListUtil.toString(new ArrayList<Integer>(model.miner.getCompoundsForHashcode(model.miner
		//				.getHashcodeViaIdx(0)))));
		int idx = 1;
		System.out.println("idx " + idx);
		System.out.println("hash-code " + model.miner.getFragmentViaIdx(idx));
		List<String> endpoint = new ArrayList<String>();
		for (Integer c : model.miner.getCompoundsForFragment(model.miner.getFragmentViaIdx(idx)))
			endpoint.add(model.miner.getEndpoints().get(c));
		System.out.println(CountedSet.create(endpoint));

		model.setActiveClassIdx(model.miner.getActiveIdx());
		model.setClassValues(model.miner.getClassValues());
		model.saveModel();

		//		{
		//			for (String smi : smiles)
		//			{
		//				Instances data = CFPtoArff.getTestDataset(model.getCFPMiner(), "DUD_vegfr2",
		//						CDKConverter.parseSmiles(smi));
		//				Instance testInstance = data.get(0);
		//				data.setClassIndex(data.numAttributes() - 1);
		//				System.err.println(ArrayUtil.toString(((Classifier) model.getClassifier())
		//						.distributionForInstance(testInstance)) + " " + smi);
		//			}
		//		}

		//		{
		//			Model m = Model.find(dataset);
		//			System.out.println("hash-code " + m.getCFPMiner().getFragmentViaIdx(idx));
		//			CFPMiner miner = m.getCFPMiner();
		//			for (int i = 0; i < miner.getNumFragments(); i++)
		//			{
		//				String smi = miner.getTrainingDataSmiles()
		//						.get(miner.getCompoundsForFragment(miner.getFragmentViaIdx(i)).iterator()
		//								.next());
		//				if (miner.getAtoms(smi, miner.getFragmentViaIdx(i)) == null)
		//					throw new IllegalStateException("no matching atoms in training intance");
		//				//			else
		//				//				System.out.println("attidx: " + i + ", hashcode " + miner.getHashcodeViaIdx(i) + ", smi " + smi
		//				//						+ ", atoms " + ArrayUtil.toString(miner.getAtoms(smi, miner.getHashcodeViaIdx(i))));
		//			}
		//		}
	}

}
