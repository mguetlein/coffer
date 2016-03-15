package org.kramerlab.cfpservice.api.objects;

import java.util.List;

public interface Prediction extends ServiceResource
{
	public String getId();

	public String getSmiles();

	public String getModelId();

	public int getPredictedIdx();

	public double[] getPredictedDistribution();

	public String getTrainingActivity();

	public List<SubgraphPredictionAttribute> getPredictionAttributes();

}