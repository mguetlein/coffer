package org.kramerlab.coffer.api.objects;

import org.mg.wekalib.attribute_ranking.PredictionAttribute;

public interface SubgraphPredictionAttribute extends PredictionAttribute
{
	public boolean hasSuperGraph();

	public boolean hasSubGraph();
}
