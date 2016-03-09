package org.kramerlab.cfpservice.api.impl.html;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.kramerlab.cfpservice.api.ModelService;
import org.kramerlab.cfpservice.api.impl.Model;
import org.kramerlab.cfpservice.api.impl.Prediction;
import org.kramerlab.cfpservice.api.impl.SubgraphPredictionAttribute;
import org.mg.cdklib.CDKConverter;
import org.mg.cdklib.cfp.CFPFragment;
import org.mg.cdklib.cfp.CFPMiner;
import org.mg.javalib.datamining.ResultSet;
import org.mg.javalib.util.StringUtil;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.rendersnake.HtmlAttributesFactory;
import org.rendersnake.HtmlCanvas;
import org.rendersnake.Renderable;

public class PredictionHtml extends DefaultHtml
{
	Prediction p;
	CFPMiner miner;

	public static enum HideFragments
	{
		SUPER, NONE, SUB;

		public String stringKey()
		{
			switch (this)
			{
				case NONE:
					return ModelService.HIDE_NO_FRAGMENTS;
				case SUB:
					return ModelService.HIDE_SUB_FRAGMENTS;
				default:
					return ModelService.HIDE_SUPER_FRAGMENTS;
			}
		}

		public static HideFragments fromString(String s)
		{
			if (s == null || s.length() == 0)
				return HIDE_FRAGMENTS_DEFAULT;
			if (s.equals(ModelService.HIDE_NO_FRAGMENTS))
				return NONE;
			else if (s.equals(ModelService.HIDE_SUB_FRAGMENTS))
				return SUB;
			else if (s.equals(ModelService.HIDE_SUPER_FRAGMENTS))
				return SUPER;
			else
				throw new IllegalArgumentException("param for hiding fragments invalid: " + s);
		}
	}

	public static HideFragments HIDE_FRAGMENTS_DEFAULT = HideFragments.NONE;

	public PredictionHtml(Prediction p)
	{
		super("Prediction of compound " + p.getSmiles(), p.getModelId(),
				Model.getName(p.getModelId()), "prediction/" + p.getId(), "Prediction");
		setHidePageTitle(true);
		this.p = p;
		miner = Model.find(p.getModelId()).getCFPMiner();
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
					html.write(
							classValues[i] + " (" + StringUtil.formatDouble(dist[i] * 100) + "%)");
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
			set.setResultValue(rIdx, "Structure",
					getImage(depictMultiMatch(p.getSmiles(), p.getModelId(), maxMolPicSize),
							depictMultiMatch(p.getSmiles(), p.getModelId(), -1), false));
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
				set.setResultValue(rIdx, text("model.measured"), endpoint);

			set.setResultValue(rIdx, "Prediction", getPrediction(true));
			set.setResultValue(rIdx, " ", " ");

			String url = "/" + p.getModelId();
			Model m = Model.find(p.getModelId());
			set.setResultValue(rIdx, "Dataset", encodeLink(url, m.getName()));
			set.setResultValue(rIdx, "Target", encodeLink(url, m.getTarget()));
			set.setResultValue(rIdx, "Classifier", encodeLink(url, m.getClassifierName()));
			set.setResultValue(rIdx, "Fragments", encodeLink(url, miner.getFeatureType()));

			setHeaderHelp("Prediction",
					text("model.prediction.tip") + " " + moreLink(DocHtml.CLASSIFIERS));
			setHeaderHelp(text("model.measured"), text("model.measured.tip"));

			setTableRowsAlternating(false);
			setTableColWidthLimited(false);
			addTable(set);//, true);
			setTableRowsAlternating(true);
		}
		addGap();
		//addGap();
		//		stopInlineTables();

		//addParagraph("The compound ");

		//		addGap();

		newSection("Fragments", true);

		String hideTxt = "";
		for (HideFragments hide : HideFragments.values())
		{
			String h = text("fragment.hide." + hide + ".link");
			if (p.getHideFragments() != hide)
				h = encodeLink(p.getId() + "?hideFragments=" + hide.stringKey(), h);
			hideTxt += h + " ";
		}
		getHtml().div();//HtmlAttributesFactory.align("right"));
		getHtml().render(new TextWithLinks(hideTxt, true));
		getHtml().render(getMouseoverHelp(text("fragment.hide"), null));
		getHtml()._div();
		addGap();

