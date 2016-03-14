package org.kramerlab.cfpservice.api;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlType(name = "Dataset", namespace = ModelService.OPENTOX_API)
public abstract class PredictionObj extends ServiceObj implements Serializable
{
	private static final long serialVersionUID = 6L;

	protected String id;
	protected String smiles;
	protected String modelId;
	protected int predictedIdx;
	protected double predictedDistribution[];
	protected String trainingActivity;

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	@Override
	public String getPath()
	{
		return "/" + modelId + "/prediction/";
	}

	public String getSmiles()
	{
		return smiles;
	}

	public void setSmiles(String smiles)
	{
		this.smiles = smiles;
	}

	public String getModelId()
	{
		return modelId;
	}

	public void setModelId(String modelId)
	{
		this.modelId = modelId;
	}

	public int getPredictedIdx()
	{
		return predictedIdx;
	}

	public void setPredictedIdx(int predictedIdx)
	{
		this.predictedIdx = predictedIdx;
	}

	public double[] getPredictedDistribution()
	{
		return predictedDistribution;
	}

	public void setPredictedDistribution(double[] predictedDistribution)
	{
		this.predictedDistribution = predictedDistribution;
	}

	public String getTrainingActivity()
	{
		return trainingActivity;
	}

	public void setTrainingActivity(String trainingActivity)
	{
		this.trainingActivity = trainingActivity;
	}

	@XmlType(name = "FeatureValue", namespace = ModelService.OPENTOX_API)
	public static abstract class FeatureValue
	{
		@XmlAttribute(namespace = ModelService.OPENTOX_API)
		public abstract String getFeature();

		@XmlAttribute(namespace = ModelService.OPENTOX_API)
		public abstract Object getValue();
	}

	@XmlType(name = "DataEntry", namespace = ModelService.OPENTOX_API)
	public static abstract class DataEntry
	{
		@XmlAttribute(namespace = ModelService.OPENTOX_API)
		public abstract String getCompound();

		@XmlElement(namespace = ModelService.OPENTOX_API)
		public abstract FeatureValue[] getValues();
	}

	@XmlElement(namespace = ModelService.OPENTOX_API)
	public abstract DataEntry getDataEntry();
}
