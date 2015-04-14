package org.kramerlab.cfpservice.api.impl.html;

import java.util.Comparator;

import org.kramerlab.cfpservice.api.impl.Model;
import org.kramerlab.cfpservice.api.impl.Prediction;
import org.mg.htmlreporting.HTMLReport;
import org.mg.javalib.datamining.ResultSet;

public class PredictionsHtml extends ExtendedHtmlReport
{
	String predictionId;
	int wait;

	public PredictionsHtml(String predictionId, int wait)
	{
		super(predictionId, "Prediction", null, null);
		setHidePageTitle(true);
		this.predictionId = predictionId;
		this.wait = wait;
	}

	public String build() throws Exception
	{
		String smiles = null;

		int count = 0;
		ResultSet res = new ResultSet();
		for (Model m : Model.listModels())
		{
			if (Prediction.exists(m.getId(), predictionId))
			{
				int idx = res.addResult();
				String url = "/" + m.getId() + "/prediction/" + predictionId;
				//				res.setResultValue(idx, "Model", HTMLReport.encodeLink(url /*"/" + m.getId()*/, m.getId()));
				Prediction p = Prediction.find(m.getId(), predictionId);
				smiles = p.getSmiles();
				res.setResultValue(idx, "Dataset", HTMLReport.encodeLink(url, m.getName()));
				res.setResultValue(idx, "Target", HTMLReport.encodeLink(url, m.getTarget()));
				res.setResultValue(idx, "Prediction", PredictionHtml.getPrediction(p, m.getClassValues(), true, url));
				res.setResultValue(idx, "p", p.getPredictedDistribution()[m.getActiveClassIdx()]);
				//							HTMLReport.encodeLink("/" + m.getId() + "/prediction/" + predictionId,
				//									p.getPredictedClass())
				count++;
			}
		}
		if (count < wait)
			setRefresh(10);

		res.sortResults("p", new Comparator<Object>()
		{
			public int compare(Object o1, Object o2)
			{
				return ((Double) o2).compareTo((Double) o1);
			}
		});
		res.removePropery("p");

		setPageTitle("Prediction of compound " + smiles);
		newSection("Predicted compound");
		Image img = getImage(imageProvider.drawCompound(smiles, molPicSize), imageProvider.hrefCompound(smiles), false);
		ResultSet set = new ResultSet();
		int rIdx = set.addResult();
		set.setResultValue(rIdx, "Test compound", img);
		PredictionHtml.setAdditionalInfo(this, set, rIdx, smiles);

		addTable(set);
		addGap();

		if (count < wait)
		{
			startInlinesTables();
			addImage("/img/wait.gif");
			addGap();
			addParagraph(count + "/" + wait + " model predictions done, this page reloads every 10 seconds.");
			stopInlineTables();
		}
		else
		{
			newSection("Predictions (select to list fragments)");
			//newSubsection("Select a target to list fragments that explain each prediction:");
			addTable(res);
		}
		return close();
	}
}
