package org.kramerlab.coffer.api.impl.objects;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.kramerlab.cfpminer.appdomain.ADInfoModel;
import org.kramerlab.cfpminer.experiments.validation.InnerValidationResults;
import org.kramerlab.coffer.api.impl.html.ModelHtml;
import org.kramerlab.coffer.api.impl.html.ModelsHtml;
import org.kramerlab.coffer.api.impl.persistance.PersistanceAdapter;
import org.kramerlab.coffer.api.impl.provider.HTMLOwner;
import org.kramerlab.coffer.api.objects.Model;
import org.kramerlab.coffer.api.objects.Prediction;
import org.mg.cdklib.cfp.CFPMiner;
import org.mg.javalib.datamining.ResultSet;
import org.mg.javalib.datamining.ResultSetIO;
import org.mg.javalib.util.CountedSet;

import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.supportVector.PolyKernel;
import weka.classifiers.functions.supportVector.RBFKernel;
import weka.classifiers.trees.RandomForest;

public abstract class AbstractModel extends AbstractServiceObject
		implements Model, HTMLOwner, Serializable
{
	private static final long serialVersionUID = 10L;

	protected String id;
	protected int activeClassIdx;
	protected String classValues[];

	@Override
	public String[] getClassValues()
	{
		return classValues;
	}

	@Override
	public String getId()
	{
		return id;
	}

	@Override
	public int getActiveClassIdx()
	{
		return activeClassIdx;
	}

	@Override
	public String getLocalURI()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public void setClassValues(String[] classValues)
	{
		this.classValues = classValues;
	}

	public void setActiveClassIdx(int activeClassIdx)
	{
		this.activeClassIdx = activeClassIdx;
	}

	public void setClassifier(Classifier classifier)
	{
		this.classifier = classifier;
	}

	public void setCFPMiner(CFPMiner miner)
	{
		this.miner = miner;
	}

	public void setDatasetWarnings(List<String> datasetWarnings)
	{
		this.datasetWarnings = datasetWarnings;
	}

	protected transient CFPMiner miner;
	protected transient Classifier classifier;
	protected transient ADInfoModel appDomain;
	protected transient List<String> datasetWarnings;

	public AbstractModel()
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

	public void setAppDomain(ADInfoModel appDomain)
	{
		this.appDomain = appDomain;
	}

	public ADInfoModel getAppDomain()
	{
		if (appDomain == null)
			appDomain = PersistanceAdapter.INSTANCE.readAppDomain(id);
		return appDomain;
	}

	@Override
	public String getHTML()
	{
		return new ModelHtml(this).build();
	}

	public static String getModelListHTML(AbstractModel models[])
	{
		return new ModelsHtml(models).build();
	}

	public String getClassifierName()
	{
		if (getClassifier() instanceof NaiveBayes)
			return "Naive Bayes";
		else if (getClassifier() instanceof RandomForest)
			return "Random Forest (trees: " + ((RandomForest) getClassifier()).getNumTrees() + ")";
		else if (getClassifier() instanceof SMO)
		{
			SMO smo = (SMO) getClassifier();
			String name = "Support Vector Machine (c:";
			if (smo.getC() % 1.0 == 0)
				name += (int) smo.getC();
			else
				name += smo.getC();
			if (smo.getKernel() instanceof PolyKernel)
			{
				if (((PolyKernel) smo.getKernel()).getExponent() == 1)
					name += ", Linear";
				else
					name += ", Poly-Kernel e:" + ((PolyKernel) smo.getKernel()).getExponent();
			}
			else if (smo.getKernel() instanceof RBFKernel)
			{
				name += ", RBF-Kernel \u03B3:" + ((RBFKernel) smo.getKernel()).getGamma();
			}
			return name + ")";
		}
		throw new IllegalStateException();
	}

	public Classifier getClassifier()
	{
		if (classifier == null)
			classifier = PersistanceAdapter.INSTANCE.readClassifier(id);
		return classifier;
	}

	public CFPMiner getCFPMiner()
	{
		if (miner == null)
			miner = PersistanceAdapter.INSTANCE.readCFPMiner(id);
		return miner;
	}

	public List<String> getDatasetWarnings()
	{
		if (datasetWarnings == null)
			datasetWarnings = PersistanceAdapter.INSTANCE.readModelDatasetWarnings(id);
		return datasetWarnings;
	}

	@Override
	public String getEndpointsSummary()
	{
		return CountedSet.create(getCFPMiner().getEndpoints()).toString();
	}

	@Override
	public String getNiceFragmentDescription()
	{
		return getCFPMiner().getNiceFragmentDescription();
	}

	@Override
	public int getNumFragments()
	{
		return getCFPMiner().getNumFragments();
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
			String valPng = PersistanceAdapter.INSTANCE.getModelValidationImageFile(id);
			//			if (!new File(valPng).exists())
			//			{
			ResultSet rs = ResultSetIO.readFromFile(
					new File(PersistanceAdapter.INSTANCE.getModelValidationResultsFile(id)));

			InnerValidationResults.plotValidationResult(rs, valPng);
			//CFPNestedValidation.plotValidationResult(rs, valPng);
			//			}
			return new FileInputStream(valPng);
			//			ValidationResultsProvider val = new ValidationResultsProvider(
			//					PersistanceAdapter.INSTANCE.getModelValidationResultsFile(id));
			//			String valPng = PersistanceAdapter.INSTANCE.getModelValidationImageFile(id);
			//			val.plot(valPng);
			//			return new FileInputStream(valPng);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public String getTarget()
	{
		return StringUtils.capitalize(getTarget(id));
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

	public static void deleteAllModels() throws Exception
	{
		for (Model m : AbstractModel.listModels())
			PersistanceAdapter.INSTANCE.deleteModel(m.getId());
	}

	public static void predict(AbstractModel model, String smiles,
			boolean createPredictionAttributes) throws Exception
	{
		Prediction p = AbstractPrediction.createPrediction(model, smiles,
				createPredictionAttributes);
		System.out.println(p.getPredictedDistribution()[model.getActiveClassIdx()]);
	}

}
