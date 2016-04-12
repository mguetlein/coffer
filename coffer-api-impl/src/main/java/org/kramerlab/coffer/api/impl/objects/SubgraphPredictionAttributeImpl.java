package org.kramerlab.coffer.api.impl.objects;

import org.kramerlab.coffer.api.objects.SubgraphPredictionAttribute;
import org.mg.wekalib.attribute_ranking.PredictionAttributeImpl;

public class SubgraphPredictionAttributeImpl extends PredictionAttributeImpl
		implements SubgraphPredictionAttribute
{
	private static final long serialVersionUID = 2L;

	public boolean hasSuperGraph = false;

	public boolean hasSubGraph = false;

	public SubgraphPredictionAttributeImpl()
	{
	}

	public SubgraphPredictionAttributeImpl(int attribute, double[] alternativeDistributionForInstance,
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

	@Override
	public boolean hasSuperGraph()
	{
		return hasSuperGraph;
	}

	public void setHashSubGraph(boolean hasSubGraph)
	{
		this.hasSubGraph = hasSubGraph;
	}

	@Override
	public boolean hasSubGraph()
	{
		return hasSubGraph;
	}
}
