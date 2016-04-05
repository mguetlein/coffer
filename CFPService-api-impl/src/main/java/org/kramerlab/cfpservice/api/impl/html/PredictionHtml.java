package org.kramerlab.cfpservice.api.impl.html;

import java.io.IOException;

import org.kramerlab.cfpservice.api.ModelService;
import org.kramerlab.cfpservice.api.impl.objects.AbstractModel;
import org.kramerlab.cfpservice.api.objects.Model;
import org.kramerlab.cfpservice.api.objects.Prediction;
import org.kramerlab.cfpservice.api.objects.SubgraphPredictionAttribute;
import org.mg.cdklib.CDKConverter;
import org.mg.cdklib.cfp.CFPFragment;
import org.mg.cdklib.cfp.CFPMiner;
import org.mg.javalib.datamining.ResultSet;
import org.mg.javalib.util.StringUtil;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.rendersnake.HtmlAttributesFactory;
import org.rendersnake.HtmlCanvas;
import org.rendersnake.Renderable;

public class PredictionHtml extends DefaultHtml
{
	Prediction p;
	CFPMiner miner;
	HideFragments hideFragments;
	int maxNumFragments;

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

	public PredictionHtml(Prediction p, HideFragments hideFragments, int maxNumFragments)
	{
		super("Prediction of compound " + p.getSmiles(), p.getModelId(),
				AbstractModel.getName(p.getModelId()), "prediction/" + p.getId(), "Prediction");
		setHidePageTitle(true);
		this.p = p;
		this.hideFragments = hideFragments;
		this.maxNumFragments = maxNumFragments;
		miner = ((AbstractModel) AbstractModel.find(p.getModelId())).getCFPMiner();
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
			final String smiles)
	{
		rep.setHeaderHelp("Info", text("compound.info.tip"));
		final String smi = StringUtil.urlEncodeUTF8(smiles);
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

	public String build()
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
			set.setResultValue(rIdx, "App-Domain", getInsideAppDomain(p));
			set.setResultValue(rIdx, " ", " ");

			String url = "/" + p.getModelId();
			Model m = AbstractModel.find(p.getModelId());
			set.setResultValue(rIdx, "Dataset", encodeLink(url, m.getName()));
			set.setResultValue(rIdx, "Target", encodeLink(url, m.getTarget()));
			set.setResultValue(rIdx, "Classifier", encodeLink(url, m.getClassifierName()));
			//			set.setResultValue(rIdx, "Features",
			//					encodeLink(url, miner.getNiceFragmentDescription()));

			setHeaderHelp("Prediction",
					text("model.prediction.tip") + " " + moreLink(DocHtml.CLASSIFIERS));
			setHeaderHelp("App-Domain",
					text("appdomain.help.general") + " " + moreLink(DocHtml.APP_DOMAIN));
			setHeaderHelp(text("model.measured"), text("model.measured.tip.single"));

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
			if (hideFragments != hide)
				h = encodeLink(p.getId() + "?hideFragments=" + hide.stringKey(), h);
			hideTxt += h + " ";
		}
		try
		{
			getHtml().div();//HtmlAttributesFactory.align("right"));
			getHtml().render(new TextWithLinks(hideTxt, true, false));
			getHtml().render(getMouseoverHelp(text("fragment.hide"), null));
			getHtml()._div();
		}
		catch (IOException e1)
		{
			throw new RuntimeException(e1);
		}
		addGap();

		for (final boolean match : new Boolean[] { true, false })
		{
			String fragmentCol = (match ? "PRESENT" : "ABSENT") + " Fragment";

			ResultSet set = new ResultSet();
			int fIdx = 0;
			for (final SubgraphPredictionAttribute pa : p.getPredictionAttributes())
			{
				if (hideFragments == HideFragments.SUPER && pa.hasSubGraph())
					continue;
				if (hideFragments == HideFragments.SUB && pa.hasSuperGraph())
					continue;

				int attIdx = pa.getAttribute();
				if (match == testInstanceContains(attIdx))
				{
					fIdx++;
					if (fIdx > maxNumFragments)
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
									+ moreLink(DocHtml.RANKING_FRAGMENTS);
						else
							txt = "The " + fragmentLink
									+ " is absent in the test compound. If present, it would have "
									+ (activating ? "an activating" : "a de-activating")
									+ " effect on the prediction:<br>" + "The " + alternativePredStr
									+ " " + moreLink(DocHtml.RANKING_FRAGMENTS);
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
											+ StringUtil.urlEncodeUTF8(p.getSmiles()),
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

			if (fIdx > maxNumFragments)
			{
				int rIdx = set.addResult();
				String hideSup = "";
				if (hideFragments != HIDE_FRAGMENTS_DEFAULT)
					hideSup = "hideFragments=" + hideFragments.stringKey() + "&";
				set.setResultValue(rIdx, fragmentCol,
						encodeLink(p.getId() + "?" + hideSup + "size="
								+ Math.min(maxNumFragments + ModelService.DEFAULT_NUM_ENTRIES, fIdx)
								+ "#" + (rIdx + 1), "More fragments"));
			}
			addTable(set);
		}
		stopColumns();
		return close();
	}

	private boolean testInstanceContains(int attIdx)
	{
		try
		{
			return miner.getFragmentsForTestCompound(p.getSmiles())
					.contains(miner.getFragmentViaIdx(attIdx));
		}
		catch (CDKException e)
		{
			throw new RuntimeException(e);
		}
	}

	private String depictMatch(int attIdx, boolean fallbackToTraining, Boolean activating,
			boolean crop)
	{
		try
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
		catch (CDKException e)
		{
			throw new RuntimeException(e);
		}
	}

}
