package org.kramerlab.coffer.api.impl.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.kramerlab.coffer.api.impl.html.DefaultHtml;
import org.mg.javalib.util.StringUtil;
import org.rendersnake.HtmlAttributesFactory;
import org.rendersnake.HtmlCanvas;
import org.rendersnake.Renderable;

public abstract class CompoundInfo implements Renderable
{
	public enum Service
	{
		pubchem, chembl, all
	}

	public abstract String getJsonUrl(String encodedSmiles);

	protected abstract void parseJson(JSONObject obj) throws JSONException, FileNotFoundException;

	protected abstract String getLinkout();

	protected abstract String getServiceName();

	protected abstract String getIdName();

	private static HashMap<String, CompoundInfo> cache = new HashMap<>();

	private static CompoundInfo getService(Service service, String smiles)
			throws MalformedURLException, JSONException, IOException
	{
		String key = service + " " + smiles;
		if (!cache.containsKey(key))
		{
			if (service == Service.pubchem)
				cache.put(key, new PubChemCompoundInfo(smiles));
			else if (service == Service.chembl)
				cache.put(key, new ChEMBLCompoundInfo(smiles));
			else
				throw new IllegalStateException();
		}
		return cache.get(key);
	}

	public static String getHTML(Service service, String smiles)
			throws MalformedURLException, JSONException, IOException
	{
		if (service == Service.pubchem || service == Service.chembl)
			return getService(service, smiles).getHTML();
		else if (service == Service.all)
		{
			HtmlCanvas html = new HtmlCanvas();
			html.macros().stylesheet(DefaultHtml.css);
			html.body(HtmlAttributesFactory.class_("pubchem"));
			CompoundInfo p = getService(Service.pubchem, smiles);
			CompoundInfo c = getService(Service.chembl, smiles);
			if (p.id != null)
				p.renderOn(html);
			if (p.id != null && c.id != null)
				html.br();
			if (c.id != null)
				c.renderOn(html);
			html._body();
			return html.toHtml();
		}
		else
			throw new IllegalArgumentException();
	}

	public static class PubChemCompoundInfo extends CompoundInfo
	{
		public PubChemCompoundInfo(String smiles)
		{
			super(smiles);
		}

		@Override
		public String getJsonUrl(String encodedSmiles)
		{
			return "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/smiles/" + encodedSmiles
					+ "/JSON";
		}

		@Override
		protected void parseJson(JSONObject obj) throws JSONException, FileNotFoundException
		{
			obj = obj.getJSONArray("PC_Compounds").getJSONObject(0);
			if (obj.getJSONObject("id").isNull("id"))
				throw new FileNotFoundException("pubchem json has no cid");
			id = obj.getJSONObject("id").getJSONObject("id").getString("cid");
			JSONArray props = obj.getJSONArray("props");
			for (int i = 0; i < props.length(); i++)
			{
				if (props.getJSONObject(i).getJSONObject("urn").getString("label").equals("Log P"))
					fields.put("LogP", StringUtil.formatDouble(Double.parseDouble(
							props.getJSONObject(i).getJSONObject("value").getString("fval")), 1));
				if (props.getJSONObject(i).getJSONObject("urn").getString("label")
						.equals("Molecular Weight"))
					fields.put("Molecular Weight", StringUtil.formatDouble(Double.parseDouble(
							props.getJSONObject(i).getJSONObject("value").getString("fval")), 1));
			}

			String titleUrl = "https://pubchem.ncbi.nlm.nih.gov/rest/pug_view/data/compound/" + id
					+ "/JSON?heading=Record+Title";
			try
			{
				JSONObject titleJson = new JSONObject(
						IOUtils.toString(new URL(titleUrl), Charsets.UTF_8));
				titleJson = titleJson.getJSONObject("Record");
				titleJson = titleJson.getJSONArray("Section").getJSONObject(0);
				titleJson = titleJson.getJSONArray("Section").getJSONObject(0);
				JSONArray info = titleJson.getJSONArray("Information");
				for (int i = 0; i < info.length(); i++)
				{
					if (info.getJSONObject(i).getString("Name").equals("Record Title"))
					{
						name = StringUtils
								.capitalize(info.getJSONObject(i).getString("StringValue"));
						break;
					}
				}
			}
			catch (JSONException | IOException e)
			{
				throw new RuntimeException(e);
			}
		}

