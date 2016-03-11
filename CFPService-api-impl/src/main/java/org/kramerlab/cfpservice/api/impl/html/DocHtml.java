package org.kramerlab.cfpservice.api.impl.html;

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

	public String build() throws Exception
	{
		setHidePageTitle(true);

		newSection("How to cite this service");
		addParagraphExternal(text("doc.cite"));

		newSection("About");
		addParagraphExternal(text("doc.about"));

		newSection(FILTERED_FRAGMENTS);
		addParagraphExternal(text("doc.filteredFragments"));

		newSection(CLASSIFIERS);
		addParagraphExternal(text("doc.classifiers"));

		newSection(RANKING_FRAGMENTS);
		addParagraphExternal(text("doc.rankingFragments"));

		newSection(VALIDATION);
		addParagraphExternal(text("doc.validation"));

		newSection("Source Code");
		addParagraphExternal(text("doc.source"));

		newSection("REST API");
		addParagraphExternal(text("doc.rest"));

		newSection("License");
		addParagraphExternal(text("doc.license"));

		return close();
	}
}
