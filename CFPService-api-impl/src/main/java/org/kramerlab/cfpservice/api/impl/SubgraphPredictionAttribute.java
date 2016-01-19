package org.kramerlab.cfpservice.api.impl;

import org.mg.wekalib.attribute_ranking.PredictionAttribute;

public class SubgraphPredictionAttribute extends PredictionAttribute
{
	private static final long serialVersionUID = 1L;

	public boolean isSuperGraph = false;

	public SubgraphPredictionAttribute()
	{
	}

	public SubgraphPredictionAttribute(int attribute, double[] alternativeDistributionForInstance,
			double diffToOrigProp, boolean isSuperGraph)
	{
		super(attribute, alternativeDistributionForInstance, diffToOrigProp);
		this.isSuperGraph = isSuperGraph;
	}

	public void setSuperGraph(boolean isSuperGraph)
	{
		this.isSuperGraph = isSuperGraph;
	}

	public boolean isSuperGraph()
	{
		return isSuperGraph;
	}
}
