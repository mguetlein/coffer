package org.kramerlab.cfpservice.api.impl.html;

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
		newSection("Welcome");
		addParagraph(text("home.welcome") + " " + HTMLReport.encodeLink("/doc", "Documentation"));
		addGap();

		newSection("Make prediction");
		addForm("/", "compound", "Predict", "Please insert SMILES string");
		addGap();
		ResultSet set = new ResultSet();

		Model[] models = Model.listModels();
		for (Model m : models)
		{
			int idx = set.addResult();
			set.setResultValue(idx, "Dataset", HTMLReport.encodeLink(m.getId(), m.getName()));
			set.setResultValue(idx, "Target", HTMLReport.encodeLink(m.getId(), m.getTarget()));
		}
		startInlinesTables();
		newSection("Prediction models");
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
				String url = "/prediction/" + predIds[i];
				res.setResultValue(rIdx, "Recent predictions", HTMLReport.encodeLink(url, p.getSmiles()));
				//				res.setResultValue(rIdx, "Date",
				//						HTMLReport.encodeLink(url, new SimpleDateFormat("yyyy-MM-dd HH:mm").format(p.getDate())));
				//					res.setResultValue(rIdx, "Prediction", HTMLReport.getHTMLCode(PredictionReport.getPredictionString(
				//							p.getPredictedDistribution(), getClassValues(), p.getPredictedIdx(), true)));
			}
			if (res.getNumResults() > 0)
			{
				//				addGap();
				addTable(res);
			}
		}
		stopInlineTables();

		return close();
	}
}
