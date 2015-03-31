package org.kramerlab.cfpservice.api.impl.html;

import java.text.SimpleDateFormat;

import org.kramerlab.cfpservice.api.impl.Model;
import org.kramerlab.cfpservice.api.impl.Prediction;
import org.mg.htmlreporting.HTMLReport;
import org.mg.javalib.datamining.ResultSet;

public class ModelsHtml extends ExtendedHtmlReport
{
	public ModelsHtml()
	{
		super(null, null, null, null);
	}

	public String build() throws Exception
	{
		newSubsection("Make prediction");
		addForm("/", "compound", "Predict", "Please insert SMILES string");
		addGap();
		ResultSet set = new ResultSet();
		Model[] models = Model.listModels();
		for (Model m : models)
		{
			int idx = set.addResult();
			set.setResultValue(idx, "List of models", HTMLReport.encodeLink(m.getId(), m.getId()));
		}
		addTable(set);

		String[] modelIds = new String[models.length];
		for (int i = 0; i < modelIds.length; i++)
			modelIds[i] = models[i].getId();
		String predIds[] = Prediction.findLastPredictions(modelIds);
		if (predIds.length > 0)
		{
			ResultSet res = new ResultSet();
			for (int i = 0; i < Math.min(predIds.length, 5); i++)
			{
				Prediction p = Prediction.find(modelIds[0], predIds[i]);
				int rIdx = res.addResult();
				res.setResultValue(rIdx, "Recent predictions",
						HTMLReport.encodeLink("/prediction/" + predIds[i], p.getSmiles()));
				res.setResultValue(rIdx, "Date", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(p.getDate()));
				//					res.setResultValue(rIdx, "Prediction", HTMLReport.getHTMLCode(PredictionReport.getPredictionString(
				//							p.getPredictedDistribution(), getClassValues(), p.getPredictedIdx(), true)));
			}
			if (res.getNumResults() > 0)
			{
				addGap();
				addTable(res);
			}
		}

		return close();
	}
}
