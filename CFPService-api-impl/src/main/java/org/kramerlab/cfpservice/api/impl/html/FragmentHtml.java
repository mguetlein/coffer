package org.kramerlab.cfpservice.api.impl.html;

import java.util.Set;

import org.kramerlab.cfpservice.api.ModelService;
import org.kramerlab.cfpservice.api.impl.Fragment;
import org.kramerlab.cfpservice.api.impl.Model;
import org.mg.cdklib.cfp.CFPFragment;
import org.mg.cdklib.cfp.CFPMiner;
import org.mg.javalib.datamining.ResultSet;
import org.mg.javalib.util.StringUtil;
import org.openscience.cdk.exception.CDKException;

public class FragmentHtml extends DefaultHtml
{
	int selectedAttributeIdx;
	String modelId;
	String fragment;
	CFPMiner miner;
	int maxNumFragments;
	String smiles;

	public FragmentHtml(Fragment f)
	{
		super(f.getModelId(), Model.getName(f.getModelId()), "fragment/" + f.getId(), "Fragment");
		this.miner = Model.find(f.getModelId()).getCFPMiner();
		this.modelId = f.getModelId();
		fragment = "Fragment " + f.getId() + " / " + miner.getNumFragments();
		setPageTitle(fragment);
		this.selectedAttributeIdx = Integer.parseInt(f.getId()) - 1;
		this.smiles = f.getSmiles();
		this.maxNumFragments = f.getMaxNumFragments();
		//		System.out.println(miner.getFragmentViaIdx(selectedAttributeIdx));

	}

	private boolean instanceContains(int instIdx, int attIdx)
	{
		return miner.getFragmentsForCompound(instIdx).contains(miner.getFragmentViaIdx(attIdx));
	}

	public String build()
	{
		try
		{
			//		setHidePageTitle(true);
			//		newSection("Occurence of " + fragment + " in dataset " + Model.getName(modelId));

			addParagraph(
					encodeLink("/" + modelId + "/fragment/" + (selectedAttributeIdx + 0), "prev")
							+ " "
							+ encodeLink("/" + modelId + "/fragment/" + (selectedAttributeIdx + 2),
									"next"));

			newSection("Occurence in dataset " + Model.getName(modelId));

			ResultSet set = new ResultSet();
			int rIdx = set.addResult();
			set.setResultValue(rIdx, "", "All compounds");
			int present = miner
					.getCompoundsForFragment(miner.getFragmentViaIdx(selectedAttributeIdx)).size();
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
					if (instanceContains(i, selectedAttributeIdx)
							&& miner.getEndpoints().get(i).equals(clazz))
					{
						cIdx++;
						if (cIdx > maxNumFragments)
							continue;

						rIdx = set.addResult();
						set.setResultValue(rIdx, "", (rIdx + 1));
						String smiles = miner.getTrainingDataSmiles().get(i);
						int atoms[] = null;
						miner.getAtoms(smiles, miner.getFragmentViaIdx(selectedAttributeIdx));
						String img = depictMatch(smiles, atoms, miner.getCFPType().isECFP(), null,
								false, maxMolPicSize);
						String href = depictMatch(smiles, atoms, miner.getCFPType().isECFP(), null,
								false, -1);
						//set.setResultValue(rIdx, "idx", i + "");
						set.setResultValue(rIdx, "'" + clazz + "' compounds",
								getImage(img, href, false));
						//					//set.setResultValue(rIdx, "Class", trainingData.get(i).stringValue(trainingData.classAttribute()));
					}

				}
				if (first)
					startLeftColumn();
				else
					startRightColumn();
				first = false;
				if (cIdx > maxNumFragments)
				{
					rIdx = set.addResult();
					set.setResultValue(rIdx,
							"'" + clazz
									+ "' compounds",
							encodeLink(
									(selectedAttributeIdx + 1) + "?size="
											+ Math.min(
													maxNumFragments
															+ ModelService.DEFAULT_NUM_ENTRIES,
													cIdx)
											+ "#" + (rIdx + 1),
									"More compounds"));
				}
				addTable(set);
			}
			stopColumns();

