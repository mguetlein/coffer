package org.kramerlab.coffer.api.impl.persistance;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kramerlab.cfpminer.appdomain.ADInfoModel;
import org.kramerlab.coffer.api.impl.objects.AbstractModel;
import org.kramerlab.coffer.api.impl.objects.AbstractPrediction;
import org.kramerlab.coffer.api.objects.Model;
import org.kramerlab.coffer.api.objects.Prediction;
import org.mg.cdklib.cfp.CFPMiner;

import weka.classifiers.Classifier;

public interface PersistanceAdapter
{
	public static PersistanceAdapter INSTANCE = new FilePersistanceAdapter();

	public boolean modelExists(String modelId);

	public Classifier readClassifier(String modelId);

	public CFPMiner readCFPMiner(String modelId);

	public Model[] readModels();

	public List<String> readDatasetSmiles(String modelId);

	public List<String> readModelDatasetWarnings(String id);

	public List<String> readDatasetEndpoints(String modelId);

	public void saveModel(AbstractModel model);

	public void savePrediction(AbstractPrediction prediction);

	public Prediction readPrediction(String modelId, String predictionId);

	public String[] findAllPredictions(String... modelIds);

	public String getModelValidationResultsFile(String id);

	public String getModelValidationImageFile(String id);

	public boolean predictionExists(String modelId, String predictionId);

	public void updateDate(String modelId, String predictionId);

	public Model readModel(String id);

	public Date getPredictionDate(String modelId, String id);

	public String getModelEndpoint(String modelId);

	public Set<String> getModelDatasetURLs(String modelId);

	public Map<String, String> getModelDatasetCitations(String modelId);

	public void deleteModel(String modelId);

	public void deletePrediction(String modelId, String predictionId);

	public ADInfoModel readAppDomain(String modelId);
}
