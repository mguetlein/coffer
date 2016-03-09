package org.kramerlab.cfpservice.api.impl.html;

import java.util.Comparator;

import org.kramerlab.cfpservice.api.impl.Model;
import org.kramerlab.cfpservice.api.impl.Prediction;
import org.mg.javalib.datamining.ResultSet;

public class PredictionsHtml extends DefaultHtml
{
	Prediction[] predictions;

	public PredictionsHtml(Prediction[] predictions)
	{
		super(predictions[0].getId(), "Prediction", null, null);
		setHidePageTitle(true);
		this.predictions = predictions;
	}

	public String build() throws Exception
	{
		String smiles = predictions[0].getSmiles();

		int count = 0;
		ResultSet res = new ResultSet();

		for (Prediction p : predictions)
		{
			if (p == null)
				break;

			int idx = res.addResult();
			String url = "/" + p.getModelId() + "/prediction/" + p.getId();
			//				res.setResultValue(idx, "Model", HTMLReport.encodeLink(url /*"/" + m.getId()*/, m.getId()));
			smiles = p.getSmiles();
			Model m = Model.find(p.getModelId());
			res.setResultValue(idx, "Dataset", encodeLink(url, m.getName()));
			res.setResultValue(idx, "Target", encodeLink(url, m.getTarget()));
			String endpoint = p.getTrainingActivity();
			if (endpoint != null)
			{
				res.setResultValue(idx, text("model.measured"), encodeLink(url, endpoint));
				setHeaderHelp(text("model.measured"), text("model.measured.tip"));
			}
			res.setResultValue(idx, "Prediction",
					PredictionHtml.getPrediction(p, m.getClassValues(), true, url));
			res.setResultValue(idx, "p", p.getPredictedDistribution()[m.getActiveClassIdx()]);
			//							HTMLReport.encodeLink("/" + m.getId() + "/prediction/" + predictionId,
			//									p.getPredictedClass())
			count++;
		}
		if (count < predictions.length)
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
		Image img = getImage(depict(smiles, maxMolPicSize), depict(smiles, -1), false);
		ResultSet set = new ResultSet();
		int rIdx = set.addResult();
		set.setResultValue(rIdx, "Test compound", img);
		PredictionHtml.setAdditionalInfo(this, set, rIdx, smiles);

		addTable(set);
		addGap();

		if (count < predictions.length)
		{
			startLeftColumn();
			addImage("/img/wait.gif");
			startRightColumn();
			addGap();
			addParagraph(count + "/" + predictions.length
					+ " model predictions done, this page reloads every 10 seconds.");
			stopColumns();
		}

		newSection("Predictions (select to show fragments)");
		//newSubsection("Select a target to list fragments that explain each prediction:");
		addTable(res);

		return close();
	}
}
