package org.kramerlab.cfpservice.api.impl;

import org.kramerlab.extendedrandomforests.html.ExtendedHtmlReport;
import org.mg.htmlreporting.HTMLReport;
import org.mg.javalib.util.ArrayUtil;

public class CFPServiceConfig
{
	private static String footer = "<div></a>Contact: <a href='http://www.informatik.uni-mainz.de/groups/information-systems/people-infosys/martin.guetlein'>Martin G&uuml;tlein</a><br><a href='http://infosys.informatik.uni-mainz.de'><img src=\"/img/jgu.png\" /></div>";
	private static String title = "Unfolded Circular Fingerprints";
	private static String header = "<a class='a_header' href=\"/\"><h1>Unfolded Circular Fingerprints</h1></a>";
	private static String css = "/css/styles.css";

	public static void initReport(HTMLReport report)
	{
		initReport(report, null, null, null, null);
	}

	public static void initModelReport(HTMLReport report, String modelId)
	{
		initReport(report, modelId, modelId, null, null);
	}

	public static void initPredictionReport(HTMLReport report, String predictionId)
	{
		initReport(report, predictionId, "Prediction", null, null);
	}

	public static void initPredictionReport(HTMLReport report, String modelId, String predictionId)
	{
		initReport(report, modelId, modelId, predictionId, "Prediction");
	}

	public static void initFragmentReport(HTMLReport report, String modelId, String fragmentId)
	{
		initReport(report, modelId, modelId, fragmentId, "Fragment");
	}

	private static void initReport(HTMLReport report, String id, String name, String subId, String subName)
	{
		if (report instanceof ExtendedHtmlReport)
			((ExtendedHtmlReport) report).setImageProvider(new DepictServiceImpl());
		report.setTitles(title, header, css, footer);

		if (id != null)
		{
			String[] urls = new String[] { "/", "/" + id };
			String[] names = new String[] { "Home", name };
			if (subId != null)
			{
				urls = ArrayUtil.push(urls, "/" + id + "/" + subId);
				names = ArrayUtil.push(names, subName);
			}
			report.setBreadCrumps(urls, names);
		}
	}
}
