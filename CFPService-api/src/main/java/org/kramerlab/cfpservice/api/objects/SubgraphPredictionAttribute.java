package org.kramerlab.cfpservice.api.objects;

import org.mg.wekalib.attribute_ranking.PredictionAttributeInterface;

public interface SubgraphPredictionAttribute extends PredictionAttributeInterface
{
	public boolean hasSuperGraph();

	public boolean hasSubGraph();
}
