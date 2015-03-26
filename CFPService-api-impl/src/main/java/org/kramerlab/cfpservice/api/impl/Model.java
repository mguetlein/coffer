package org.kramerlab.cfpservice.api.impl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.xml.bind.annotation.XmlRootElement;

import org.kramerlab.cfpminer.CFPMiner;
import org.kramerlab.cfpminer.CFPtoArff;
import org.kramerlab.cfpminer.weka.ValidationResultsProvider;
import org.kramerlab.cfpservice.api.ModelObj;
import org.kramerlab.cfpservice.api.impl.persistance.PersistanceAdapter;
import org.kramerlab.extendedrandomforests.html.PredictionReport;
import org.kramerlab.extendedrandomforests.weka.ExtendedRandomForest;
import org.mg.htmlreporting.HTMLReport;
import org.mg.javalib.datamining.ResultSet;
import org.mg.javalib.util.CountedSet;

import weka.core.Instances;

@SuppressWarnings("restriction")
@XmlRootElement
public class Model extends ModelObj
{
	private static final long serialVersionUID = 1L;

	protected transient CFPMiner miner;
	protected transient ExtendedRandomForest erf;
	protected transient List<String> trainingDataSmiles;

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
			HTMLReport report = new HTMLReport("Prediction model");
			CFPServiceConfig.initModelReport(report, id);
			report.startInlinesTables();

			ResultSet set = new ResultSet();
			int idx = set.addResult();
			set.setResultValue(idx, "Dataset", id);
			set.setResultValue(idx, "Num compounds", getCFPMiner().getNumCompounds());
			set.setResultValue(idx, "Endpoint values", CountedSet.create(getCFPMiner().getEndpoints()));
			set.concatCols(getExtendedRandomForest().getSummary(true));
			set.concatCols(getCFPMiner().getSummary(true));
			report.addList(set);

			report.addImage(report.getImage("/" + id + "/validation"));

			report.stopInlineTables();

			report.newSubsection("Make prediction");
			report.addForm("/" + id, "compound", "Predict", "Please insert SMILES string");

			//			ValidationResultsProvider val = new ValidationResultsProvider(
			//					PersistanceAdapter.INSTANCE.getModelValidationResultsFile(id));
			//			report.addTable(val.getJoinedResults());

			String predIds[] = Prediction.findLastPredictions(id);
			if (predIds.length > 0)
			{
				ResultSet res = new ResultSet();
				for (int i = 0; i < Math.min(predIds.length, 5); i++)
				{
					Prediction p = Prediction.find(id, predIds[i]);
					int rIdx = res.addResult();
					res.setResultValue(rIdx, "Recent predictions",
							HTMLReport.encodeLink("/" + id + "/prediction/" + predIds[i], p.getSmiles()));
					res.setResultValue(rIdx, "Prediction", HTMLReport.getHTMLCode(PredictionReport.getPredictionString(
							p.getPredictedDistribution(), getClassValues(), p.getPredictedIdx(), true)));
				}
				if (res.getNumResults() > 0)
				{
					report.addGap();
					report.addTable(res);
				}
			}

			return report.close();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public ExtendedRandomForest getExtendedRandomForest()
	{
		if (erf == null)
			erf = PersistanceAdapter.INSTANCE.readExtendedRandomForest(id);
		return erf;
	}

	public CFPMiner getCFPMiner()
	{
		if (miner == null)
			miner = PersistanceAdapter.INSTANCE.readCFPMiner(id);
		return miner;
	}

	public List<String> getTrainingDataSmiles()
	{
		if (trainingDataSmiles == null)
			trainingDataSmiles = PersistanceAdapter.INSTANCE.readTrainingDataSmiles(id);
		return trainingDataSmiles;
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

	public static void main(String[] args) throws Exception
	{
		//		for (Model m : PersistanceAdapter.INSTANCE.readModels())
		//			System.out.println(m.toString());

		//buildModel("ChEMBL_61");
		//buildModel("CPDBAS_Mutagenicity");
		buildModel("NCTRER");

		//		Model.find("CPDBAS_Mutagenicity").getValidationChart();

		//CPDBAS_Hamster
		//buildModel("ChEMBL_61");
	}

	public static void buildModel(String id) throws Exception
	{
		Model model = new Model();
		model.id = id;

		List<String> endpoints = PersistanceAdapter.INSTANCE.readTrainingDataEndpoints(id);
		model.miner = new CFPMiner(endpoints);
		model.miner.setType(CFPMiner.CFPType.ecfp6);
		model.miner.setFeatureSelection(CFPMiner.FeatureSelection.filt);
		model.miner.setHashfoldsize(1024);

		//		model.miner.setpValueThreshold(0.5);
		//		model.miner.setRelMinFreq(0.001);
		List<String> smiles = model.getTrainingDataSmiles();
		model.miner.mine(smiles);

		String outfile = PersistanceAdapter.INSTANCE.getModelValidationResultsFile(id);
		CFPMiner.validate(id, 1, outfile, new String[] { "RaF" }, endpoints, model.miner);

		model.miner.applyFilter();
		System.out.println(model.miner);

		if (model.miner.getNumCompounds() != endpoints.size())
			throw new IllegalStateException();

		//			Instances inst = new Instances(new FileReader("/home/martin/workspace/external/weka-3-7-10/data/vote.arff"));
		///home/martin/data/arffs/breast-cancer.arff
		Instances inst = CFPtoArff.getTrainingDataset(model.miner, id);
		inst.setClassIndex(inst.numAttributes() - 1);

		if (inst.size() != smiles.size())
			throw new IllegalStateException();
		if (inst.numAttributes() != model.miner.getNumAttributes() + 1)
			throw new IllegalStateException();
		if (!model.miner.getClassValues()[0].equals(inst.classAttribute().value(0)))
			throw new IllegalArgumentException();
		if (!model.miner.getClassValues()[1].equals(inst.classAttribute().value(1)))
			throw new IllegalArgumentException();

		int seed = 1;
		inst.randomize(new Random(seed));
		model.erf = new ExtendedRandomForest();
		model.erf.setSeed(seed);
		model.erf.buildClassifier(inst);

		//		model.erf.printTrees();

		//		System.out.println(model.miner.getHashcodeViaIdx(57));
		//		System.out.println(ListUtil.toString(new ArrayList<Integer>(model.miner.getCompoundsForHashcode(model.miner
		//				.getHashcodeViaIdx(57)))));

		//		System.out.println(ListUtil.toString(new ArrayList<Integer>(model.miner.getCompoundsForHashcode(model.miner
		//				.getHashcodeViaIdx(0)))));
		int idx = 1;
		System.out.println("idx " + idx);
		System.out.println("hash-code " + model.miner.getHashcodeViaIdx(idx));
		List<String> endpoint = new ArrayList<String>();
		for (Integer c : model.miner.getCompoundsForHashcode(model.miner.getHashcodeViaIdx(idx)))
			endpoint.add(model.miner.getEndpoints().get(c));
		System.out.println(CountedSet.create(endpoint));

		model.setActiveClassIdx(model.miner.getActiveIdx());
		model.setClassValues(model.miner.getClassValues());
		model.saveModel();

		Model m = Model.find(id);
		System.out.println("hash-code " + m.getCFPMiner().getHashcodeViaIdx(idx));
	}

}
