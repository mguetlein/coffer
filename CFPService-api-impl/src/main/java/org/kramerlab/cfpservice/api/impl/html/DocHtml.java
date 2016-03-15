package org.kramerlab.cfpservice.api.impl.html;

import org.kramerlab.cfpservice.api.impl.ModelServiceImpl;

public class DocHtml extends DefaultHtml
{
	public static String CLASSIFIERS = "Classifiers";

	public static String RANKING_FRAGMENTS = "Ranking of Fragments";

	public static String FILTERED_FRAGMENTS = "Filtering of Circular Fingerprint Fragments";

	public static String VALIDATION = "Validation";

	public static String getAnker(String section)
	{
		return section.toLowerCase().replaceAll(" ", "-");
	}

	public DocHtml()
	{
		super("Documentation", "doc", "Documentation", null, null);
	}

	public String build()
	{
		setHidePageTitle(true);

		newSection("How to cite this service");
		addParagraphExternal(text("doc.cite"));
		addGap();

		newSection("About");
		addParagraphExternal(text("doc.about"));
		addGap();

		newSection(FILTERED_FRAGMENTS);
		addParagraphExternal(text("doc.filteredFragments"));
		addGap();

		newSection(CLASSIFIERS);
		addParagraphExternal(text("doc.classifiers"));
		addGap();

		newSection(RANKING_FRAGMENTS);
		addParagraphExternal(text("doc.rankingFragments"));
		addGap();

		newSection(VALIDATION);
		addParagraphExternal(text("doc.validation"));
		addGap();

		newSection("Source Code");
		addParagraphExternal(text("doc.source"));
		addGap();

		newSection("License");
		addParagraphExternal(text("doc.license"));
		addGap();

		newSection("REST API");
		addParagraphExternal(text("doc.rest"));
		addGap();

		newSection("REST Example");
		addParagraphExternal(text("rest.intro"));

		for (int i = 0; i < Integer.parseInt(text("rest.num")); i++)
		{
			newSubsection(text("rest." + i + ".title", ModelServiceImpl.HOST));
			addParagraph(text("rest." + i + ".curl", ModelServiceImpl.HOST));
			addGap();
			addParagraph(text("rest." + i + ".res", ModelServiceImpl.HOST));
		}

		return close();
	}
}