		@Override
		protected String getLinkout()
		{
			return "https://pubchem.ncbi.nlm.nih.gov/compound/" + id;
		}

		@Override
		protected String getServiceName()
		{
			return "PubChem";
		}

		@Override
		protected String getIdName()
		{
			return "CID";
		}
	}

	public static class ChEMBLCompoundInfo extends CompoundInfo
	{

		public ChEMBLCompoundInfo(String smiles)
				throws MalformedURLException, JSONException, IOException
		{
			super(smiles);
		}

		@Override
		public String getJsonUrl(String encodedSmiles)
		{
			//			return "https://www.ebi.ac.uk/chembl/api/data/molecule/" + encodedSmiles
			//					+ "?format=json";
			return "https://www.ebi.ac.uk/chembl/api/data/similarity/" + encodedSmiles
					+ "/100?format=json";
		}

		@Override
		protected void parseJson(JSONObject obj) throws JSONException
		{
			JSONArray array = obj.getJSONArray("molecules");
			if (array.length() > 0)
			{
				obj = array.getJSONObject(0);
				id = obj.getString("molecule_chembl_id");
				name = obj.getString("pref_name");
			}
		}

		@Override
		protected String getServiceName()
		{
			return "ChEMBL";
		}

		@Override
		protected String getIdName()
		{
			return "ID";
		}

		@Override
		protected String getLinkout()
		{
			return "https://www.ebi.ac.uk/chembldb/index.php/compound/inspect/" + id;
		}
	}

	protected String id;
	protected String name;
	protected HashMap<String, String> fields = new LinkedHashMap<String, String>();

	public CompoundInfo(String smiles)
	{
		try
		{
			JSONObject obj = new JSONObject(IOUtils.toString(
					new URL(getJsonUrl(URLEncoder.encode(smiles, "UTF-8"))), Charsets.UTF_8));
			parseJson(obj);
		}
		catch (UnknownHostException e)
		{
			System.err.println(
					"Compound info fetching failed - cannot conect to host : " + e.getMessage());
		}
		catch (FileNotFoundException e)
		{
			System.err.println("Compound info fetching failed - no compound found for smiles "
					+ smiles + " : " + e.getMessage());
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public String getHTML()
	{
		try
		{
			HtmlCanvas html = new HtmlCanvas();
			html.macros().stylesheet(DefaultHtml.css);
			html.body(HtmlAttributesFactory.class_("pubchem"));
			renderOn(html);
			html._body();
			return html.toHtml();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public void renderOn(HtmlCanvas html) throws IOException
	{
		html.div();
		html.write(getServiceName() + " ");
		html.a(HtmlAttributesFactory.href(getLinkout()).target("_blank"));
		html.img(HtmlAttributesFactory.src("/img/iconExternalLink.gif"));
		html._a();
		html.br();

		html.write(getIdName() + ": ");
		html.write(id + " ");
		html.br();

		String nameStr = name != null ? name : "";
		if (nameStr.length() > 40)
			nameStr = name.substring(0, 38) + "..";
		html.write("Name: " + nameStr);
		for (String k : fields.keySet())
		{
			html.br();
			html.write(k + ": " + fields.get(k));
		}
		html._div();
	}

	public static void main(String[] args) throws MalformedURLException, JSONException, IOException
	{
		String smiles = "CCc1c2cc(ccc2nc-3c1Cn4c3cc5c(c4=O)COC(=O)[C@@]5(CC)O)OC(=O)N6CCC(CC6)[NH+]7CCCCC7";
		System.out.println(new PubChemCompoundInfo(smiles).getHTML());
		//System.out.println(new ChEMBLCompoundInfo(smiles).getHTML());
	}
}
