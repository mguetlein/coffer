package org.kramerlab.cfpservice.api.impl.html;

import java.text.SimpleDateFormat;

import org.kramerlab.cfpservice.api.impl.Model;
import org.kramerlab.cfpservice.api.impl.Prediction;
import org.mg.htmlreporting.HTMLReport;
import org.mg.javalib.datamining.ResultSet;
import org.mg.javalib.util.CountedSet;

public class ModelHtml extends ExtendedHtmlReport
{
	Model m;

	public ModelHtml(Model m)
	{
		super("Prediction model", m.getId(), m.getId(), null, null);
		this.m = m;
	}

	public String build() throws Exception
	{
		startInlinesTables();

		ResultSet set = new ResultSet();
		int idx = set.addResult();
		set.setResultValue(idx, "Dataset", m.getId());
		set.setResultValue(idx, "Num compounds", m.getCFPMiner().getNumCompounds());
		set.setResultValue(idx, "Endpoint values", CountedSet.create(m.getCFPMiner().getEndpoints()));
		set.concatCols(m.getExtendedRandomForest().getSummary(true));
		set.concatCols(m.getCFPMiner().getSummary(true));
		addList(set);

		addImage(getImage("/" + m.getId() + "/validation"));

		stopInlineTables();

		newSubsection("Make prediction");
		addForm("/" + m.getId(), "compound", "Predict", "Please insert SMILES string");

		//			ValidationResultsProvider val = new ValidationResultsProvider(
		//					PersistanceAdapter.INSTANCE.getModelValidationResultsFile(id));
		//			addTable(val.getJoinedResults());

		String predIds[] = Prediction.findLastPredictions(m.getId());
		if (predIds.length > 0)
		{
			ResultSet res = new ResultSet();
			for (int i = 0; i < Math.min(predIds.length, 5); i++)
			{
				Prediction p = Prediction.find(m.getId(), predIds[i]);
				int rIdx = res.addResult();
				res.setResultValue(rIdx, "Recent predictions", p.getSmiles());
				res.setResultValue(
						rIdx,
						"Prediction",
						HTMLReport.getHTMLCode(PredictionHtml.getPredictionString(p, m.getClassValues(), true,
								"/" + m.getId() + "/prediction/" + predIds[i])));
				res.setResultValue(rIdx, "Date", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(p.getDate()));
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
