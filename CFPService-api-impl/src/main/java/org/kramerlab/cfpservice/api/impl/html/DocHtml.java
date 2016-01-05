package org.kramerlab.cfpservice.api.impl.html;

public class DocHtml extends DefaultHtml
{
	public static String CLASSIFIERS = "Classifiers";

	public static String PREDICTION_FRAGMENTS = "Prediction Fragments";

	public static String FRAGMENTS = "UCFP Fragments";

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

		newSection("Background");

		addParagraph(text("doc.background"));

		newSection("Contact");

		addParagraph(text("doc.contact"));

		newSection(FRAGMENTS);

		addParagraph(text("doc.fragments"));

		newSection(CLASSIFIERS);

		addParagraph(text("doc.classifiers"));

		newSection(PREDICTION_FRAGMENTS);

		addParagraph(text("doc.predictionFragments"));

		newSection("Source Code");

		addParagraph(text("doc.source"));

		newSection("REST API");

		addParagraph(text("doc.rest"));

		newSection("How to cite this service");

		addParagraph(text("doc.cite"));

		return close();
	}
}