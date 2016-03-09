package org.kramerlab.cfpservice.api.impl.html;

import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.kramerlab.cfpservice.api.ModelService;
import org.mg.javalib.util.ArrayUtil;

public class DefaultHtml extends HTMLReport
{
	private String footer()
	{
		return "<div><a href='/'>Home</a> - " + "<a href='/doc'>Documentation</a> - "
				+ "</a><a href='http://www.datamining.informatik.uni-mainz.de/martin-guetlein'>Contact: Martin G&uuml;tlein</a>"
				+ "</div><div><a href='http://www.datamining.informatik.uni-mainz.de/'><img src=\"/img/jgu.png\" /></div>";
	}

	private String serviceHeader()
	{
		return "<a class='a_header' href=\"/\"><h1>" + ModelService.SERVICE_TITLE + "</h1></a>";
	}

	public static String css = "/css/styles.css";

	protected int maxMolPicSize = 300;
	protected int croppedPicSize = 150;

	public DefaultHtml(String id, String name, String subId, String subName)
	{
		this(null, id, name, subId, subName);
	}

	public DefaultHtml(String title, String id, String name, String subId, String subName)
	{
		super(title);

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
		setTitles(ModelService.SERVICE_TITLE, serviceHeader(), css, footer());
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

	public static String text(String key, Object... params)
	{
		return MessageFormat.format(bundle.getString(key), params);
	}

	public String moreLink(String docSection)
	{
		return encodeLink("/doc#" + DocHtml.getAnker(docSection), "more..");
	}

	public String depict(String smiles, int size) throws Exception
	{
		String sizeStr = "";
		if (size != -1)
			sizeStr = "&size=" + size;
		return "/depict?smiles=" + URLEncoder.encode(smiles, "UTF8") + sizeStr;
	}

	public String depictMatch(String smiles, int[] atoms, boolean highlightOutgoingBonds,
			Boolean activating, boolean crop, int size) throws Exception
	{
		String sizeStr = "";
		if (size != -1)
			sizeStr = "&size=" + size;
		String cropStr = "&crop=" + crop;
		if (atoms == null || atoms.length == 0)
			throw new IllegalArgumentException("atoms missing");
		String atomsStr = "&atoms="
				+ ArrayUtil.toString(ArrayUtil.toIntegerArray(atoms), ",", "", "", "");
		String highlightOutgoingBondsStr = "&highlightOutgoingBonds=" + highlightOutgoingBonds;
		String activatingStr = "";
		if (activating != null)
			activatingStr = "&activating=" + activating;
		return "/depictMatch?smiles=" + URLEncoder.encode(smiles, "UTF8") + sizeStr + atomsStr
				+ highlightOutgoingBondsStr + cropStr + activatingStr;
	}

	public String depictMultiMatch(String smiles, String modelId, int size) throws Exception
	{
		String sizeStr = "";
		if (size != -1)
			sizeStr = "&size=" + size;
		return "/depictMultiMatch?smiles=" + URLEncoder.encode(smiles, "UTF8") + "&model=" + modelId
				+ sizeStr;
	}
}
