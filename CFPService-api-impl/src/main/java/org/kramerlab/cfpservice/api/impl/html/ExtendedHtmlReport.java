package org.kramerlab.cfpservice.api.impl.html;

import java.util.ResourceBundle;

import org.kramerlab.cfpservice.api.impl.DepictServiceImpl;
import org.mg.htmlreporting.HTMLReport;
import org.mg.javalib.util.ArrayUtil;

public class ExtendedHtmlReport extends HTMLReport
{
	private String footer()
	{
		return "<div><a href='/'>Home</a> - "
				+ "<a href='/doc'>Documentation</a> - "
				+ "</a><a href='http://www.informatik.uni-mainz.de/groups/information-systems/people-infosys/martin.guetlein'>Contact: Martin G&uuml;tlein</a>"
				+ "</div><div><a href='http://infosys.informatik.uni-mainz.de'><img src=\"/img/jgu.png\" /></div>";
	}

	private static String serviceTitle = "Unfolded Circular Fingerprints";
	private static String serviceHeader = "<a class='a_header' href=\"/\"><h1>Unfolded Circular Fingerprints</h1></a>";
	public static String css = "/css/styles.css";

	protected int molPicSize = 200;
	protected int croppedPicSize = 150;
	protected ImageProvider imageProvider = new DepictServiceImpl();

	public ExtendedHtmlReport(String id, String name, String subId, String subName)
	{
		this(null, id, name, subId, subName);
	}

	public ExtendedHtmlReport(String title, String id, String name, String subId, String subName)
	{
		super(title);
		setTitles(serviceTitle, serviceHeader, css, footer());
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

	private static ResourceBundle bundle;

	private static ResourceBundle bundle()
	{
		if (bundle == null)
			bundle = ResourceBundle.getBundle("cfpservice");
		return bundle;
	}

	public static String text(String key)
	{
		return bundle().getString(key);
	}

	public static String moreLink(String docSection)
	{
		return HTMLReport.encodeLink("/doc#" + DocHtml.getAnker(docSection), "more..");
	}

}
