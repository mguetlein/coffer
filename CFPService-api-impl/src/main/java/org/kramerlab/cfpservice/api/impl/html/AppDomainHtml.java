package org.kramerlab.cfpservice.api.impl.html;

import java.util.List;

import org.kramerlab.cfpminer.appdomain.ADPrediction;
import org.kramerlab.cfpminer.appdomain.CFPAppDomain;
import org.kramerlab.cfpminer.appdomain.CFPAppDomain.Neighbor;
import org.kramerlab.cfpservice.api.ModelService;
import org.kramerlab.cfpservice.api.impl.objects.AbstractModel;
import org.kramerlab.cfpservice.api.objects.Model;
import org.mg.cdklib.cfp.CFPMiner;
import org.mg.javalib.datamining.ResultSet;
import org.mg.javalib.util.StringUtil;

public class AppDomainHtml extends DefaultHtml
{
	Model m;
	String smiles;
	int maxNumNeighbors;

	public AppDomainHtml(Model m, String smiles, int maxNumNeighbors)
	{
		super("Applicability domain of prediction model " + m.getName(), m.getId(), m.getName(),
				"appdomain", "AppDomain");
		//		System.out.println(miner.getFragmentViaIdx(selectedAttributeIdx));
		this.m = m;
		this.smiles = smiles;
		this.maxNumNeighbors = maxNumNeighbors;
	}

	public String build()
	{
		//		setHidePageTitle(true);
		//		newSection("Occurence of " + fragment + " in dataset " + Model.getName(modelId));

		CFPAppDomain a = ((AbstractModel) m).getAppDomain();
		int k = a.getNumNeighbors();
		String avgMeasure = a.getAveragingScheme();
		String helpTextGeneral = text("appdomain.help.general") + " "
				+ moreLink(DocHtml.APP_DOMAIN);
		String helpTextDistance = text("appdomain.help.distance", avgMeasure, k) + " "
				+ moreLink(DocHtml.APP_DOMAIN);

		addMouseoverHelp(helpTextGeneral, text("appdomain.intro.general"));
		addGap();
		addGap();
		addMouseoverHelp(helpTextDistance, text("appdomain.intro.distance"));
		addGap();

		if (smiles == null)
		{
			addGap();
			addParagraph(text("appdomain.dist.training",
					StringUtil.formatDouble(a.getMeanTrainingDistance())));
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

			ADPrediction adPrediction = a.isInsideAppdomain(smiles);
			double dist = a.getDistance(smiles);
			double prob = a.getCumulativeProbability(smiles);

			String helpTextStats = text("appdomain.help.statistics",
					StringUtil.formatSmallDoubles(dist), StringUtil.formatSmallDoubles(prob),
					a.getPThreshold(ADPrediction.PossiblyOutside),
					ADPrediction.PossiblyOutside.toNiceString(),
					a.getPThreshold(ADPrediction.Outside), ADPrediction.Outside.toNiceString())
					+ " " + moreLink(DocHtml.APP_DOMAIN);
			//			newSubsection("Query compound is " + (inside ? "INSIDE" : "OUTSIDE"));

			addGap();
			ResultSet rs = new ResultSet();
			rs.addResult();
			rs.setResultValue(0, "Query compound", getImage(depict(smiles, maxMolPicSize)));
			rs.setResultValue(0, "Applicability Domain", adPrediction.toNiceString().toUpperCase());
			//						rs.setResultValue(0, "App-Domain",
			//								getInsideAppDomain(a.isInsideAppdomain(smiles), a.pValue(smiles), null));
			addTable(rs);

			addGap();
			String msg = text("appdomain.dist.training",
					StringUtil.formatDouble(a.getMeanTrainingDistance())) + " ";
			msg += text("appdomain.dist.query", StringUtil.formatDouble(dist));
			addMouseoverHelp(helpTextDistance, msg);
			addGap();
			addGap();

			msg = "The query compound is " + adPrediction.toNiceString()
					+ " the applicability domain, ";
			msg += text("appdomain.reason." + adPrediction);
			addMouseoverHelp(helpTextStats, msg);
			addGap();
			addGap();

			addImage(getImage("/" + m.getId() + "/depictAppdomain?smiles="
					+ StringUtil.urlEncodeUTF8(smiles)));

			newSubsection("Nearest neigbors");
			addMouseoverHelp(helpTextDistance, text("appdomain.neigbors", k));
			addGap();
			addGap();
			List<Neighbor> l = a.getNeighbors(smiles);
			int nIdx = 0;
			ResultSet set = new ResultSet();
			for (; nIdx < l.size(); nIdx++)
			{
				if (nIdx >= maxNumNeighbors)
					continue;
				int rIdx = set.addResult();
				set.setResultValue(rIdx, "", (rIdx + 1));
				String smiles = l.get(nIdx).smiles;
				String img = depict(smiles, maxMolPicSize);
				//String href = depict(smiles, -1);
				String href = "/compound/" + StringUtil.urlEncodeUTF8(smiles);
				set.setResultValue(rIdx, "Distance", l.get(nIdx).distance);
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
