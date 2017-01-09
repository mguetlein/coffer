package org.kramerlab.coffer.api.impl.html;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.kramerlab.coffer.api.ModelService;
import org.kramerlab.coffer.api.impl.DepictService;
import org.kramerlab.coffer.api.impl.ModelServiceTasks;
import org.kramerlab.coffer.api.impl.objects.AbstractModel;
import org.kramerlab.coffer.api.objects.Model;
import org.kramerlab.coffer.api.objects.Prediction;
import org.kramerlab.coffer.api.objects.SubgraphPredictionAttribute;
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

		if (p.getPredictionAttributes() == null)
			setRefresh(5);
	}

	//	private Renderable getPrediction(boolean hideNonMax, int activeClassIdx)
	//	{
	//		return getPrediction(p, miner.getClassValues(), activeClassIdx, hideNonMax, null);
	//	}

	public static void setAdditionalInfo(HTMLReport rep, ResultSet tableSet, int rIdx,
			String smiles)
	{
		rep.setHeaderHelp("Info", text("compound.info.tip"));
		final String origSmiles = smiles;

		final String[] partitioned = smiles.split("\\.");
		final List<String> encoded = new ArrayList<>();
		for (String s : partitioned)
			encoded.add(StringUtil.urlEncodeUTF8(s));

		//		System.out.println(smi);
		tableSet.setResultValue(rIdx, "Info", new Renderable()
		{
			public void renderOn(HtmlCanvas html) throws IOException
			{
				html.div(HtmlAttributesFactory.class_("small"));
				html.write("Smiles: " + origSmiles);
				html._div();

				// due to lack of space, fetch info only for first connected compound in mixture
				int i = 0;
				if (partitioned.length > 1)
				{
					html.br();
					html.div(HtmlAttributesFactory.class_("small"));
					html.write("#" + (i + 1) + ": " + partitioned[i]);
					html._div();
				}
				html.object(HtmlAttributesFactory.data("/info/all/" + encoded.get(i)))._object();//.width("300")
			}
		});
	}

	public String build()
	{
		final Model m = AbstractModel.find(p.getModelId());

		newSection("Predicted compound");
		{
			ResultSet set = new ResultSet();
			int rIdx = set.addResult();
			//			set.setResultValue(rIdx, "Smiles", p.getSmiles());

			if (p.getPredictionAttributes() != null)
			{
				set.setResultValue(rIdx, "Structure",
						getImage(depictMultiMatch(p.getSmiles(), p.getModelId(), maxMolSizeLarge),
								depictMultiMatch(p.getSmiles(), p.getModelId(), -1), false));
				setHeaderHelp("Structure",
						text("fragment.coloringQueryCompound.tip", DepictService.ACTIVE_AS_TEXT,
								DepictService.INACTIVE_AS_TEXT) + " "
								+ moreLink(DocHtml.COLORING_QUERY_COMPOUND));
			}
			else
				set.setResultValue(rIdx, "Structure", getImage(
						depict(p.getSmiles(), maxMolSizeLarge), depict(p.getSmiles(), -1), false));
			setAdditionalInfo(this, set, rIdx, p.getSmiles());

			setTableRowsAlternating(false);
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

			set.setResultValue(rIdx, "Prediction", getPrediction(p, m, null));
			if (ModelService.APP_DOMAIN_VISIBLE)
				set.setResultValue(rIdx, "App-Domain", getInsideAppDomain(p, null));
			set.setResultValue(rIdx, " ", " ");

			String url = "/" + p.getModelId();
			//			set.setResultValue(rIdx, "Dataset", encodeLink(url, m.getName()));
			//			set.setResultValue(rIdx, "Target", encodeLink(url, m.getTarget()));

			set.setResultValue(rIdx, "Target", doubleText(m.getTarget(), m.getName(), url, true));

			//			String name = m.getClassifierName().replaceAll(" \\(.*", "");
			//			set.setResultValue(rIdx, "Classifier", encodeLink(url, name));
			//			set.setResultValue(rIdx, "Features",
			//					encodeLink(url, miner.getNiceFragmentDescription()));

			setHeaderHelp("Prediction",
					text("model.prediction.tip") + " " + moreLink(DocHtml.CLASSIFIERS));
			setHeaderHelp("App-Domain",
					AppDomainHtml.getGeneralInfo() + " " + moreLink(DocHtml.APP_DOMAIN));
			setHeaderHelp(text("model.measured"),
					text("model.measured.tip.one.compound.one.model"));

			setTableRowsAlternating(false);
			addTable(set);//, true);
			setTableRowsAlternating(true);
		}
		addGap();
		//addGap();
		//		stopInlineTables();

		//addParagraph("The compound ");

		if (p.getPredictionAttributes() == null)
		{
			newSection("Fragments");

			Runnable r = new Runnable()
			{
				@Override
				public void run()
				{
					p.computePredictionAttributesComputed();
				}
			};
			ModelServiceTasks.addTask("compute fragments for " + p.getLocalURI(), r);

			startLeftColumn();
			addImage("/img/wait.gif");
			startRightColumn();
			addGap();
			addParagraph("Computing fragments, this page reloads every 5 seconds.");
			stopColumns();
		}
		else
		{
			String hideTxt = "";
			for (HideFragments hide : HideFragments.values())
			{
				String h = text("fragment.hide." + hide + ".link");
				if (hideFragments != hide)
					h = encodeLink(p.getId() + "?hideFragments=" + hide.stringKey(), h);
				hideTxt += h + " ";
			}
			final String fHideTxt = hideTxt;

			newSection("Fragments", new Renderable()
			{
				@Override
				public void renderOn(HtmlCanvas html) throws IOException
				{
					//html.div();//HtmlAttributesFactory.align("right"));
					html.write("&nbsp;&nbsp;", false);
					html.render(new TextWithLinks(fHideTxt, true, false));
					html.render(getMouseoverHelp(text("fragment.hide"), null));
					//html._div();
				}
			});
			//			addGap();

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
											p.getPredictedDistribution()[miner.getActiveIdx()]
													* 100)
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
										+ " effect on the prediction:<br>" + "The "
										+ alternativePredStr + " "
										+ moreLink(DocHtml.RANKING_FRAGMENTS);
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
								getImage(depictMatch(attIdx, activating),
										"/" + p.getModelId() + "/fragment/" + (attIdx + 1)
												+ "?smiles="
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
								double iconValue = 0.5;
								if (activating != null)
								{
									if (activating)
										effectStr = "activating";
									else
										effectStr = "de-activating";

									if (activating)
										iconValue += pa.getDiffToOrigProp() * 0.5;
									else
										iconValue -= pa.getDiffToOrigProp() * 0.5;
								}
								html.write(effectStr);
								html.write(" ");
								//.renderOn(html);
								if (activating != null)
									html.render(getMouseoverHelp(txt, " ",
											getImage("/depictActiveIcon?drawHelp=true&probability="
													+ iconValue)));
								html.div(HtmlAttributesFactory.class_("smallGrey"));
								if (match)
									html.write("Prediction if absent:");
								else
									html.write("Prediction if present:");
								html.br();
								html.render(getPrediction(pa, m));
								html._div();
							}
						});
					}
				}
				//			addParagraph((match ? "Matching" : "Not matching") + " attributes");
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
					set.setResultValue(rIdx, fragmentCol, encodeLink(p.getId() + "?" + hideSup
							+ "size="
							+ Math.min(maxNumFragments + ModelService.DEFAULT_NUM_ENTRIES, fIdx)
							+ "#" + (rIdx + 1), "More fragments"));
				}
				addTable(set);
			}
			stopColumns();
		}
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

	private String depictMatch(int attIdx, Boolean activating)
	{
		try
		{
			String m = p.getSmiles();
			if (!testInstanceContains(attIdx))
				//		if (testInstance.stringValue(attr).equals("0"))
				m = miner.getTrainingDataSmiles()
						.get(miner.getCompoundsForFragment(miner.getFragmentViaIdx(attIdx))
								.iterator().next());
			if (miner.getAtoms(m, miner.getFragmentViaIdx(attIdx)) == null)
				throw new IllegalStateException("no atoms in " + m + " for att-idx " + attIdx
						+ ", hashcode: " + miner.getFragmentViaIdx(attIdx));
			return depictMatch(m, miner.getAtoms(m, miner.getFragmentViaIdx(attIdx)),
					miner.getCFPType().isECFP(), activating, true, croppedPicSize);
		}
		catch (CDKException e)
		{
			throw new RuntimeException(e);
		}
	}

}
