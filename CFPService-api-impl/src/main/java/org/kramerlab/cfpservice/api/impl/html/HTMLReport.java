package org.kramerlab.cfpservice.api.impl.html;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.mg.javalib.datamining.ResultSet;
import org.mg.javalib.util.StringUtil;
import org.mg.javalib.util.TimeFormatUtil;
import org.rendersnake.HtmlAttributes;
import org.rendersnake.HtmlAttributesFactory;
import org.rendersnake.HtmlCanvas;
import org.rendersnake.Renderable;

public class HTMLReport
{
	HtmlCanvas html;

	String pageTitle;
	String preTitleText;
	boolean hidePageTitle;
	boolean hideTableBorder;
	boolean wide = false;
	boolean addCreateDateToFooter = false;
	String portalTitle = "title";
	String portalHeader = "header";
	String footer = "footer";
	String cssFile = "css/styles.css";
	String breadCrumpUrls[];
	String breadCrumpNames[];
	int refresh = -1;

	protected String helpImg = "/home/martin/workspace/HTMLReporting/src/main/resources/help.png";
	protected String externalLinkImg = "/home/martin/workspace/HTMLReporting/src/main/resources/iconExternalLink.png";

	public HTMLReport()
	{
	}

	public HTMLReport(String pageTitle)
	{
		this.pageTitle = pageTitle;
	}

	public void setPageTitle(String pageTitle)
	{
		this.pageTitle = pageTitle;
	}

	public void setTitles(String portalTitle, String portalHeader, String cssFile, String footer)
	{
		this.portalTitle = portalTitle;
		this.portalHeader = portalHeader;
		this.cssFile = cssFile;
		this.footer = footer;
	}

	public void setBreadCrumps(String[] breadCrumpUrls, String[] breadCrumpNames)
	{
		this.breadCrumpUrls = breadCrumpUrls;
		this.breadCrumpNames = breadCrumpNames;
	}

	public void setPreTitleText(String preTitleText)
	{
		this.preTitleText = preTitleText;
	}

	public void setWide(boolean wide)
	{
		this.wide = wide;
	}

	public void setRefresh(int seconds)
	{
		if (html != null)
			throw new IllegalStateException();
		refresh = seconds;
	}

	protected HtmlCanvas getHtml() throws IOException
	{
		if (html == null)
		{
			html = new HtmlCanvas().html();
			html.head();
			html.macros().stylesheet(cssFile);
			String pageTitleStr = portalTitle;
			if (pageTitle != null && pageTitle.length() > 0)
				pageTitleStr = pageTitle + " - " + pageTitleStr;
			html.title().content(pageTitleStr);
			if (refresh > 0)
				html.meta(HtmlAttributesFactory.http_equiv("refresh").content(refresh + ""));
			html._head();
			html.body();
			html.div(HtmlAttributesFactory.id("header")).write(portalHeader, HtmlCanvas.NO_ESCAPE);
			html._div();
			html.div(HtmlAttributesFactory.id(wide ? "wide-content" : "content"));
			if (breadCrumpNames != null)
			{
				html.div(HtmlAttributesFactory.class_("smallGrey"));

				if (breadCrumpNames != null)
					for (int i = 0; i < breadCrumpNames.length; i++)
					{
						if (i > 0)
							html.write("&nbsp;>&nbsp;", false);
						html.a(HtmlAttributesFactory.href(breadCrumpUrls[i]));
						html.write(breadCrumpNames[i]);
						html._a();
					}
				//                if (breadCrumpNames != null && backToName != null)
				//                    html.write("&nbsp;&nbsp;|&nbsp;&nbsp;", false);
				//                if (backToName != null)
				//                {
				//                    html.a(HtmlAttributesFactory.href(backToUrl));
				//                    html.write("Back to " + backToName);
				//                    html._a();
				//                }
				html._div();
			}
			if (preTitleText != null)
				addParagraph(preTitleText);
			if (!hidePageTitle && (pageTitle != null && pageTitleStr.length() > 0))
				newSection(pageTitle);
		}
		return html;
	}

