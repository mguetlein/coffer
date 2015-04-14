package org.kramerlab.cfpservice.api.impl.html;

import org.kramerlab.cfpservice.api.impl.DepictServiceImpl;
import org.mg.htmlreporting.HTMLReport;
import org.mg.javalib.util.ArrayUtil;

public class ExtendedHtmlReport extends HTMLReport
{
	private static String serviceFooter = "<div></a>Contact: <a href='http://www.informatik.uni-mainz.de/groups/information-systems/people-infosys/martin.guetlein'>Martin G&uuml;tlein</a><br><a href='http://infosys.informatik.uni-mainz.de'><img src=\"/img/jgu.png\" /></div>";
	private static String serviceTitle = "Unfolded Circular Fingerprints";
	private static String serviceHeader = "<a class='a_header' href=\"/\"><h1>Unfolded Circular Fingerprints</h1></a>";
	public static String css = "/css/styles.css";

	protected static int molPicSize = 200;
	protected static int croppedPicSize = 150;
	protected ImageProvider imageProvider = new DepictServiceImpl();

	public ExtendedHtmlReport(String id, String name, String subId, String subName)
	{
		this(null, id, name, subId, subName);
	}

	public ExtendedHtmlReport(String title, String id, String name, String subId, String subName)
	{
		super(title);
		setTitles(serviceTitle, serviceHeader, css, serviceFooter);
		if (id != null)
		{
			String[] urls = new String[] { "/", "/" + id };
			String[] names = new String[] { "Home", name };
			if (subId != null)
			{
				urls = ArrayUtil.push(urls, "/" + id + "/" + subId);
				names = ArrayUtil.push(names, subName);
			}
			setBreadCrumps(urls, names);
		}
		setHelpImg("/img/help14.png");
		setExternalLinkImg("/img/iconExternalLink.gif");
	}

}
