package org.kramerlab.cfpservice.api.impl.persistance;

import java.util.Date;
import java.util.List;

import org.kramerlab.cfpminer.CFPMiner;
import org.kramerlab.cfpservice.api.impl.Model;
import org.kramerlab.cfpservice.api.impl.Prediction;
import org.kramerlab.extendedrandomforests.weka.ExtendedRandomForest;

public interface PersistanceAdapter
{
	public static PersistanceAdapter INSTANCE = new FilePersistanceAdapter();

	public boolean modelExists(String modelId);

	public ExtendedRandomForest readExtendedRandomForest(String modelId);

	public CFPMiner readCFPMiner(String modelId);

	public Model[] readModels();

	public List<String> readDatasetSmiles(String modelId);

	public List<String> readDatasetEndpoints(String modelId);

	public void saveModel(Model model);

	public void savePrediction(Prediction prediction);

	public Prediction readPrediction(String modelId, String predictionId);

	public String[] findLastPredictions(String... modelIds);

	public String getModelValidationResultsFile(String id);

	public String getModelValidationImageFile(String id);

	public boolean predictionExists(String modelId, String predictionId);

	public void updateDate(String modelId, String predictionId);

	public Model readModel(String id);

	public Date getPredictionDate(String modelId, String id);

}
