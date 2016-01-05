package org.kramerlab.cfpservice.api.impl.html;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.kramerlab.cfpservice.api.impl.Model;
import org.kramerlab.cfpservice.api.impl.Prediction;
import org.mg.cdklib.cfp.CFPMiner;
import org.mg.htmlreporting.HTMLReport;
import org.mg.javalib.datamining.ResultSet;
import org.mg.javalib.util.StringUtil;
import org.mg.wekalib.attribute_ranking.PredictionAttribute;
import org.rendersnake.HtmlAttributesFactory;
import org.rendersnake.HtmlCanvas;
import org.rendersnake.Renderable;

public class PredictionHtml extends DefaultHtml
{
	Prediction p;
	CFPMiner miner;

	public PredictionHtml(Prediction p, String maxNumFragments)
	{
		super("Prediction of compound " + p.getSmiles(), p.getModelId(), Model.getName(p
				.getModelId()), "prediction/" + p.getId(), "Prediction");
		setHidePageTitle(true);
		this.p = p;
		miner = Model.find(p.getModelId()).getCFPMiner();
		parseMaxNumElements(maxNumFragments);
	}

	private Renderable getPrediction(boolean hideNonMax)
	{
		return getPrediction(p, miner.getClassValues(), hideNonMax, null);
	}

	private Renderable getPrediction(double dist[], int predIdx, boolean hideNonMax)
	{
		return getPrediction(dist, miner.getClassValues(), predIdx, hideNonMax, null);
	}

	public static Renderable getPrediction(Prediction p, String classValues[], boolean hideNonMax,
			String url)
	{
		return getPrediction(p.getPredictedDistribution(), classValues, p.getPredictedIdx(),
				hideNonMax, url);
	}

	private static Renderable getPrediction(final double dist[], final String classValues[],
			final int predIdx, final boolean hideNonMax, final String url)
	{
		return new Renderable()
		{
			public void renderOn(HtmlCanvas html) throws IOException
			{
				if (url != null)
					html.a(HtmlAttributesFactory.href(url));

				boolean hide = hideNonMax && dist[predIdx] > (1 / (double) dist.length);
				for (int i = 0; i < dist.length; i++)
				{
					if (i != predIdx && hide)
						html.div(HtmlAttributesFactory.class_("smallGrey"));
					html.write(classValues[i] + " (" + StringUtil.formatDouble(dist[i] * 100)
							+ "%)");
					if (i != predIdx && hide)
						html._div();
					else if (i < dist.length - 1)
						html.br();
				}
				if (url != null)
					html._a();
			}
		};
	}

	public static void setAdditionalInfo(HTMLReport rep, ResultSet tableSet, int rIdx,
			final String smiles) throws UnsupportedEncodingException
	{
		rep.setHeaderHelp("Info", "Looking up the compound smiles in PubChem and ChEMBL.");
		final String smi = URLEncoder.encode(smiles, "UTF-8");
		//		System.out.println(smi);
		tableSet.setResultValue(rIdx, "Info", new Renderable()
		{
			public void renderOn(HtmlCanvas html) throws IOException
			{
				html.div(HtmlAttributesFactory.class_("small"));
				html.write("Smiles: " + smiles);
				html._div();

				html.object(HtmlAttributesFactory.data("/info/all/" + smi))._object();//.width("300")
				//html.object(HtmlAttributesFactory.data("/info/pubchem/" + smi).width("300"))._object();
				//html.object(HtmlAttributesFactory.data("/info/chembl/" + smi).width("300"))._object();
			}
		});
	}

