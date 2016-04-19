package org.kramerlab.coffer.api.impl.html;

import org.kramerlab.coffer.api.ModelService;
import org.kramerlab.coffer.api.impl.objects.AbstractPrediction;
import org.kramerlab.coffer.api.objects.Model;
import org.kramerlab.coffer.api.objects.Prediction;
import org.mg.javalib.datamining.ResultSet;

public class ModelsHtml extends DefaultHtml
{
	Model[] models;

	public ModelsHtml(Model[] models)
	{
		super(null, null, null, null);
		this.models = models;
	}

	public String build()
	{
		newSection("CoFFer", new TextWithLinks(
				text("home.welcome") + " " + encodeLink("/doc", "Learn more >>"), true, false));

		//newSection("Make prediction");
		addForm("/", ModelService.PREDICT_PARAM_COMPOUND_SMILES, "Predict compound",
				"Please insert SMILES string");
		addGap();
		ResultSet set = new ResultSet();

		for (Model m : models)
		{
			int idx = set.addResult();
			//			set.setResultValue(idx, "Prediction model", encodeLink("/" + m.getId(), m.getName()));
			//			set.setResultValue(idx, "Target", encodeLink("/" + m.getId(), m.getTarget()));

			set.setResultValue(idx, "Prediction models",
					doubleText(m.getTarget(), m.getName(), "/" + m.getId()));
		}

		String[] modelIds = new String[models.length];
		for (int i = 0; i < modelIds.length; i++)
			modelIds[i] = models[i].getId();
		String predIds[] = AbstractPrediction.findAllPredictions(modelIds);
		if (predIds.length > 0)
			startLeftColumn();

		//		newSection("Prediction models");
		setTableColWidthLimited(false);
		addTable(set);

		if (predIds.length > 0)
		{
			startRightColumn();
			ResultSet res = new ResultSet();
			for (int i = 0; i < Math.min(predIds.length, 10); i++)
			{
				Prediction p = AbstractPrediction.find(modelIds[0], predIds[i]);
				int rIdx = res.addResult();
				String url = "/prediction/" + predIds[i];
				res.setResultValue(rIdx, "Recent predictions", encodeLink(url, p.getSmiles()));
				//				res.setResultValue(rIdx, "Date",
				//						HTMLReport.encodeLink(url, new SimpleDateFormat("yyyy-MM-dd HH:mm").format(p.getDate())));
				//					res.setResultValue(rIdx, "Prediction", HTMLReport.getHTMLCode(PredictionReport.getPredictionString(
				//							p.getPredictedDistribution(), getClassValues(), p.getPredictedIdx(), true)));
			}

			//			newSection("Recent predictions");
			addTable(res);
			stopColumns();
		}

		return close();
	}
}
