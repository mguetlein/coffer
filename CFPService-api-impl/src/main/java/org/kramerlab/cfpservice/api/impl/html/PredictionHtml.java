package org.kramerlab.cfpservice.api.impl.html;

import org.kramerlab.cfpminer.CFPMiner;
import org.kramerlab.cfpservice.api.impl.Model;
import org.kramerlab.cfpservice.api.impl.Prediction;
import org.kramerlab.extendedrandomforests.weka.PredictionAttribute;
import org.mg.htmlreporting.HTMLReport;
import org.mg.javalib.datamining.ResultSet;
import org.mg.javalib.util.StringUtil;

public class PredictionHtml extends ExtendedHtmlReport
{
	Prediction p;
	CFPMiner miner;

	public PredictionHtml(Prediction p)
	{
		super("Prediction of compound " + p.getSmiles(), p.getModelId(), p.getModelId(), p.getId(), "Prediction");
		this.p = p;
		miner = Model.find(p.getModelId()).getCFPMiner();
	}

	private String getPredictionString(boolean hideNonMax)
	{
		return getPredictionString(p, miner.getClassValues(), hideNonMax, null);
	}

	private String getPredictionString(double dist[], int predIdx, boolean hideNonMax)
	{
		return getPredictionString(dist, miner.getClassValues(), predIdx, hideNonMax, null);
	}

	public static String getPredictionString(Prediction p, String classValues[], boolean hideNonMax, String url)
	{
		return getPredictionString(p.getPredictedDistribution(), classValues, p.getPredictedIdx(), hideNonMax, url);
	}

	private static String getPredictionString(double dist[], String classValues[], int predIdx, boolean hideNonMax,
			String url)
	{
		boolean hide = hideNonMax && dist[predIdx] > (1 / (double) dist.length);
		StringBuffer s = new StringBuffer();
		for (int i = 0; i < dist.length; i++)
		{
			if (i > 0)
				s.append("<br>");
			if (i != predIdx && hide)
				s.append("<small><font color=grey>");
			s.append(classValues[i]);
			s.append(" (");
			s.append(StringUtil.formatDouble(dist[i] * 100));
			s.append("%)");
			if (i != predIdx && hide)
				s.append("</font></small>");
		}
		if (url == null)
			return s.toString();
		else
			return "<a href=\"" + url + "\">" + s.toString() + "</a>";
	}

	public String build() throws Exception
	{
		ResultSet set = new ResultSet();
		int rIdx = set.addResult();
		set.setResultValue(
				rIdx,
				"Test compound",
				getImage(imageProvider.drawCompound(p.getSmiles(), molPicSize),
						imageProvider.hrefCompound(p.getSmiles()), false));

		String predStr = getPredictionString(true);
		set.setResultValue(rIdx, "Predicted class", HTMLReport.getHTMLCode(predStr));
		addTable(set);

		addGap();
		newSubsection("Prediction of model " + p.getModelId() + " is based on the following fragments:");

		startInlinesTables();
		for (boolean match : new Boolean[] { true, false })
		{
			set = new ResultSet();
			for (PredictionAttribute pa : p.getPredictionAttributes())
			{
				int attIdx = pa.attribute;
				if (match == testInstanceContains(attIdx))
				{
					rIdx = set.addResult();
					set.setResultValue(
							rIdx,
							(match ? "Present" : "Absent") + " fragments",
							getImage(getFragmentPicInTestInstance(attIdx, true, true),
									imageProvider.hrefFragment(p.getModelId(), attIdx), true));
					//					set.setResultValue(rIdx, "Value", renderer.renderAttributeValue(att, attIdx));

					Boolean activating = null;
					if (pa.alternativeDistributionForInstance[miner.getActiveIdx()] != p.getPredictedDistribution()[miner
							.getActiveIdx()])
					{
						activating = pa.alternativeDistributionForInstance[miner.getActiveIdx()] > p
								.getPredictedDistribution()[miner.getActiveIdx()];
						if (match)
							activating = !activating;
					}

					String effectStr = "none";
					if (activating != null)
					{
						if (activating)
							effectStr = "activating";
						else
							effectStr = "de-activating";
					}
					effectStr += "<font color=grey><small>";
					if (match)
						effectStr += "<br>Prediction if absent:<br>";
					else
						effectStr += "<br>Prediction if present:<br>";
					effectStr += getPredictionString(pa.alternativeDistributionForInstance,
							pa.alternativePredictionIdx, false);
					effectStr += "</small></font>";
					set.setResultValue(rIdx, "Effect", HTMLReport.getHTMLCode(effectStr));
				}
			}
			//			addParagraph((match ? "Matching" : "Not matching") + " attributes");
			addTable(set);
		}
		stopInlineTables();
		return close();
	}

	private boolean testInstanceContains(int attIdx) throws Exception
	{
		return miner.getHashcodesForTestCompound(p.getSmiles()).contains(miner.getHashcodeViaIdx(attIdx));
	}

	private String getFragmentPicInTestInstance(int attIdx, boolean fallbackToTraining, boolean crop) throws Exception
	{
		String m = p.getSmiles();
		if (!testInstanceContains(attIdx))
			//		if (testInstance.stringValue(attr).equals("0"))
			if (fallbackToTraining)
				m = miner.getTrainingDataSmiles().get(
						miner.getCompoundsForHashcode(miner.getHashcodeViaIdx(attIdx)).iterator().next());
			else
				crop = false;
		if (miner.getAtoms(m, miner.getHashcodeViaIdx(attIdx)) == null)
			throw new IllegalStateException("no atoms in " + m + " for att-idx " + attIdx + ", hashcode: "
					+ miner.getHashcodeViaIdx(attIdx));
		return imageProvider.drawCompoundWithFP(m, miner.getAtoms(m, miner.getHashcodeViaIdx(attIdx)), crop,
				crop ? croppedPicSize : molPicSize);
	}

}
