package org.kramerlab.cfpservice.api.impl.html;

import org.kramerlab.cfpminer.CFPMiner;
import org.kramerlab.cfpservice.api.impl.Fragment;
import org.kramerlab.cfpservice.api.impl.Model;
import org.mg.javalib.datamining.ResultSet;

public class FragmentHtml extends DefaultHtml
{
	int selectedAttributeIdx;
	String modelId;
	String fragment;
	CFPMiner miner;

	public FragmentHtml(Fragment f, String maxNumCompounds)
	{
		super(f.getModelId(), Model.getName(f.getModelId()), "fragment/" + f.getId(), "Fragment");
		this.miner = Model.find(f.getModelId()).getCFPMiner();
		this.modelId = f.getModelId();
		fragment = "Fragment " + f.getId() + " / " + miner.getNumFragments();
		setPageTitle(fragment);
		this.selectedAttributeIdx = Integer.parseInt(f.getId()) - 1;
		parseMaxNumElements(maxNumCompounds);
	}

	private boolean instanceContains(int instIdx, int attIdx) throws Exception
	{
		return miner.getFragmentsForCompound(instIdx).contains(miner.getFragmentViaIdx(attIdx));
	}

	public String build() throws Exception
	{
		//		setHidePageTitle(true);
		//		newSection("Occurence of " + fragment + " in dataset " + Model.getName(modelId));

		addParagraph(encodeLink("/" + modelId + "/fragment/" + (selectedAttributeIdx + 0), "prev") + " "
				+ encodeLink("/" + modelId + "/fragment/" + (selectedAttributeIdx + 2), "next"));

		newSection("Occurence in dataset " + Model.getName(modelId));

		ResultSet set = new ResultSet();
		int rIdx = set.addResult();
		set.setResultValue(rIdx, "", "All compounds");
		int present = miner.getCompoundsForFragment(miner.getFragmentViaIdx(selectedAttributeIdx)).size();
		set.setResultValue(rIdx, "Total", miner.getNumCompounds());
		set.setResultValue(rIdx, "Present", present);
		set.setResultValue(rIdx, "Absent", miner.getNumCompounds() - present);
		for (String clazz : miner.getClassValues())
		{
			rIdx = set.addResult();
			set.setResultValue(rIdx, "", "'" + clazz + "' compounds");
			present = 0;
			int absent = 0;
			for (int i = 0; i < miner.getNumCompounds(); i++)
				if (miner.getEndpoints().get(i).equals(clazz))
					if (instanceContains(i, selectedAttributeIdx))
						present++;
					else
						absent++;
			set.setResultValue(rIdx, "Total", present + absent);
			set.setResultValue(rIdx, "Present", present);
			set.setResultValue(rIdx, "Absent", absent);
		}
		addTable(set);

		addGap();
		newSection("Compounds including the fragment");

		boolean first = true;
		setTableRowsAlternating(false);
		for (String clazz : miner.getClassValues())
		{
			int cIdx = 0;
			System.err.println(clazz);
			set = new ResultSet();
			for (int i = 0; i < miner.getNumCompounds(); i++)
			{
				if (instanceContains(i, selectedAttributeIdx) && miner.getEndpoints().get(i).equals(clazz))
				{
					cIdx++;
					if (cIdx > maxNumElements)
						continue;

					rIdx = set.addResult();
					set.setResultValue(rIdx, "No.", (rIdx + 1));
					String smiles = miner.getTrainingDataSmiles().get(i);
					int atoms[] = miner.getAtoms(smiles, miner.getFragmentViaIdx(selectedAttributeIdx));
					String img = depictMatch(smiles, atoms, miner.getCFPType().isECFP(), null, false, maxMolPicSize);
					String href = depictMatch(smiles, atoms, miner.getCFPType().isECFP(), null, false, -1);
					//set.setResultValue(rIdx, "idx", i + "");
					set.setResultValue(rIdx, "'" + clazz + "' compounds", getImage(img, href, false));
					//					//set.setResultValue(rIdx, "Class", trainingData.get(i).stringValue(trainingData.classAttribute()));
				}

			}
			if (first)
				startLeftColumn();
			else
				startRightColumn();
			first = false;
			if (cIdx > maxNumElements)
			{
				rIdx = set.addResult();
				set.setResultValue(
						rIdx,
						"'" + clazz + "' compounds",
						encodeLink(
								(selectedAttributeIdx + 1) + "?size="
										+ Math.min(maxNumElements + defaultMaxNumElements, cIdx) + "#" + (rIdx + 1),
								"More compounds"));
			}
			addTable(set);
		}
		stopColumns();

		return close();
	}
}
