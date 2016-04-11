package org.kramerlab.cfpservice.api.impl.html;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.kramerlab.cfpminer.appdomain.ADPrediction;
import org.kramerlab.cfpservice.api.ModelService;
import org.kramerlab.cfpservice.api.objects.Model;
import org.kramerlab.cfpservice.api.objects.Prediction;
import org.mg.javalib.util.ArrayUtil;
import org.mg.javalib.util.StringUtil;
import org.mg.wekalib.attribute_ranking.PredictionAttribute;
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
		return "<a class='a_header' href=\"/\"><h1><h1large>COFFER</h1large><br>CO<h1small>LLISION-FREE</h1small>"
				+ " F<h1small>ILTERED CIRCULAR</h1small>"
				+ " F<h1small>ING</h1small>ER<h1small>PRINT-BASED</h1small> QSARS</h1></a>";
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
		//		ResourceBundle.clearCache();
		//		bundle = ResourceBundle.getBundle("cfpservice");
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

	protected static Renderable getInsideAppDomain(Prediction p, String url)
	{
		return getInsideAppDomain(p.getADPrediction(),
				url != null ? url
						: "/" + p.getModelId() + "/appdomain?smiles="
								+ StringUtil.urlEncodeUTF8(p.getSmiles()),
				false);
	}

	protected static Renderable getInsideAppDomainCheck(Prediction p, String url)
	{
		return getInsideAppDomain(p.getADPrediction(),
				url != null ? url
						: "/" + p.getModelId() + "/appdomain?smiles="
								+ StringUtil.urlEncodeUTF8(p.getSmiles()),
				true);
	}

	private static Renderable getInsideAppDomain(final ADPrediction prediction, final String url,
			final boolean onlyCheckIcon)
	{
		return new Renderable()
		{
			public void renderOn(HtmlCanvas html) throws IOException
			{
				if (url != null)
					html.a(HtmlAttributesFactory.href(url));

				if (onlyCheckIcon)
				{
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
				}
				else
				{
					if (prediction == ADPrediction.Outside)
						html.write("\u26A0 ");
					html.write(prediction.toNiceString());
					html.br();
					//					html.div(HtmlAttributesFactory.class_("smallGrey"));
					//					html.write("p-Value: " + StringUtil.formatSmallDoubles(pValue));
					//					html._div();
				}

				if (url != null)
					html._a();
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