			addGap();
			if (smiles != null)
			{
				newSection("Matching compound: " + smiles);
				int atoms[] = miner.getAtoms(smiles, miner.getFragmentViaIdx(selectedAttributeIdx));
				Image img;
				if (atoms != null)
					img = getImage(depictMatch(smiles, atoms, miner.getCFPType().isECFP(), null,
							false, maxMolPicSize));
				else
					img = getImage(depict(smiles, maxMolPicSize), depict(smiles, -1), false);
				set = new ResultSet();
				rIdx = set.addResult();
				set.setResultValue(rIdx, "Match",
						(atoms != null && atoms.length > 0 ? "Yes" : "-"));
				set.setResultValue(rIdx, "Compound", img);
				addTable(set);
				addGap();
			}
			newSection("Sub and super fragments");
			setTableRowsAlternating(false);
			CFPFragment frag = miner.getFragmentViaIdx(selectedAttributeIdx);
			for (Boolean sub : new Boolean[] { true, false })
			{
				Set<CFPFragment> frags;
				if (sub)
					frags = miner.getSubFragments(frag);
				else
					frags = miner.getSuperFragments(frag);
				set = new ResultSet();
				int cIdx = 0;
				if (frags != null)
				{
					for (CFPFragment fragment : frags)
					{
						int attIdx = miner.getIdxForFragment(fragment);
						int atoms[] = null;
						String smi = "";
						if (smiles != null)
						{
							smi = smiles;
							atoms = miner.getAtoms(smi, fragment);
						}
						if (atoms == null)
						{
							int cmpIdx = miner
									.getCompoundsForFragment(miner.getFragmentViaIdx(attIdx))
									.iterator().next();
							smi = miner.getTrainingDataSmiles().get(cmpIdx);
							atoms = miner.getAtoms(smi, fragment);
						}

						cIdx++;
						if (cIdx > maxNumFragments)
							continue;

						String dep = depictMatch(smi, atoms, miner.getCFPType().isECFP(), null,
								true, croppedPicSize);
						rIdx = set.addResult();
						set.setResultValue(rIdx, "", (rIdx + 1));
						if (smiles != null && smi.equals(smiles))
						{
							boolean inc;
							if (sub)
							{
								Set<CFPFragment> incl = miner.getIncludedFragments(frag, smi);
								inc = incl != null && incl.contains(fragment);
							}
							else
							{
								Set<CFPFragment> incl = miner.getIncludedFragments(fragment, smi);
								inc = incl != null && incl.contains(frag);
							}
							if (inc)
								setHeaderHelp("Match",
										"Yes (Exclusive): this sub/super fragment and the fragment always match the compound at the same location/s. I.e., the smaller fragment does not match elsewhere separately.");
							set.setResultValue(rIdx, "Match", (inc ? "Yes (Exclusive)" : "Yes"));
						}
						else if (smiles != null)
							set.setResultValue(rIdx, "Match", "-");
						set.setResultValue(rIdx,
								(sub ? "Sub" : "Super")
										+ " Fragment",
								getImage(dep,
										"/" + modelId + "/fragment/" + (attIdx + 1)
												+ ((smiles != null
														? "?smiles="
																+ StringUtil.urlEncodeUTF8(smiles)
														: "")),
										true));
					}
					if (cIdx > maxNumFragments)
					{
						rIdx = set.addResult();
						set.setResultValue(rIdx, (sub ? "Sub" : "Super") + " Fragment",
								encodeLink(
										(selectedAttributeIdx + 1) + "?size="
												+ Math.min(
														maxNumFragments
																+ ModelService.DEFAULT_NUM_ENTRIES,
														cIdx)
												+ "#" + (rIdx + 1),
										"More compounds"));
					}
				}

				setTableColWidthLimited(true);
				if (sub)
					startLeftColumn();
				else
					startRightColumn();
				addTable(set);
			}
			stopColumns();

			return close();
		}
		catch (CDKException e)
		{
			throw new RuntimeException(e);
		}
	}

}
