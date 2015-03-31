package org.kramerlab.cfpservice.api.impl.html;

import java.util.Comparator;

import org.kramerlab.cfpservice.api.impl.Model;
import org.kramerlab.cfpservice.api.impl.Prediction;
import org.mg.htmlreporting.HTMLReport;
import org.mg.javalib.datamining.ResultSet;

public class PredictionsHtml extends ExtendedHtmlReport
{
	String predictionId;

	public PredictionsHtml(String predictionId)
	{
		super(predictionId, "Prediction", null, null);
		this.predictionId = predictionId;
	}

	public String build() throws Exception
	{
		String smiles = null;
		ResultSet res = new ResultSet();
		for (Model m : Model.listModels())
		{
			if (Prediction.exists(m.getId(), predictionId))
			{
				int idx = res.addResult();
				res.setResultValue(idx, "Model", HTMLReport.encodeLink("/" + m.getId(), m.getId()));
				Prediction p = Prediction.find(m.getId(), predictionId);
				smiles = p.getSmiles();
				res.setResultValue(
						idx,
						"Prediction",
						HTMLReport.getHTMLCode(PredictionHtml.getPredictionString(p, m.getClassValues(), true,
								"/" + m.getId() + "/prediction/" + predictionId)));
				res.setResultValue(idx, "p", p.getPredictedDistribution()[m.getActiveClassIdx()]);
				//							HTMLReport.encodeLink("/" + m.getId() + "/prediction/" + predictionId,
				//									p.getPredictedClass())
			}
		}
		res.sortResults("p", new Comparator<Object>()
		{
			public int compare(Object o1, Object o2)
			{
				return ((Double) o2).compareTo((Double) o1);
			}
		});
		res.removePropery("p");

		setPageTitle("Prediction of compound " + smiles);
		addImage(getImage(imageProvider.drawCompound(smiles, molPicSize), imageProvider.hrefCompound(smiles), true));
		addGap();
		addParagraph("Select a prediction for a list of fragments that were employed by the prediction model.");
		addGap();
		addTable(res);
		return close();
	}
}
