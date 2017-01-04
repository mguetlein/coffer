package org.kramerlab.coffer.api.impl.html;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.kramerlab.cfpminer.appdomain.ADPrediction;
import org.kramerlab.coffer.api.ModelService;
import org.kramerlab.coffer.api.objects.Model;
import org.kramerlab.coffer.api.objects.Prediction;
import org.mg.javalib.util.ArrayUtil;
import org.mg.javalib.util.ResourceBundleOwner;
import org.mg.javalib.util.StringUtil;
import org.mg.wekalib.attribute_ranking.PredictionAttribute;
import org.rendersnake.HtmlAttributes;
import org.rendersnake.HtmlAttributesFactory;
import org.rendersnake.HtmlCanvas;
import org.rendersnake.Renderable;

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
		//return "<a class='a_header' href=\"/\"><h1>" + ModelService.SERVICE_TITLE + "</h1></a>";
		return "<a class='a_header' href=\"/\"><h1><h1large>CoFFer</h1large><br>Co<h1small>llision-free</h1small>"
				+ " F<h1small>iltered Circular</h1small>"
				+ " F<h1small>ing</h1small>er<h1small>print-based QSARS</h1small></h1></a>";
	}

	public static String css = "/css/styles.css";

	protected int maxMolPicSize = 300;
	protected int croppedPicSize = 150;

	private static final String trackingCode;

	static
	{
		String s = "";
		try
		{
			File f = new File(System.getProperty("user.home") + "/results/coffer/trackingCode");
			if (f.exists())
				s = IOUtils.toString(new FileInputStream(f));
			else
				s = "<!-- file not found: " + f.getAbsolutePath() + " -->";
		}
		catch (IOException e)
		{
			e.printStackTrace();
			s = "<!-- sth went wrong: " + e.getMessage() + " -->";
		}
		trackingCode = s;
	}

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
		setTrackingCode(trackingCode);
	}

	public static Renderable doubleText(final String mainText, final String greyText,
			final String url)
	{
		return doubleText(mainText, greyText, url, false);
	}

	public static Renderable doubleText(final String mainText, final String greyText,
			final String url, final boolean lineBreak)
	{
		return new Renderable()
		{
			public void renderOn(HtmlCanvas html) throws IOException
			{
				if (url != null)
					html.a(HtmlAttributesFactory.href(url));

				html.write(mainText + (lineBreak ? "" : " "));
				if (lineBreak)
					html.br();
				HtmlAttributes atts = HtmlAttributesFactory.class_("smallGrey");
				if (!lineBreak)
					atts = atts.style("display: inline;");
				html.div(atts);
				html.write(greyText);
				html._div();

				if (url != null)
					html._a();
			}
		};
	}

	private static ResourceBundleOwner bundle = new ResourceBundleOwner("coffer");

	public static String text(String key)
	{
		return bundle.text(key);
	}

	public static String text(String key, Object... params)
	{
		return bundle.text(key, params);
	}

	public String moreLink(String docSection)
	{
		return encodeLink("/doc#" + DocHtml.getAnker(docSection), "more..");
	}

	public String depict(String smiles, int size)
	{
		String sizeStr = "";
		if (size != -1)
			sizeStr = "&size=" + size;
		return "/depict?smiles=" + StringUtil.urlEncodeUTF8(smiles) + sizeStr;
	}

	public String depictMatch(String smiles, int[] atoms, boolean highlightOutgoingBonds,
			Boolean activating, boolean crop, int size)
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
		return "/depictMatch?smiles=" + StringUtil.urlEncodeUTF8(smiles) + sizeStr + atomsStr
				+ highlightOutgoingBondsStr + cropStr + activatingStr;
	}

	public String depictMultiMatch(String smiles, String modelId, int size)
	{
		String sizeStr = "";
		if (size != -1)
			sizeStr = "&size=" + size;
		return "/depictMultiMatch?smiles=" + StringUtil.urlEncodeUTF8(smiles) + "&model=" + modelId
				+ sizeStr;
	}

	protected Renderable getInsideAppDomain(Prediction p, String url)
	{
		return getInsideAppDomain(p.getADPrediction(),
				url != null ? url
						: "/" + p.getModelId() + "/appdomain?smiles="
								+ StringUtil.urlEncodeUTF8(p.getSmiles()),
				false);
	}

	protected Renderable getInsideAppDomainCheck(Prediction p, String url)
	{
		return getInsideAppDomain(p.getADPrediction(),
				url != null ? url
						: "/" + p.getModelId() + "/appdomain?smiles="
								+ StringUtil.urlEncodeUTF8(p.getSmiles()),
				true);
	}

	private Renderable getInsideAppDomain(final ADPrediction prediction, final String url,
			final boolean onlyCheckIcon)
	{
		return new Renderable()
		{
			public void renderOn(HtmlCanvas html) throws IOException
			{
				if (onlyCheckIcon)
				{
					if (url != null)
						html.a(HtmlAttributesFactory.href(url));
					switch (prediction)
					{
						case Inside:
							html.write("\u2713");
							break;
						case PossiblyOutside:
							html.write("?");
							break;
						default:
							html.write("-");
					}
					if (url != null)
						html._a();
				}
				else
				{
					if (prediction == ADPrediction.Outside)
						html.write("\u26A0 ");
					html.write(prediction.toNiceString());
					html.br();

					if (url != null)
					{
						html.div(HtmlAttributesFactory.class_("smallGrey"));
						new TextWithLinks(encodeLink(url, "(inspect App-Domain)"), true, false)
								.renderOn(html);
						html._div();
						//html.write("p-Value: " + StringUtil.formatSmallDoubles(pValue));
					}
				}

			}
		};
	}

	//	protected static Renderable getPrediction(double dist[], int predIdx, int activeClassIdx,
	//			boolean hideNonMax)
	//	{
	//		return getPrediction(dist, miner.getClassValues(), predIdx, activeClassIdx, hideNonMax,
	//				null);
	//	}

	protected static Renderable getPrediction(Prediction p, Model m, String url)
	{
		return getPrediction(p.getPredictedDistribution(), m.getClassValues(), p.getPredictedIdx(),
				m.getActiveClassIdx(), true, url, false);
	}

	protected static Renderable getPredictionWithIcon(Prediction p, Model m, String url)
	{
		return getPrediction(p.getPredictedDistribution(), m.getClassValues(), p.getPredictedIdx(),
				m.getActiveClassIdx(), true, url, true);
	}

	protected static Renderable getPrediction(PredictionAttribute pa, Model m)
	{
		return getPrediction(pa.getAlternativeDistributionForInstance(), m.getClassValues(),
				pa.getAlternativePredictionIdx(), m.getActiveClassIdx(), false, null, false);
	}

	private static Renderable getPrediction(final double dist[], final String classValues[],
			final int predIdx, final int activeClassIdx, final boolean hideNonMax, final String url,
			final boolean icon)
	{
		return new Renderable()
		{
			public void renderOn(HtmlCanvas html) throws IOException
			{
				if (url != null)
					html.a(HtmlAttributesFactory.href(url));

				if (icon)
				{
					html.table(HtmlAttributesFactory.style("border:0px"));
					html.tr(HtmlAttributesFactory.style("background-color:transparent"));
					html.td(HtmlAttributesFactory.style("padding:0px"));
					getImage("/depictActiveIcon?probability=" + dist[activeClassIdx])
							.renderOn(html);
					html.write("&nbsp;&nbsp;", false);
					html._td();
					html.td(HtmlAttributesFactory.style("padding:0px"));

					html.write(classValues[predIdx] + " ("
							+ StringUtil.formatDouble(dist[predIdx] * 100) + "%)");

					html._td();
					html._tr();
					html._table();

				}
				else
				{
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
				}

				if (url != null)
					html._a();
			}
		};
	}
}
