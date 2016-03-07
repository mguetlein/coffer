package org.kramerlab.cfpservice.api.impl;

import org.mg.wekalib.attribute_ranking.PredictionAttribute;

public class SubgraphPredictionAttribute extends PredictionAttribute
{
	private static final long serialVersionUID = 2L;

	public boolean hasSuperGraph = false;

	public boolean hasSubGraph = false;

	public SubgraphPredictionAttribute()
	{
	}

	public SubgraphPredictionAttribute(int attribute, double[] alternativeDistributionForInstance,
			double diffToOrigProp, boolean hasSuperGraph, boolean hasSubGraph)
	{
		super(attribute, alternativeDistributionForInstance, diffToOrigProp);
		this.hasSuperGraph = hasSuperGraph;
		this.hasSubGraph = hasSubGraph;
	}

	public void setHasSuperGraph(boolean hasSuperGraph)
	{
		this.hasSuperGraph = hasSuperGraph;
	}

	public boolean hasSuperGraph()
	{
		return hasSuperGraph;
	}

	public void setHashSubGraph(boolean hasSubGraph)
	{
		this.hasSubGraph = hasSubGraph;
	}

	public boolean hasSubGraph()
	{
		return hasSubGraph;
	}
}