		for (final boolean match : new Boolean[] { true, false })
		{
			String fragmentCol = (match ? "PRESENT" : "ABSENT") + " Fragment";

			ResultSet set = new ResultSet();
			int fIdx = 0;
			for (final SubgraphPredictionAttribute pa : p.getPredictionAttributes())
			{
				if (p.getHideFragments() == HideFragments.SUPER && pa.hasSubGraph)
					continue;
				if (p.getHideFragments() == HideFragments.SUB && pa.hasSuperGraph)
					continue;

				int attIdx = pa.getAttribute();
				if (match == testInstanceContains(attIdx))
				{
					fIdx++;
					if (fIdx > p.getMaxNumFragments())
						continue;

					int rIdx = set.addResult();
					set.setResultValue(rIdx, "", fIdx + "");

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
								+ (moreActive ? "increased" : "decreased") + " probability (" + //
								StringUtil.formatDouble(
										pa.getAlternativeDistributionForInstance()[miner
												.getActiveIdx()] * 100)
								+ "% instead of "
								+ StringUtil.formatDouble(
										p.getPredictedDistribution()[miner.getActiveIdx()] * 100)
								+ "%).";

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
									+ " effect on the prediction:<br>" + "The " + alternativePredStr
									+ " " + moreLink(DocHtml.PREDICTION_FRAGMENTS);
					}
					else
					{
						txt = null;
						activating = null;
					}

					if (match)
					{
						try
						{
							IAtomContainer mol = CDKConverter.parseSmiles(p.getSmiles());
							CFPFragment frag = miner.getFragmentViaIdx(pa.getAttribute());
							int numMatches = miner.getAtomsMultipleDistinct(mol, frag).size();
							if (numMatches == 0)
								throw new IllegalStateException();
							set.setResultValue(rIdx, "#",
									(numMatches > 1) ? (numMatches + "x") : "");
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}

					set.setResultValue(rIdx, fragmentCol,
							getImage(depictMatch(attIdx, true, activating, true),
									"/" + p.getModelId() + "/fragment/" + (attIdx + 1) + "?smiles="
											+ URLEncoder.encode(p.getSmiles(), "UTF8"),
									true));
									//					set.setResultValue(rIdx, "Value", renderer.renderAttributeValue(att, attIdx));

					//					String hideTxt = "";
					//					hideTxt = text("fragment.hide." + hideFragments);
					//					for (HideFragments hide : HideFragments.values())
					//						if (hideFragments != hide)
					//							hideTxt += " "
					//									+ encodeLink(p.getId() + "?hideFragments=" + hide.stringKey(),
					//											text("fragment.hide." + hide + ".link"));
					//					setHeaderHelp("Fragment", hideTxt);

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
			//getHtml().br();//hr(HtmlAttributesFactory.style("height:1pt; visibility:hidden;"));

			if (fIdx > p.getMaxNumFragments())
			{
				int rIdx = set.addResult();
				String hideSup = "";
				if (p.getHideFragments() != HIDE_FRAGMENTS_DEFAULT)
					hideSup = "hideFragments=" + p.getHideFragments().stringKey() + "&";
				set.setResultValue(rIdx, fragmentCol,
						encodeLink(
								p.getId() + "?" + hideSup + "size="
										+ Math.min(p.getMaxNumFragments()
												+ ModelService.DEFAULT_NUM_ENTRIES, fIdx)
										+ "#" + (rIdx + 1),
								"More fragments"));
			}
			addTable(set);
		}
		stopColumns();
		return close();
	}

	private boolean testInstanceContains(int attIdx) throws Exception
	{
		return miner.getFragmentsForTestCompound(p.getSmiles())
				.contains(miner.getFragmentViaIdx(attIdx));
	}

	private String depictMatch(int attIdx, boolean fallbackToTraining, Boolean activating,
			boolean crop) throws Exception
	{
		String m = p.getSmiles();
		if (!testInstanceContains(attIdx))
			//		if (testInstance.stringValue(attr).equals("0"))
			if (fallbackToTraining)
				m = miner.getTrainingDataSmiles()
						.get(miner.getCompoundsForFragment(miner.getFragmentViaIdx(attIdx))
								.iterator().next());
			else
				crop = false;
		if (miner.getAtoms(m, miner.getFragmentViaIdx(attIdx)) == null)
			throw new IllegalStateException("no atoms in " + m + " for att-idx " + attIdx
					+ ", hashcode: " + miner.getFragmentViaIdx(attIdx));
		return depictMatch(m, miner.getAtoms(m, miner.getFragmentViaIdx(attIdx)),
				miner.getCFPType().isECFP(), activating, crop,
				crop ? croppedPicSize : maxMolPicSize);
	}

}