	public String build() throws Exception
	{
		newSection("Predicted compound");
		{
			ResultSet set = new ResultSet();
			int rIdx = set.addResult();
			//			set.setResultValue(rIdx, "Smiles", p.getSmiles());
			set.setResultValue(
					rIdx,
					"Structure",
					getImage(
							depictMultiMatch(p.getSmiles(), p.getId(), p.getModelId(),
									maxMolPicSize),
							depictMultiMatch(p.getSmiles(), p.getId(), p.getModelId(), -1), false));
			setAdditionalInfo(this, set, rIdx, p.getSmiles());

			setTableRowsAlternating(false);
			setTableColWidthLimited(true);
			addTable(set, false);
			setTableRowsAlternating(true);
		}
		addGap();

		newSection("Prediction");
		{
			ResultSet set = new ResultSet();
			int rIdx = set.addResult();

			String endpoint = p.getTrainingActivity();
			if (endpoint != null)
				set.setResultValue(rIdx, "Activity", endpoint);

			set.setResultValue(rIdx, "Prediction", getPrediction(true));
			set.setResultValue(rIdx, " ", " ");

			String url = "/" + p.getModelId();
			Model m = Model.find(p.getModelId());
			set.setResultValue(rIdx, "Dataset", encodeLink(url, m.getName()));
			set.setResultValue(rIdx, "Target", encodeLink(url, m.getTarget()));
			set.setResultValue(
					rIdx,
					"Classifier",
					encodeLink(url, text("classifier."
							+ m.getClassifier().getClass().getSimpleName())));
			set.setResultValue(rIdx, "Fragments", encodeLink(url, m.getCFPMiner().getFeatureType()));

			setHeaderHelp("Prediction", text("model.prediction.tip") + " "
					+ moreLink(DocHtml.CLASSIFIERS));
			setHeaderHelp("Activity", text("model.activity.tip"));

			setTableRowsAlternating(false);
			setTableColWidthLimited(false);
			addTable(set);//, true);
			setTableRowsAlternating(true);
		}
		addGap();
		//		stopInlineTables();

		//addParagraph("The compound ");

		//		addGap();

		for (final boolean match : new Boolean[] { true, false })
		{
			ResultSet set = new ResultSet();
			int fIdx = 0;
			for (final PredictionAttribute pa : p.getPredictionAttributes())
			{
				int attIdx = pa.getAttribute();
				if (match == testInstanceContains(attIdx))
				{
					fIdx++;
					if (fIdx > maxNumElements)
						continue;

					int rIdx = set.addResult();
					set.setResultValue(rIdx, "No.", fIdx + "");

					Boolean moreActive = null;
					final String txt;
					final Boolean activating;

					if (pa.getAlternativeDistributionForInstance()[miner.getActiveIdx()] != p
							.getPredictedDistribution()[miner.getActiveIdx()])
					{
						moreActive = pa.getAlternativeDistributionForInstance()[miner
								.getActiveIdx()] > p.getPredictedDistribution()[miner
								.getActiveIdx()];
						if (match)
							activating = !moreActive;
						else
							activating = moreActive;

						String fragmentLink = "fragment";
						// HTMLReport.encodeLink(imageProvider.hrefFragment(p.getModelId(), attIdx),"fragment");
						String alternativePredStr = "compound would be predicted as active with "
								+ (moreActive ? "increased" : "decreased")
								+ " probability ("
								+ //
								StringUtil
										.formatDouble(pa.getAlternativeDistributionForInstance()[miner
												.getActiveIdx()] * 100)
								+ "% instead of "
								+ StringUtil.formatDouble(p.getPredictedDistribution()[miner
										.getActiveIdx()] * 100) + "%).";

						if (match)
							txt = "The " + fragmentLink
									+ " is present in the test compound, it has "
									+ (activating ? "an activating" : "a de-activating")
									+ " effect on the prediction:<br>" + "If absent, the "
									+ alternativePredStr + " "
									+ moreLink(DocHtml.PREDICTION_FRAGMENTS);
						else
							txt = "The " + fragmentLink
									+ " is absent in the test compound. If present, it would have "
									+ (activating ? "an activating" : "a de-activating")
									+ " effect on the prediction:<br>" + "The "
									+ alternativePredStr + " "
									+ moreLink(DocHtml.PREDICTION_FRAGMENTS);
					}
					else
					{
						txt = null;
						activating = null;
					}

					set.setResultValue(
							rIdx,
							"Fragment",
							getImage(depictMatch(attIdx, true, activating, true),
									"/" + p.getModelId() + "/fragment/" + (attIdx + 1), true));
					//					set.setResultValue(rIdx, "Value", renderer.renderAttributeValue(att, attIdx));

					set.setResultValue(rIdx, "Effect", new Renderable()
					{
						public void renderOn(HtmlCanvas html) throws IOException
						{
							String effectStr = "none";
							if (activating != null)
							{
								if (activating)
									effectStr = "activating";
								else
									effectStr = "de-activating";
							}
							html.write(effectStr);
							if (activating != null)
								html.render(getMouseoverHelp(txt, " "));
							html.div(HtmlAttributesFactory.class_("smallGrey"));
							if (match)
								html.write("Prediction if absent:");
							else
								html.write("Prediction if present:");
							html.br();
							html.render(getPrediction(pa.getAlternativeDistributionForInstance(),
									pa.getAlternativePredictionIdx(), false));
							html._div();
						}
					});
				}
			}
			//			addParagraph((match ? "Matching" : "Not matching") + " attributes");
			setTableColWidthLimited(true);

			if (match)
				startLeftColumn();
			else
				startRightColumn();
			newSection((match ? "Present" : "Absent") + " fragments");

			if (fIdx > maxNumElements)
			{
				int rIdx = set.addResult();
				set.setResultValue(
						rIdx,
						"Fragment",
						encodeLink(
								p.getId() + "?size="
										+ Math.min(maxNumElements + defaultMaxNumElements, fIdx)
										+ "#" + (rIdx + 1), "More fragments"));
			}
			addTable(set);
		}
		stopColumns();
		return close();
	}

	private boolean testInstanceContains(int attIdx) throws Exception
	{
		return miner.getFragmentsForTestCompound(p.getSmiles()).contains(
				miner.getFragmentViaIdx(attIdx));
	}

	private String depictMatch(int attIdx, boolean fallbackToTraining, Boolean activating,
			boolean crop) throws Exception
	{
		String m = p.getSmiles();
		if (!testInstanceContains(attIdx))
			//		if (testInstance.stringValue(attr).equals("0"))
			if (fallbackToTraining)
				m = miner.getTrainingDataSmiles().get(
						miner.getCompoundsForFragment(miner.getFragmentViaIdx(attIdx)).iterator()
								.next());
			else
				crop = false;
		if (miner.getAtoms(m, miner.getFragmentViaIdx(attIdx)) == null)
			throw new IllegalStateException("no atoms in " + m + " for att-idx " + attIdx
					+ ", hashcode: " + miner.getFragmentViaIdx(attIdx));
		return depictMatch(m, miner.getAtoms(m, miner.getFragmentViaIdx(attIdx)), miner
				.getCFPType().isECFP(), activating, crop, crop ? croppedPicSize : maxMolPicSize);
	}

}
