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

	public FragmentHtml(Fragment f)
	{
		super(f.getModelId(), Model.getName(f.getModelId()), "fragment/" + f.getId(), "Fragment");
		this.miner = Model.find(f.getModelId()).getCFPMiner();
		this.modelId = f.getModelId();
		fragment = "Fragment " + f.getId() + " / " + miner.getNumAttributes();
		setPageTitle(fragment);
		this.selectedAttributeIdx = Integer.parseInt(f.getId()) - 1;
	}

	private boolean instanceContains(int instIdx, int attIdx) throws Exception
	{
		return miner.getHashcodesForCompound(instIdx).contains(miner.getHashcodeViaIdx(attIdx));
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
		int present = miner.getCompoundsForHashcode(miner.getHashcodeViaIdx(selectedAttributeIdx)).size();
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
			System.err.println(clazz);
			set = new ResultSet();
			for (int i = 0; i < miner.getNumCompounds(); i++)
			{
				if (instanceContains(i, selectedAttributeIdx) && miner.getEndpoints().get(i).equals(clazz))
				{
					rIdx = set.addResult();
					String smiles = miner.getTrainingDataSmiles().get(i);
					int atoms[] = miner.getAtoms(smiles, miner.getHashcodeViaIdx(selectedAttributeIdx));
					String img = imageProvider.drawCompoundWithFP(smiles, atoms, miner.getCFPType().isECFP(), false,
							molPicSize);
					String href = imageProvider.hrefCompoundWithFP(smiles, atoms, miner.getCFPType().isECFP());
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
			addTable(set);
		}
		stopColumns();

		return close();
	}
}
