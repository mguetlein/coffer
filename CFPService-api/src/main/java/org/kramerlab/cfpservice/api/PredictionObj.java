package org.kramerlab.cfpservice.api;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@SuppressWarnings("restriction")
@XmlRootElement
public class PredictionObj implements Serializable
{
	private static final long serialVersionUID = 3L;

	@XmlAttribute
	protected String id;
	@XmlAttribute
	protected String smiles;
	@XmlAttribute
	protected String modelId;
	@XmlAttribute
	protected String predictedClass;
	@XmlAttribute
	protected double predictedDistribution[];

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
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

	public String getPredictedClass()
	{
		return predictedClass;
	}

	public void setPredictedClass(String predictedClass)
	{
		this.predictedClass = predictedClass;
	}

	public double[] getPredictedDistribution()
	{
		return predictedDistribution;
	}

	public void setPredictedDistribution(double[] predictedDistribution)
	{
		this.predictedDistribution = predictedDistribution;
	}
}
