package org.kramerlab.cfpservice.api.impl.html;

import org.kramerlab.cfpservice.api.ModelService;
import org.kramerlab.cfpservice.api.impl.Model;
import org.kramerlab.cfpservice.api.impl.Prediction;
import org.mg.javalib.datamining.ResultSet;

public class ModelsHtml extends DefaultHtml
{
	Model[] models;

	public ModelsHtml(Model[] models)
	{
		super(null, null, null, null);
		this.models = models;
	}

	public String build() throws Exception
	{
		newSection("Welcome");
		addParagraph(text("home.welcome") + " " + encodeLink("/doc", "Documentation"));
		addGap();

		newSection("Make prediction");
		addForm("/", ModelService.PREDICT_PARAM_COMPOUND_SMILES, "Predict", "Please insert SMILES string");
		addGap();
		ResultSet set = new ResultSet();

		for (Model m : models)
		{
			int idx = set.addResult();
			set.setResultValue(idx, "Dataset", encodeLink("/" + m.getId(), m.getName()));
			set.setResultValue(idx, "Target", encodeLink("/" + m.getId(), m.getTarget()));
		}

		String[] modelIds = new String[models.length];
		for (int i = 0; i < modelIds.length; i++)
			modelIds[i] = models[i].getId();
		String predIds[] = Prediction.findAllPredictions(modelIds);
		if (predIds.length > 0)
			startLeftColumn();

		newSection("Prediction models");
		setTableColWidthLimited(false);
		addTable(set);

		if (predIds.length > 0)
		{
			startRightColumn();
			ResultSet res = new ResultSet();
			for (int i = 0; i < Math.min(predIds.length, 10); i++)
			{
				Prediction p = Prediction.find(modelIds[0], predIds[i]);
				int rIdx = res.addResult();
				String url = "/prediction/" + predIds[i];
				res.setResultValue(rIdx, "Compounds", encodeLink(url, p.getSmiles()));
				//				res.setResultValue(rIdx, "Date",
				//						HTMLReport.encodeLink(url, new SimpleDateFormat("yyyy-MM-dd HH:mm").format(p.getDate())));
				//					res.setResultValue(rIdx, "Prediction", HTMLReport.getHTMLCode(PredictionReport.getPredictionString(
				//							p.getPredictedDistribution(), getClassValues(), p.getPredictedIdx(), true)));
			}

			newSection("Recent predictions");
			addTable(res);
			stopColumns();
		}

		return close();
	}
}
