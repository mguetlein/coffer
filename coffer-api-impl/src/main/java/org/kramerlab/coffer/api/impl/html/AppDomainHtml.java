package org.kramerlab.coffer.api.impl.html;

import java.util.List;

import org.kramerlab.cfpminer.appdomain.ADInfoModel;
import org.kramerlab.cfpminer.appdomain.ADNeighbor;
import org.kramerlab.cfpminer.appdomain.KNNTanimotoCFPAppDomainModel;
import org.kramerlab.coffer.api.ModelService;
import org.kramerlab.coffer.api.impl.objects.AbstractModel;
import org.kramerlab.coffer.api.objects.Model;
import org.mg.cdklib.cfp.CFPMiner;
import org.mg.javalib.datamining.ResultSet;
import org.mg.javalib.util.StringUtil;

public class AppDomainHtml extends DefaultHtml
{
	Model m;
	String smiles;
	int maxNumNeighbors;

	private static ADInfoModel DEFAULT_AD_MODEL = new KNNTanimotoCFPAppDomainModel(3, true);

	public static String getDocumentation()
	{
		return DEFAULT_AD_MODEL.getDocumentation();
	}

	public static String getGeneralInfo()
	{
		return DEFAULT_AD_MODEL.getGeneralInfo(true);
	}

	public AppDomainHtml(Model m, String smiles, int maxNumNeighbors)
	{
		super("Applicability domain for " + m.getTarget() + " \u2500 " + m.getName(), m.getId(),
				m.getName(), "appdomain", "AppDomain");
		//		System.out.println(miner.getFragmentViaIdx(selectedAttributeIdx));
		this.m = m;
		this.smiles = smiles;
		this.maxNumNeighbors = maxNumNeighbors;
	}

	public String build()
	{
		//		setHidePageTitle(true);
		//		newSection("Occurence of " + fragment + " in dataset " + Model.getName(modelId));

		ADInfoModel a = ((AbstractModel) m).getAppDomain();
		addMouseoverHelp(a.getGeneralInfo(true) + " " + moreLink(DocHtml.APP_DOMAIN),
				a.getGeneralInfo(false));
		addGap();
		addGap();
		addMouseoverHelp(a.getDistanceInfo(true) + " " + moreLink(DocHtml.APP_DOMAIN),
				a.getDistanceInfo(false));
		addGap();

		if (smiles == null)
		{
			addGap();

			//, "/doc#" + DocHtml.getAnker(DocHtml.APP_DOMAIN),
			//	StringUtil.formatDouble(a.getMeanTrainingDistance())));
			addImage(getImage("/" + m.getId() + "/depictAppdomain"));

			addGap();
			addGap();
			addForm("/" + m.getId() + "/appdomain", ModelService.PREDICT_PARAM_COMPOUND_SMILES,
					"Check applicability domain", "Please insert SMILES string");
		}
		else
		{
			CFPMiner miner = ((AbstractModel) m).getCFPMiner();
			a.setCFPMiner(miner);

			//			newSubsection("Query compound is " + (inside ? "INSIDE" : "OUTSIDE"));

			addGap();
			ResultSet rs = new ResultSet();
			rs.addResult();
			rs.setResultValue(0, "Query compound", getImage(depict(smiles, maxMolPicSize)));
			rs.setResultValue(0, "Applicability Domain",
					a.isInsideAppdomain(smiles).toNiceString().toUpperCase());
			//						rs.setResultValue(0, "App-Domain",
			//								getInsideAppDomain(a.isInsideAppdomain(smiles), a.pValue(smiles), null));
			addTable(rs);

			addGap();
			addMouseoverHelp(
					a.getPredictionDistanceInfo(smiles, true) + " " + moreLink(DocHtml.APP_DOMAIN),
					a.getPredictionDistanceInfo(smiles, false));
			addGap();
			addGap();

			addMouseoverHelp(
					a.getPredictionRationalInfo(smiles, true) + " " + moreLink(DocHtml.APP_DOMAIN),
					a.getPredictionRationalInfo(smiles, false));
			addGap();
			addGap();

			addImage(getImage("/" + m.getId() + "/depictAppdomain?smiles="
					+ StringUtil.urlEncodeUTF8(smiles)));

			newSubsection("Nearest neigbors");
			addMouseoverHelp(a.getNeighborInfo(true) + " " + moreLink(DocHtml.APP_DOMAIN),
					a.getNeighborInfo(false));
			addGap();
			addGap();
			List<ADNeighbor> l = a.getNeighbors(smiles);
			int nIdx = 0;
			ResultSet set = new ResultSet();
			for (; nIdx < l.size(); nIdx++)
			{
				if (nIdx >= maxNumNeighbors)
					continue;
				int rIdx = set.addResult();
				set.setResultValue(rIdx, "", (rIdx + 1));
				String smiles = l.get(nIdx).getSmiles();
				String img = depict(smiles, maxMolPicSize);
				//String href = depict(smiles, -1);
				String href = "/compound/" + StringUtil.urlEncodeUTF8(smiles);
				set.setResultValue(rIdx, "Distance", l.get(nIdx).getDistance());
				set.setResultValue(rIdx, "Neighbors", getImage(img, href, false));
			}
			if (l.size() > maxNumNeighbors)
			{
				int rIdx = set.addResult();
				set.setResultValue(rIdx, "Neighbors",
						encodeLink("?smiles=" + StringUtil.urlEncodeUTF8(smiles) + "&size="
								+ Math.min(maxNumNeighbors + ModelService.DEFAULT_NUM_ENTRIES, nIdx)
								+ "#" + (rIdx + 1), "More neighbors"));
			}
			addTable(set);
		}

		return close();
	}

}
