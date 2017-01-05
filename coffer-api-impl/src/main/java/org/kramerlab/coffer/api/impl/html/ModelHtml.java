package org.kramerlab.coffer.api.impl.html;

import java.io.IOException;
import java.util.List;

import org.kramerlab.coffer.api.ModelService;
import org.kramerlab.coffer.api.impl.objects.AbstractFragment;
import org.kramerlab.coffer.api.impl.objects.AbstractPrediction;
import org.kramerlab.coffer.api.objects.Fragment;
import org.kramerlab.coffer.api.objects.Model;
import org.kramerlab.coffer.api.objects.Prediction;
import org.mg.javalib.datamining.ResultSet;
import org.mg.javalib.util.ListUtil;
import org.rendersnake.HtmlAttributesFactory;
import org.rendersnake.HtmlCanvas;
import org.rendersnake.Renderable;

public class ModelHtml extends DefaultHtml
{
	Model m;

	public ModelHtml(Model m)
	{
		super(m.getTarget() + " \u2500 " + m.getName(), m.getId(), m.getName(), null, null);
		this.m = m;
	}

	public String build()
	{
		ResultSet set = new ResultSet();

		int idx = set.addResult();

		//		set.setResultValue(idx, "Dataset name", m.getName());
		//		set.setResultValue(idx, "Target", m.getTarget());

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
		set.setResultValue(idx, "Compounds", m.getEndpointsSummary());

		//		ResultSet setM = new ResultSet();
		//		setM.addResult();

		//		set.setResultValue(idx, "Classifier", getMouseoverHelp(
		//				text("model.tip") + " " + moreLink(DocHtml.CLASSIFIERS), m.getClassifierName()));
		set.setResultValue(idx, "Classifier", m.getClassifierName());

		set.setResultValue(idx, "Features",
				getMouseoverHelp(
						text("fragment.type.tip") + " " + moreLink(DocHtml.FILTERED_FRAGMENTS),
						m.getNiceFragmentDescription()));
		set.setResultValue(idx, "Num features", new Renderable()
		{
			public void renderOn(HtmlCanvas html) throws IOException
			{
				html.write(m.getNumFragments() + " ");
				Fragment f = AbstractFragment.find(m.getId(), "1");
				html.div(HtmlAttributesFactory.class_("smallGrey").style("display: inline;"));
				new TextWithLinks(encodeLink(f.getLocalURI(), "(inspect fragments)"), true, false)
						.renderOn(html);
				html._div();
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
		//		newSection("Make prediction");
		addForm("" + m.getId(), ModelService.PREDICT_PARAM_COMPOUND_SMILES, "Predict compound",
				"Please insert SMILES string");

		//			ValidationResultsProvider val = new ValidationResultsProvider(
		//					PersistanceAdapter.INSTANCE.getModelValidationResultsFile(id));
		//			addTable(val.getJoinedResults());

		String predIds[] = AbstractPrediction.findAllPredictions(m.getId());
		if (predIds.length > 0)
		{
			ResultSet res = new ResultSet();
			for (int i = 0; i < Math.min(predIds.length, 10); i++)
			{
				Prediction p = AbstractPrediction.find(m.getId(), predIds[i]);
				String url = p.getLocalURI();
				int rIdx = res.addResult();
				res.setResultValue(rIdx, "Compound",
						new TextWithLinks(encodeLink(url, p.getSmiles()), false, false, true));
				String endpoint = p.getTrainingActivity();
				if (endpoint != null)
					res.setResultValue(rIdx, text("model.measured"), encodeLink(url, endpoint));

				res.setResultValue(rIdx, "Prediction", getPredictionWithIcon(p, m, url));
				if (ModelService.APP_DOMAIN_VISIBLE)
					res.setResultValue(rIdx, "App-Domain", getInsideAppDomainCheck(p, url));
				//				res.setResultValue(rIdx, "Date", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(p.getDate()));
			}
			if (res.getNumResults() > 0)
			{
				addGap();
				newSection("Recent predictions");

				setHeaderHelp("Prediction",
						text("model.prediction.tip") + " " + moreLink(DocHtml.CLASSIFIERS));
				setHeaderHelp("App-Domain",
						AppDomainHtml.getGeneralInfo() + " " + moreLink(DocHtml.APP_DOMAIN));
				setHeaderHelp(text("model.measured"),
						text("model.measured.tip.n.compounds.one.model"));

				res.sortProperties(ListUtil.createList("Compound", text("model.measured"),
						"Prediction", "App-Domain"));

				addTable(res);
			}
		}

		return close();

	}
}