	public void setHidePageTitle(boolean hidePageTitle)
	{
		this.hidePageTitle = hidePageTitle;
	}

	public void setAddCreateDateToFooter(boolean addCreateDateToFooter)
	{
		this.addCreateDateToFooter = addCreateDateToFooter;
	}

	public void close(String outfile)
	{
		closeReport(outfile);
	}

	public String close()
	{
		return closeReport(null);
	}

	public static String mouseoverJavascript = "var span = document.querySelectorAll('.MouseoverHelp');\n"
			+ //
			"for (var i = span.length; i--;) {\n" + //
			"    (function () {\n" + //
			"        var t;\n" + //
			"        span[i].onmouseover = function () {\n" + //
			"            hideAll();\n" + //
			"            clearTimeout(t);\n" + //
			"            this.className = 'MouseoverHelpHover';\n" + //
			"        };\n" + //
			"        span[i].onmouseout = function () {\n" + //
			"            var self = this;\n" + //
			"            t = setTimeout(function () {\n" + //
			"                self.className = 'MouseoverHelp';\n" + //
			"            }, 300);\n" + //
			"        };\n" + //
			"    })();\n" + //
			"}\n" + //
			"\n" + //
			"function hideAll() {\n" + //
			"    for (var i = span.length; i--;) {\n" + //
			"        span[i].className = 'MouseoverHelp'; \n" + //
			"    }\n" + //
			"};";

