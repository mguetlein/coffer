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

import org.kramerlab.cfpminer.CFPDataLoader;
import org.kramerlab.cfpminer.CFPMiner;
import org.kramerlab.cfpminer.CFPMiner.CFPType;
import org.kramerlab.cfpminer.CFPtoArff;
import org.kramerlab.cfpminer.weka.ValidationResultsProvider;
import org.kramerlab.cfpservice.api.ModelObj;
import org.kramerlab.cfpservice.api.impl.html.ModelHtml;
import org.kramerlab.cfpservice.api.impl.html.ModelsHtml;
import org.kramerlab.cfpservice.api.impl.persistance.PersistanceAdapter;
import org.kramerlab.extendedrandomforests.weka.ExtendedRandomForest;
import org.mg.javalib.datamining.ResultSet;
import org.mg.javalib.datamining.ResultSetIO;
import org.mg.javalib.util.CountedSet;
import org.mg.javalib.util.FileUtil;
import org.mg.javalib.util.ListUtil;

import weka.core.Instances;

@SuppressWarnings("restriction")
@XmlRootElement
public class Model extends ModelObj
{
	private static final long serialVersionUID = 2L;

	protected transient CFPMiner miner;
	protected transient ExtendedRandomForest erf;

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
		//buildModel("DUD_vegfr2");
		//buildModel("ChEMBL_61");
		//buildModel("NCTRER");
		//buildModel("CPDBAS_Mutagenicity");
		//		buildModel("AMES");
		//		buildModel("CPDBAS_Rat");
		//		buildModel("CPDBAS_Hamster");
		//		buildModel("ChEMBL_61");
		//		buildModel("MUV_859");
		//		buildModel("CPDBAS_MultiCellCall");
		//		buildModel("ChEMBL_87");

		for (String dataset : new CFPDataLoader("persistance/data").allDatasets())
		{
			//			if (!PersistanceAdapter.INSTANCE.modelExists(dataset))
			//            if (dataset.equals("AMES"))
			if (dataset.startsWith("ChEMBL") || dataset.startsWith("MUV"))
				buildModel(dataset);
			//break;
		}

		//		Model.find("CPDBAS_Mutagenicity").getValidationChart();
	}

	@SuppressWarnings("unchecked")
	public static void buildModel(String id) throws Exception
	{
		Model model = new Model();
		model.id = id;

		List<String> endpoints = PersistanceAdapter.INSTANCE.readDatasetEndpoints(id);
		List<String> smiles = PersistanceAdapter.INSTANCE.readDatasetSmiles(id);
		ListUtil.scramble(new Random(1), smiles, endpoints);

		model.miner = new CFPMiner(endpoints);
		model.miner.setFeatureSelection(CFPMiner.FeatureSelection.filt);

		ResultSet r = ResultSetIO.parseFromFile(new File(ValidationResultsProvider.RESULTS_MERGED_FOLDER
				+ "RaF_filt.best"));
		for (int i = 0; i < r.getNumResults(); i++)
		{
			if (r.getResultValue(i, "Dataset").toString().equals(id))
			{
				CFPType type = CFPType.valueOf(r.getResultValue(i, "type").toString());
				int hashfoldsize = Double.valueOf(r.getResultValue(i, "hashfoldsize").toString()).intValue();
				model.miner.setType(type);
				model.miner.setHashfoldsize(hashfoldsize);
				break;
			}
		}

		model.miner.mine(smiles);

		String outfile = PersistanceAdapter.INSTANCE.getModelValidationResultsFile(id);
		String classifier = "RaF";
		if (ValidationResultsProvider.resultsExist(id, model.miner, classifier))
			FileUtil.copy(ValidationResultsProvider.getResultsFile(id, model.miner, classifier), outfile);
		else
			CFPMiner.validate(id, 1, outfile, new String[] { classifier }, endpoints, model.miner);

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

		CFPMiner miner = m.getCFPMiner();
		for (int i = 0; i < miner.getNumAttributes(); i++)
		{
			String smi = miner.getTrainingDataSmiles().get(
					miner.getCompoundsForHashcode(miner.getHashcodeViaIdx(i)).iterator().next());
			if (miner.getAtoms(smi, miner.getHashcodeViaIdx(i)) == null)
				throw new IllegalStateException("no matching atoms in training intance");
			//			else
			//				System.out.println("attidx: " + i + ", hashcode " + miner.getHashcodeViaIdx(i) + ", smi " + smi
			//						+ ", atoms " + ArrayUtil.toString(miner.getAtoms(smi, miner.getHashcodeViaIdx(i))));
		}
	}

}
