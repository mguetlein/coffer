package org.kramerlab.cfpservice.api.impl.html;

import org.kramerlab.cfpminer.appdomain.ADPrediction;
import org.kramerlab.cfpservice.api.impl.objects.AbstractModel;
import org.kramerlab.cfpservice.api.objects.Model;
import org.kramerlab.cfpservice.api.objects.Prediction;
import org.mg.javalib.datamining.ResultSet;
import org.mg.javalib.util.DefaultComparator;
import org.mg.javalib.util.ListUtil;

public class PredictionsHtml extends DefaultHtml
{
	Prediction[] predictions;

	public PredictionsHtml(Prediction[] predictions)
	{
		super(predictions[0].getId(), "Prediction", null, null);
		setHidePageTitle(true);
		this.predictions = predictions;
	}

	public String build()
	{
		String smiles = predictions[0].getSmiles();

		int count = 0;
		ResultSet res = new ResultSet();

		for (Prediction p : predictions)
		{
			if (p == null || p.getId() == null)
				break;

			int idx = res.addResult();
			String url = "/" + p.getModelId() + "/prediction/" + p.getId();
			//				res.setResultValue(idx, "Model", HTMLReport.encodeLink(url /*"/" + m.getId()*/, m.getId()));
			smiles = p.getSmiles();
			Model m = AbstractModel.find(p.getModelId());
			res.setResultValue(idx, "Dataset", encodeLink(url, m.getName()));
			res.setResultValue(idx, "Target", encodeLink(url, m.getTarget()));
			String endpoint = p.getTrainingActivity();
			if (endpoint != null)
				res.setResultValue(idx, text("model.measured"), encodeLink(url, endpoint));

			//res.setResultValue(idx, " ", getImage("/depictActiveIcon?probability="
			//+ p.getPredictedDistribution()[m.getActiveClassIdx()]));

			res.setResultValue(idx, "Prediction", PredictionHtml.getPredictionWithIcon(p, m, url));
			res.setResultValue(idx, "App-Domain", getInsideAppDomainCheck(p, url));
			res.setResultValue(idx, "p", p.getPredictedDistribution()[m.getActiveClassIdx()]);
			res.setResultValue(idx, "a", p.getADPrediction());
			//							HTMLReport.encodeLink("/" + m.getId() + "/prediction/" + predictionId,
			//									p.getPredictedClass())
			count++;
		}
		if (count < predictions.length)
			setRefresh(5);

		res.sortProperties(
				ListUtil.createList("Dataset", "Target", "Measured", "Prediction", "App-Domain"));
		setHeaderHelp("Prediction",
				text("model.prediction.tip") + " " + moreLink(DocHtml.CLASSIFIERS));
		setHeaderHelp("App-Domain",
				text("appdomain.help.general") + " " + moreLink(DocHtml.APP_DOMAIN));
		setHeaderHelp(text("model.measured"), text("model.measured.tip.single"));

		res.sortResults("p", new DefaultComparator<Double>(false));
		res.removePropery("p");
		res.sortResults("a", new DefaultComparator<ADPrediction>());
		res.removePropery("a");

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
					+ " model predictions done, this page reloads every 5 seconds.");
			stopColumns();
		}

		//newSubsection("Select a target to list fragments that explain each prediction:");

		newSection("Predictions (select to show fragments)");
		addTable(res);

		return close();
	}
}
