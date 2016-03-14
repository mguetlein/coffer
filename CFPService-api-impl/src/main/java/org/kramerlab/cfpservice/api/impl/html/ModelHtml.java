package org.kramerlab.cfpservice.api.impl.html;

import java.io.IOException;
import java.util.List;

import org.kramerlab.cfpservice.api.ModelService;
import org.kramerlab.cfpservice.api.impl.Model;
import org.kramerlab.cfpservice.api.impl.Prediction;
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

		List<String> w = m.getDatasetWarnings();
		if (w != null && w.size() > 0)
			set.setResultValue(idx, "Dataset warnings",
					getMouseoverHelp(getList(w), w.size() + " warnings"));

		//set.setResultValue(idx, "Num compounds", m.getCFPMiner().getNumCompounds());
		set.setResultValue(idx, "Compounds", CountedSet.create(m.getCFPMiner().getEndpoints()));

		//		ResultSet setM = new ResultSet();
		//		setM.addResult();

		//		set.setResultValue(idx, "Classifier", getMouseoverHelp(
		//				text("model.tip") + " " + moreLink(DocHtml.CLASSIFIERS), m.getClassifierName()));
		set.setResultValue(idx, "Classifier", m.getClassifierName());

		set.setResultValue(idx, "Features",
				getMouseoverHelp(
						text("fragment.type.tip") + " " + moreLink(DocHtml.FILTERED_FRAGMENTS),
						m.getCFPMiner().getNiceFragmentDescription()));
		set.setResultValue(idx, "Num features", new Renderable()
		{
			public void renderOn(HtmlCanvas html) throws IOException
			{
				html.write(m.getCFPMiner().getNumFragments() + " ");
				new TextWithLinks(
						encodeLink("/" + m.getId() + "/fragment/1", "(inspect fragments)"), true,
						false).renderOn(html);
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
		addImage(getImage("/" + m.getId() + "/validation",
				"/doc#" + DocHtml.getAnker(DocHtml.VALIDATION), false));

		stopColumns();
		setTableColWidthLimited(false);

		addGap();
		newSection("Make prediction");
		addForm("/" + m.getId(), ModelService.PREDICT_PARAM_COMPOUND_SMILES, "Predict",
				"Please insert SMILES string");

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
				res.setResultValue(rIdx, "Prediction",
						PredictionHtml.getPrediction(p, m.getClassValues(), true, url));
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
