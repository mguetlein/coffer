package org.kramerlab.cfpservice.api.impl.html;

public class DocHtml extends DefaultHtml
{
	public static String CLASSIFIERS = "Classifiers";

	public static String PREDICTION_FRAGMENTS = "Prediction Fragments";

	public static String FRAGMENTS = "CFP Fragments";

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

		newSection("Background");

		addParagraph(text("doc.background"));

		newSection(FRAGMENTS);

		addParagraph(text("doc.fragments"));

		newSection(CLASSIFIERS);

		addParagraph(text("doc.classifiers"));

		newSection(PREDICTION_FRAGMENTS);

		addParagraph(text("doc.predictionFragments"));

		newSection(VALIDATION);

		addParagraph(text("doc.validation"));

		newSection("Source Code");

		addParagraph(text("doc.source"));

		newSection("REST API");

		addParagraph(text("doc.rest"));

		newSection("How to cite this service");

		addParagraph(text("doc.cite"));

		return close();
	}
}
