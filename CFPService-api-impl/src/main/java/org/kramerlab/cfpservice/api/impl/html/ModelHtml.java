package org.kramerlab.cfpservice.api.impl.html;

import java.io.IOException;

import org.kramerlab.cfpservice.api.impl.Model;
import org.kramerlab.cfpservice.api.impl.Prediction;
import org.mg.htmlreporting.HTMLReport;
import org.mg.javalib.datamining.ResultSet;
import org.mg.javalib.util.CountedSet;
import org.rendersnake.HtmlCanvas;
import org.rendersnake.Renderable;

public class ModelHtml extends DefaultHtml
{
	Model m;

	public ModelHtml(Model m)
	{
		super("Prediction model", m.getId(), m.getName(), null, null);
		this.m = m;
	}

	public String build() throws Exception
	{
		ResultSet set = new ResultSet();

		int idx = set.addResult();

		set.setResultValue(idx, "Dataset name", m.getName());
		set.setResultValue(idx, "Target", m.getTarget());

		Renderable citations = null;
		for (String key : m.getDatasetCitations().keySet())
		{
			Renderable c = getExternalLink(key, null, m.getDatasetCitations().get(key));
			if (citations == null)
				citations = c;
			else
				citations = HTMLReport.join(citations, c);
		}
		set.setResultValue(idx, "Dataset sources", citations);

		set.setResultValue(idx, "Num compounds", m.getCFPMiner().getNumCompounds());
		set.setResultValue(idx, "Endpoint values", CountedSet.create(m.getCFPMiner().getEndpoints()));

		//		ResultSet setM = new ResultSet();
		//		setM.addResult();
		set.setResultValue(idx, "Classifier",
				getMouseoverHelp(text("model.tip") + " " + moreLink(DocHtml.CLASSIFIERS), m.getClassifier().getName()));

		set.setResultValue(
				idx,
				"Fragment type",
				getMouseoverHelp(text("fragment.type.tip") + " " + moreLink(DocHtml.FRAGMENTS), m.getCFPMiner()
						.getFeatureType()));
		set.setResultValue(idx, "Num fragments", new Renderable()
		{
			public void renderOn(HtmlCanvas html) throws IOException
			{
				html.write(m.getCFPMiner().getNumFragments() + " ");
				new TextWithLinks(encodeLink("/" + m.getId() + "/fragment/1", "(inspect fragments)"), true)
						.renderOn(html);
				//html.write("bla");
			}
		});
		//				 + " " + ;

		//		set.concatCols(m.getCFPMiner().getSummary(true));
		//set.setResultValue(idx, "Model", getList(setM));

		setTableColWidthLimited(true);
		setTableRowsAlternating(false);
		setHideTableBorder(true);
		startLeftColumn();
		addTable(set, true);

		setHideTableBorder(false);
		setTableRowsAlternating(true);
		startRightColumn();
		addImage(HTMLReport.getImage("/" + m.getId() + "/validation"));

		stopColumns();
		setTableColWidthLimited(false);

		addGap();
		newSection("Make prediction");
		addForm("/" + m.getId(), "compound", "Predict", "Please insert SMILES string");

		//			ValidationResultsProvider val = new ValidationResultsProvider(
		//					PersistanceAdapter.INSTANCE.getModelValidationResultsFile(id));
		//			addTable(val.getJoinedResults());

		String predIds[] = Prediction.findAllPredictions(m.getId());
		if (predIds.length > 0)
		{
			ResultSet res = new ResultSet();
			for (int i = 0; i < Math.min(predIds.length, 10); i++)
			{
				Prediction p = Prediction.find(m.getId(), predIds[i]);
				String url = "/" + m.getId() + "/prediction/" + predIds[i];
				int rIdx = res.addResult();
				res.setResultValue(rIdx, "Compound", encodeLink(url, p.getSmiles()));
				res.setResultValue(rIdx, "Prediction", PredictionHtml.getPrediction(p, m.getClassValues(), true, url));
				//				res.setResultValue(rIdx, "Date", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(p.getDate()));
			}
			if (res.getNumResults() > 0)
			{
				addGap();
				newSection("Recent predictions");
				addTable(res);
			}
		}

		return close();

	}
}