	public String closeReport(String outfile)
	{
		try
		{
			getHtml()._div();
			getHtml()._body();
			getHtml().footer();
			if (addCreateDateToFooter)
				getHtml().write("Created at "
						+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
			getHtml().write(footer, HtmlCanvas.NO_ESCAPE);
			getHtml()._footer();
			getHtml().script(HtmlAttributesFactory.type("text/javascript"))
					.write(mouseoverJavascript, false)._script();
			getHtml()._html();

			if (outfile != null)
			{
				BufferedWriter buffy = new BufferedWriter(new FileWriter(outfile));
				buffy.write(getHtml().toHtml());
				buffy.close();
				System.out.println("wrote report to " + outfile);
				return null;
			}
			else
				return html.toHtml();
		}
		catch (Exception e)
		{
			throw new Error(e);
		}
	}

	class JSmolPlugin implements Renderable
	{
		String url;

		public JSmolPlugin(String url)
		{
			this.url = url;
		}

		@Override
		public void renderOn(HtmlCanvas html) throws IOException
		{
			html.write("<script type=\"text/javascript\" src=\"lib/JSmol.lite.js\"></script>",
					HtmlCanvas.NO_ESCAPE);
			html.write("<script type=\"text/javascript\">\nvar Info;\n" + "\n" + ";(function() {\n"
					+ "\n" + "\n" + "Info = {\n" + " border: 1,\n width: 300,\n"
					+ "	height: 300,\n" + "	debug: false,\n" + "	color: \"white\",\n"
					+ "	addSelectionOptions: false,\n" + "	serverURL: \"none\",\n"
					+ "	use: \"HTML5\",\n" + "	readyFunction: null,\n" + "	src: \"" + url + "\",\n"
					+ "    bondWidth: 3,\n" + "    zoomScaling: 1.5,\n" + "    pinchScaling: 2.0,\n"
					+ "    mouseDragFactor: 0.5,\n" + "    touchDragFactor: 0.15,\n"
					+ "    multipleBondSpacing: 4,\n" + "    spinRateX: 0.2,\n"
					+ "    spinRateY: 0.5,\n" + "    spinFPS: 20,\n" + "    spin:false,\n"
					+ "    debug: false\n" + "}\n" + "\n" + "})();\n</script>",
					HtmlCanvas.NO_ESCAPE);
			html.write(
					"<a href=\"javascript:jmol.spin(true)\">spin ON</a> <a href=\"javascript:jmol.spin(false)\">OFF</a>",
					HtmlCanvas.NO_ESCAPE);
			html.script().content("Jmol.getTMApplet(\"jmol\", Info)", HtmlCanvas.NO_ESCAPE);
		}
	}

	public JSmolPlugin getJSmolPlugin(String url)
	{
		return new JSmolPlugin(url);
	}

	private static String LINK_START = "[";
	private static String LINK_END = "]";
	private static String LINK_SEP = " ";

	public static Image getImage(String src)
	{
		return new Image(src);
	}

	public Image getImage(String src, String href, boolean border)
	{
		return new Image(src, href, border);
	}

	public Image getImage(String src, int width, int height)
	{
		return new Image(src, width, height);
	}

	public String encodeLink(String url, String text)
	{
		return LINK_START + url + LINK_SEP + text + LINK_END;
	}

	public class TextWithLinks implements Renderable
	{
		String text;
		boolean underline = false;
		boolean external = false;

		public TextWithLinks(String text)
		{
			this.text = text;
		}

		public TextWithLinks(String text, boolean underline, boolean external)
		{
			this(text);
			this.underline = underline;
			this.external = external;
		}

		@Override
		public void renderOn(HtmlCanvas html) throws IOException
		{
			render(html, text);
		}

		private void renderLink(HtmlCanvas html, String url, String text) throws IOException
		{
			HtmlAttributes attr = HtmlAttributesFactory.href(url);
			if (underline)
				attr.class_("underline");
			html.a(attr);
			if (text != null && text.length() > 1)
				html.write(text);
			if (external)
			{
				if (text != null && text.length() > 1)
					html.write(" ");
				html.img(HtmlAttributesFactory.src(externalLinkImg));
			}
			html._a();
		}

		private void render(HtmlCanvas html, String text) throws IOException
		{
			if (text.contains("<br>"))
			{
				boolean first = true;
				for (String t : text.split("<br>"))
				{
					if (first)
						first = false;
					else if (!t.isEmpty())
						html.br();
					render(html, t);
				}
			}
			else if (text.contains(LINK_START) && text.contains(LINK_END))
			{
				int s_start = text.indexOf(LINK_START);
				int s_end = s_start + LINK_START.length();

				int e_start = text.indexOf(LINK_END);
				int e_end = e_start + LINK_END.length();

				render(html, text.substring(0, s_start));

				String link = text.substring(s_end, e_start);
				if (link.contains(LINK_SEP))
				{
					int se_start = link.indexOf(LINK_SEP);
					int se_end = se_start + LINK_SEP.length();
					renderLink(html, link.substring(0, se_start), link.substring(se_end));
				}
				else
					renderLink(html, link, null);

				render(html, text.substring(e_end));
			}
			else
				html.write(text);
		}
	}

	public void addParagraphExternal(String text)
	{
		addParagraph(new TextWithLinks(text, false, true));
	}

	public void setExternalLinkImg(String externalLinkImg)
	{
		this.externalLinkImg = externalLinkImg;
	}

	public Renderable getExternalLink(final String text, final String textUrl,
			final String... extUrls)
	{
		return new Renderable()
		{

			public void renderOn(HtmlCanvas html) throws IOException
			{
				if (textUrl != null)
					html.a(HtmlAttributesFactory.href(textUrl));
				html.write(text + " ");
				if (textUrl != null)
					html._a();
				for (String url : extUrls)
				{
					html.a(HtmlAttributesFactory.href(url.toString()));
					html.img(HtmlAttributesFactory.src(externalLinkImg));
					html._a();
					html.write(" ");
				}
			}
		};
	}

	public Renderable getList(final ResultSet set)
	{
		return new Renderable()
		{

			@Override
			public void renderOn(HtmlCanvas html) throws IOException
			{
				html.ul();
				for (String p : set.getProperties())
				{
					html.li().render(new TextWithLinks(p + ": " + set.getUniqueValue(p)))._li();
				}
				html._ul();
			}
		};
	}

	public void addList(ResultSet set)
	{
		try
		{
			getHtml().render(getList(set));
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public void addList(List<?> list)
	{
		try
		{
			getHtml().render(getList(list));
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public Renderable getList(final List<?> list)
	{
		return new Renderable()
		{
			@Override
			public void renderOn(HtmlCanvas html) throws IOException
			{
				html.ul();
				for (Object o : list)
				{
					if (o instanceof Renderable)
						html.li().render((Renderable) o)._li();
					else
						html.li().render(new TextWithLinks(o.toString()))._li();
				}
				html._ul();
			}
		};
	}

	public void setHelpImg(String helpImg)
	{
		this.helpImg = helpImg;
	}

	public Renderable getMouseoverHelp(final String helpText)
	{
		return getMouseoverHelp(helpText, null);
	}

	public Renderable getMouseoverHelp(final String helpText, final String title)
	{
		return getMouseoverHelp(helpText, title, getImage(helpImg));
	}

	public Renderable getMouseoverHelp(final String helpText, final String title, final Image image)
	{
		return getMouseoverHelp(new TextWithLinks(helpText, true, false), title, image);
	}

	public Renderable getMouseoverHelp(final Renderable helpText, final String title)
	{
		return getMouseoverHelp(helpText, title, getImage(helpImg));
	}

	public Renderable getMouseoverHelp(final Renderable helpText, final String title,
			final Image image)
	{
		return new Renderable()
		{

			@Override
			public void renderOn(HtmlCanvas html) throws IOException
			{
				if (title != null)
				{
					html.write(title);
					html.write("&nbsp;", false);
				}
				HtmlAttributes attr = HtmlAttributesFactory.class_("MouseoverHelp");
				html.span(attr);
				//html.a(HtmlAttributesFactory.href("google.de"));
				html.render(image);
				//			html.write("?");
				//html._a();
				html.div(attr).render(helpText)._div();
				html._span();
			}
		};
	}

	public void addMouseoverHelp(String helpText, String title)
	{
		addMouseoverHelp(helpText, title, getImage(helpImg));
	}

	public void addMouseoverHelp(String helpText, String title, Image image)
	{
		try
		{
			getHtml().render(getMouseoverHelp(helpText, title, image));
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args)
	{
		HTMLReport rep = new HTMLReport("Title of this page");
		rep.setTitles("Title", "<h1>Title</h1>",
				"/home/martin/workspace/HTMLReporting/src/main/resources/css/styles.css", "footer");
		String names[] = { "home", "documentation", "this-document" };
		String urls[] = { "/", "/documentation", "/documentation/this-document" };
		rep.setBreadCrumps(urls, names);

		rep.addMouseoverHelp("/home/martin/tmp/help.png",
				"if you need more help, this will be documented here:<br>"
						+ rep.encodeLink("google.de", "a-link"));

		rep.newSection("Section");
		rep.addParagraph("Bla a lot of test\nmore text");

		rep.addForm("send.html", "compound", "predict");

		rep.addParagraph("Bla a lot of test\nmore text");

		rep.addImage(getImage("/home/martin/tmp/rules/img/082c41ebf41b1e9a348ff44634f3a790.png"));

		rep.addImage(rep.getImage("/home/martin/tmp/rules/img/082c41ebf41b1e9a348ff44634f3a790.png",
				100, 100));

		ResultSet set = new ResultSet();
		int idx = set.addResult();
		set.setResultValue(idx,
				"bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla",
				123);
		set.setResultValue(idx, "blub", "true " + rep.encodeLink("google.de", "a-link"));
		set.setResultValue(idx, "blob", "img");
		idx = set.addResult();
		set.setResultValue(idx, "bla", rep.getJSmolPlugin("data/test.sdf"));
		//		set.setResultValue(idx, "bla", new Toggler("hidden image", new Image(
		//				"/home/martin/workspace/JavaLib/data/ok.png")));
		set.setResultValue(idx, "blub", 456);

		rep.setTableColWidthLimited(true);
		rep.addTable(set);
		rep.setTableColWidthLimited(false);
		rep.addTable(set);

		rep.addParagraph("here comes # the link '" + rep.encodeLink("http://google.de", "gogle")
				+ "' <- link");

		set = new ResultSet();
		idx = set.addResult();
		set.setResultValue(idx, "bla", 123);
		set.setResultValue(idx, "blub", "true " + rep.encodeLink("google.de", "a-link"));
		set.setResultValue(idx, "blob", "img");
		idx = set.addResult();
		set.setResultValue(idx, "bla", rep.getJSmolPlugin("data/test.sdf"));
		//		set.setResultValue(idx, "bla", new Toggler("hidden image", new Image(
		//				"/home/martin/workspace/JavaLib/data/ok.png")));
		set.setResultValue(idx, "blub", 456);
		rep.addTable(set);

		rep.setHideTableBorder(true);
		rep.addTable(set, true);
		rep.setHideTableBorder(false);

		set = new ResultSet();
		idx = set.addResult();
		set.setResultValue(idx, "bla", 123);
		set.setResultValue(idx, "blub", "true " + rep.encodeLink("google.de", "a-link"));
		rep.addList(set);

		rep.startLeftColumn();
		rep.newSection("Section Left Column");
		rep.addParagraph(
				"blub bla blub blob blub bla blub blob blub bla blub blob blub bla blub blob ");
		rep.startRightColumn();
		rep.newSection("Section Right Column");
		rep.addParagraph(
				"blub bla blub blob blub bla blub blob blub bla blub blob\nblub bla blub blob blub bla blub blob blub bla blub blob\nblub bla blub blob blub bla blub blob blub bla blub blob");
		rep.stopColumns();

		rep.close("/tmp/delme.html");
	}

	public static class Image implements Renderable
	{
		HtmlAttributes attributes = new HtmlAttributes();
		String img;
		String href;

		public Image(String img)
		{
			this(img, null, false);
		}

		public Image(String img, String href, boolean border)
		{
			this(img, href, border, -1, -1);
		}

		public Image(String img, int width, int height)
		{
			this(img, null, false, width, height);
		}

		public Image(String img, String href, boolean border, int width, int height)
		{
			this.img = img;
			attributes.src(img);
			if (href != null)
				this.href = href;
			else if (width != -1 || height != -1)
				this.href = img;
			if (width != -1 || height != -1)
				attributes.width(width).height(height);
			if (border)
				attributes.add("border", 1);
		}

		public void renderOn(HtmlCanvas html) throws IOException
		{
			if (href != null)
				html.a(HtmlAttributesFactory.href(href));
			html.img(attributes);
			if (href != null)
				html._a();
		}

		public Image alt(String alt)
		{
			this.attributes.alt(alt);
			return this;
		}
	}

	private static HtmlAttributes getAnker(String name)
	{
		return HtmlAttributesFactory.id(name.toLowerCase().replaceAll(" ", "-"));
	}

	public void newSection(String title)
	{
		newSection(title, true);
	}

	public void newSection(String title, boolean addNormalSpace)
	{
		try
		{
			HtmlAttributes att = getAnker(title);
			if (!addNormalSpace)
				att.class_("inline");
			getHtml().h2(att).content(title);
		}
		catch (IOException e)
		{
			throw new Error(e);
		}
	}

	public void addParagraph(String text)
	{
		addParagraph(new TextWithLinks(text, true, false));
	}

	public void addParagraph(Renderable r)
	{
		try
		{
			getHtml().div().render(r)._div();
		}
		catch (IOException e)
		{
			throw new Error(e);
		}
	}

	public void addForm(String dest, String param, String button)
	{
		addForm(dest, param, button, null);
	}

	public void addForm(String dest, String param, String button, String placeholder)
	{
		try
		{
			HtmlAttributes attr = HtmlAttributesFactory.action(dest).method("POST");
			getHtml().form(attr);
			//attr = HtmlAttributesFactory.cols("50").id("text").name(param).rows("5");
			//html.textarea(attr)._textarea();
			attr = HtmlAttributesFactory.size("50").id("text").name(param);
			if (placeholder != null)
				attr.add("placeholder", placeholder);
			getHtml().input(attr);
			attr = HtmlAttributesFactory.type("submit").value(button);
			getHtml().write("&nbsp;", false);
			getHtml().input(attr);
			getHtml()._form();
		}
		catch (IOException e)
		{
			throw new Error(e);
		}
	}

	public void addImage(Image img)
	{
		try
		{
			getHtml().div().render(img)._div();
		}
		catch (IOException e)
		{
			throw new Error(e);
		}
	}

	public void addImage(String file)
	{
		try
		{
			getHtml().div().render(new Image(file))._div();
		}
		catch (IOException e)
		{
			throw new Error(e);
		}
	}

	public void addSmallImages(String[] smallImages)
	{
		try
		{
			HtmlCanvas table = getHtml().table();
			int i = 0;
			for (String file : smallImages)
			{
				//					String f = image_dir + file.getName();
				//					FileUtil.createParentFolders(f);
				//					FileUtil.copy(file, new File(f));
				//					file.delete();
				if (i % 2 == 0)
					table.tr().td().render(new Image(file))._td();
				else
					table.td().render(new Image(file))._td()._tr();
				i++;
			}
			table._table();
		}
		catch (IOException e)
		{
			throw new Error(e);
		}

	}

	//	public void addSection(String title, String content, ResultSet[] tables, String[] images, String[] smallImages)
	//	{
	//		newSection(title);
	//		if (content != null && content.length() > 0)
	//			addParagraph(content);
	//
	//		if (tables != null && tables.length > 0)
	//			for (ResultSet rs : tables)
	//				addTable(rs);
	//
	//		if (images != null && images.length > 0)
	//			for (String file : images)
	//				addImage(file);
	//
	//		if (smallImages != null && smallImages.length > 0)
	//			addSmallImages(smallImages);
	//	}

	private HashMap<String, String> headerHelp = new HashMap<>();

	public void setHeaderHelp(String headerProp, String helpText)
	{
		headerHelp.put(headerProp, helpText);
	}

	private class TableHeader extends TableData
	{

		public TableHeader(String prop, Object val, boolean format, boolean transpose)
		{
			super(prop, val, format);
			if (transpose)
				attr = HtmlAttributesFactory.class_("transpose");
		}

		protected void init(HtmlCanvas html) throws IOException
		{
			html.th(attr);
		}

		protected void end(HtmlCanvas html) throws IOException
		{
			if (headerHelp.containsKey(prop))
			{
				html.write(" ");
				html.render(getMouseoverHelp(headerHelp.get(prop)));
			}
			html._th();
		}
	}

	private class TableData implements Renderable
	{
		Object val;
		String prop;
		HtmlAttributes attr;

		public TableData(String prop, Object val, boolean format)
		{
			this.prop = prop;
			this.val = val;

			String clazz = "";
			if (format)
			{
				if (val != null && val.toString().matches(".*inactive.*"))
				{
					//					if (val.toString().matches(".*low.*"))
					//						attr = HtmlAttributesFactory.class_("inactive_low");
					//					else if (val.toString().matches(".*medium.*"))
					//						attr = HtmlAttributesFactory.class_("inactive_medium");
					//					else
					clazz = "inactive";
				}
				else if (val != null && val.toString().matches(".*active.*"))
				{
					//					if (val.toString().matches(".*low.*"))
					//						attr = HtmlAttributesFactory.class_("active_low");
					//					else if (val.toString().matches(".*medium.*"))
					//						attr = HtmlAttributesFactory.class_("active_medium");
					//					else
					clazz = "active";
				}
				else if (val != null && val.toString().matches(".*missing.*"))
					clazz = "missing";
				else if (val != null && val.toString().matches(".*outside.*"))
					clazz = "outside";
			}
			if (tableColWidthLimited)
				clazz += " slim";
			if (prop.equals("#"))
				clazz += " removePaddingRight";
			attr = HtmlAttributesFactory.class_(clazz);
		}

		protected void init(HtmlCanvas html) throws IOException
		{
			html.td(attr);
		}

		protected void end(HtmlCanvas html) throws IOException
		{
			html._td();
		}

		@Override
		public void renderOn(HtmlCanvas html) throws IOException
		{
			init(html);
			if (val != null)
			{
				if (prop.contains("runtime") && !val.toString().contains("runtime"))
					html.write(TimeFormatUtil.format(((Double) val).longValue()));
				else if (val instanceof Renderable)
					html.render((Renderable) val);
				else if (val instanceof Double)
					html.write(StringUtil.formatDouble((Double) val, 3));
				else if (val instanceof String)
					html.render(new TextWithLinks((String) val));
				else
					html.write(val + "");
			}
			end(html);
		}
	}

	//	private static void setCell(HtmlCanvas table, String p, Object val) throws IOException
	//	{
	//		table.td();
	//
	//		if (val == null)
	//			table.write("");
	//		else
	//		{
	//			HtmlAttributes attr = null;
	//			if (val.toString().matches(".*inactive.*"))
	//			{
	//				if (val.toString().matches(".*low.*"))
	//					table.attributes().class_("inactive_low");
	//				else if (val.toString().matches(".*medium.*"))
	//					attr = HtmlAttributesFactory.class_("inactive_medium");
	//				else
	//					attr = HtmlAttributesFactory.class_("inactive");
	//			}
	//			else if (val.toString().matches(".*active.*"))
	//			{
	//				if (val.toString().matches(".*low.*"))
	//					attr = HtmlAttributesFactory.class_("active_low");
	//				else if (val.toString().matches(".*medium.*"))
	//					attr = HtmlAttributesFactory.class_("active_medium");
	//				else
	//					table.attributes().class_("active");
	//			}
	//			if (val.toString().matches(".*missing.*"))
	//				attr = HtmlAttributesFactory.class_("missing");
	//
	//		}
	//
	//		table._td();
	//	}

	//	boolean inlineTable = false;
	boolean tableRowsAlternating = true;
	boolean tableColWidthLimited = true;

	public void setHideTableBorder(boolean b)
	{
		hideTableBorder = b;
	}

	Boolean doubleCol = null;

	public void startLeftColumn()
	{
		if (doubleCol != null)
			throw new IllegalStateException();
		doubleCol = true;
		try
		{
			getHtml().div(HtmlAttributesFactory.class_("wrap_col"));
			getHtml().div(HtmlAttributesFactory.class_("left_col"));
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public void startRightColumn()
	{
		if (doubleCol == null || doubleCol == false)
			throw new IllegalStateException();
		doubleCol = false;
		try
		{
			getHtml()._div();
			getHtml().div(HtmlAttributesFactory.class_("right_col"));
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public void stopColumns()
	{
		if (doubleCol == null || doubleCol == true)
			throw new IllegalStateException();
		doubleCol = null;
		try
		{
			getHtml()._div();
			getHtml().div(HtmlAttributesFactory.class_("clear_col"));
			getHtml()._div();
			getHtml()._div();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public void setTableRowsAlternating(boolean tableRowsAlternating)
	{
		this.tableRowsAlternating = tableRowsAlternating;
	}

	public void setTableColWidthLimited(boolean tableColWidthLimited)
	{
		this.tableColWidthLimited = tableColWidthLimited;
	}

	public void addTable(ResultSet rs)
	{
		addTable(rs, null);
	}

	public void addTable(ResultSet rs, Boolean transpose)
	{
		addTable(rs, transpose, null);
	}

	public void addTable(ResultSet rs, Boolean transpose, String title)
	{
		addTable(rs, transpose, title, false);
	}

	public void addTable(ResultSet rs, Boolean transpose, String title, boolean format)
	{

		//		.tr().th().content("City").th().content("Country")._tr().tr().td().content("Amsterdam").td()
		//				.content("The Netherlands")._tr()._table();

		if (title != null)
		{
			try
			{
				getHtml().h4().content(title);
			}
			catch (IOException e)
			{
				throw new Error(e);
			}
		}

		try
		{
			String clazz = "";
			if (tableRowsAlternating)
				clazz += "alternate";
			if (hideTableBorder)
				clazz += " noBorder";
			HtmlAttributes attr = HtmlAttributesFactory.class_(clazz);
			HtmlCanvas table = getHtml().table(attr);
			if ((transpose == null && rs.getProperties().size() > 8
					&& rs.getProperties().size() > rs.getNumResults() + 1)
					|| (transpose != null && transpose))
			{
				//transpose, header is first column
				for (String p : rs.getProperties())
				{
					String niceP = rs.getNiceProperty(p);
					table.tr(tableColWidthLimited ? HtmlAttributesFactory.class_("slim") : null);
					table.render(new TableHeader(niceP, niceP, format,
							(transpose != null && transpose)));
					for (int i = 0; i < rs.getNumResults(); i++)
					{
						Object val = rs.getResultValue(i, p);
						table.render(new TableData(niceP, val, format));
						//setCell(table, p, val);
					}
					table._tr();
				}
			}
			else
			{
				table.tr(tableColWidthLimited ? HtmlAttributesFactory.class_("slim") : null);
				for (String p : rs.getProperties())
				{
					String niceP = rs.getNiceProperty(p);
					table.render(new TableHeader(niceP, niceP, format,
							(transpose != null && transpose)));
				}
				table._tr();
				for (int i = 0; i < rs.getNumResults(); i++)
				{
					attr = getAnker(rs.getResultValue(i, rs.getProperties().get(0)) + "");
					if (tableColWidthLimited)
						attr.class_("slim");
					table.tr(attr);
					for (String p : rs.getProperties())
					{
						String niceP = rs.getNiceProperty(p);
						Object val = rs.getResultValue(i, p);
						table.render(new TableData(niceP, val, format));
						//setCell(table, p, val);
					}
					table._tr();
				}
			}
			table._table();
		}
		catch (Exception e)
		{
			throw new Error(e);
		}
	}

	//	public void newParagraph()
	//	{
	//		try
	//		{
	//			body.div();
	//		}
	//		catch (IOException e)
	//		{
	//			e.printStackTrace();
	//		}
	//	}
	//
	//	public void addText(String string)
	//	{
	//		try
	//		{
	//			body.write(string);
	//		}
	//		catch (IOException e)
	//		{
	//			e.printStackTrace();
	//		}
	//	}
	//
	//	public void addLink(String url, String text)
	//	{
	//		try
	//		{
	//			body.a(HtmlAttributesFactory.href(url)).content(text);
	//		}
	//		catch (IOException e)
	//		{
	//			e.printStackTrace();
	//		}
	//	}
	//
	//	public void closeParagraph()
	//	{
	//		try
	//		{
	//			body._div();
	//		}
	//		catch (IOException e)
	//		{
	//			e.printStackTrace();
	//		}
	//	}

	public void addGap()
	{
		try
		{
			getHtml().br();
		}
		catch (IOException e)
		{
			throw new Error(e);
		}
	}

	public void newSubsection(String string)
	{
		try
		{
			getHtml().h3(getAnker(string)).content(string);
		}
		catch (IOException e)
		{
			throw new Error(e);
		}
	}

	public static Renderable join(final Renderable... r)
	{
		return new Renderable()
		{
			@Override
			public void renderOn(HtmlCanvas html) throws IOException
			{
				for (Renderable renderable : r)
					html.render(renderable);
			}
		};
	}

	public static Renderable getHTMLCode(final String unescapedStr)
	{
		return new Renderable()
		{
			@Override
			public void renderOn(HtmlCanvas html) throws IOException
			{
				html.write(unescapedStr, false);
			}
		};
	}

}
