package org.kramerlab.coffer.api.impl.ot;

import org.kramerlab.coffer.api.ModelService;
import org.kramerlab.coffer.api.impl.objects.AbstractCompound;
import org.kramerlab.coffer.api.impl.objects.AbstractModel;
import org.kramerlab.coffer.api.impl.objects.AbstractPrediction;
import org.kramerlab.coffer.api.ot.OpenToxModel;
import org.kramerlab.coffer.api.ot.OpenToxPrediction;

public class PredictionImpl extends AbstractPrediction implements OpenToxPrediction
{
	private static final long serialVersionUID = 1L;

	@Override
	public String[] getType()
	{
		return new String[] { ModelService.OPENTOX_API_PREFIX + ".Dataset" };
	}

	@Override
	public DataEntry getDataEntry()
	{
		return new DataEntry()
		{
			@Override
			public FeatureValue[] getValues()
			{
				return new FeatureValue[] { new FeatureValue()
						{
							@Override
							public Object getValue()
							{
								return AbstractModel.find(getModelId()).getClassValues()[getPredictedIdx()];
							}

							@Override
							public String getFeature()
							{
								return ((OpenToxModel) AbstractModel.find(getModelId()))
										.getPredictedVariables()[0];
							}
						}, new FeatureValue()
						{
							@Override
							public Object getValue()
							{
								return getPredictedDistribution()[getPredictedIdx()];
							}

							@Override
							public String getFeature()
							{
								return ((OpenToxModel) AbstractModel.find(getModelId()))
										.getPredictedVariables()[1];
							}
						} };
			}

			@Override
			public String getCompound()
			{
				AbstractCompound c = new CompoundImpl();
				c.setSmiles(getSmiles());
				return c.getURI();
			}
		};
	};
}