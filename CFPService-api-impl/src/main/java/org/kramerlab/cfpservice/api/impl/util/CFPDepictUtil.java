package org.kramerlab.cfpservice.api.impl.util;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.kramerlab.cfpservice.api.impl.DepictService;
import org.kramerlab.cfpservice.api.impl.objects.AbstractModel;
import org.kramerlab.cfpservice.api.objects.Model;
import org.kramerlab.cfpservice.api.objects.Prediction;
import org.kramerlab.cfpservice.api.objects.SubgraphPredictionAttribute;
import org.mg.cdklib.AtomContainerUtil;
import org.mg.cdklib.cfp.CFPFragment;
import org.mg.cdklib.cfp.CFPMiner;
import org.mg.cdklib.depict.CDKDepict;
import org.mg.javalib.gui.property.ColorGradient;
import org.mg.javalib.util.ArrayUtil;
import org.mg.wekalib.attribute_ranking.PredictionAttribute;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemObject;

public class CFPDepictUtil
{
	public static void depictMultiMatchToPNG(String pngFile, IAtomContainer mol, Prediction p,
			Model m, int maxSize) throws Exception
	{
		depictMultiMatchToPNG(pngFile, mol, ((AbstractModel) m).getCFPMiner(),
				p.getPredictedDistribution(), p.getPredictionAttributes(), maxSize);
	}

	static void depictMultiMatchToPNG(String pngFile, IAtomContainer mol, CFPMiner miner,
			double dist[], List<? extends SubgraphPredictionAttribute> pAtt, int maxSize)
					throws Exception
	{
		setAtomBondWeights(mol, miner, pAtt, dist);
		Color cols[] = weightsToColorGradient(mol, new ColorGradient(DepictService.ACTIVE_BRIGHT,
				DepictService.NEUTRAL_BRIGHT, DepictService.INACTIVE_BRIGHT));
		CDKDepict.depictMatchToPNG(pngFile, mol, cols, false, maxSize);
	}

	private static String WEIGHT_PROP = "weightProp";

	/**
	 * convert WEIGHT_PROP property to color
	 * this is done using a color gradient
	 * COLOR_PROP will contain indices for resulting color[] array
	 * 
	 * @param mol
	 * @param gradient
	 * @return
	 */
	private static Color[] weightsToColorGradient(IAtomContainer mol, ColorGradient gradient)
	{
		List<Color> palette = new ArrayList<Color>();
		for (IChemObject c : AtomContainerUtil.getAtomsAndBonds(mol))
		{
			double w = c.getProperty(WEIGHT_PROP, Double.class);
			Color col = gradient.getColor(w);
			c.setProperty(CDKDepict.COLOR_PROP, palette.size());
			palette.add(col);
		}
		return ArrayUtil.toArray(palette);
	}

	/**
	 * sets weights in each in atom/bond WEIGHT_PROP property
	 * weight is between [0,1], 0.5 corresponds to neutral, 0 is de-activating, 1 is activating 
	 * 
	 * @param mol
	 * @param cfp
	 * @param att
	 * @param dist
	 */
	private static void setAtomBondWeights(IAtomContainer mol, CFPMiner cfp,
			List<? extends PredictionAttribute> att, double dist[])
	{
		try
		{
			// step 1: sum up prop-diffs
			for (IChemObject c : AtomContainerUtil.getAtomsAndBonds(mol))
				c.setProperty(WEIGHT_PROP, 0.0);
			double propActive = dist[cfp.getActiveIdx()];
			// iterate over all attributes
			for (PredictionAttribute a : att)
			{
				CFPFragment f = cfp.getFragmentViaIdx(a.getAttribute());
				Set<Integer> atoms = cfp.getAtomsMultiple(mol, f);
				if (atoms != null && atoms.size() > 0)
				{
					// use only matching attributes
					double altPropActive = a.getAlternativeDistributionForInstance()[cfp
							.getActiveIdx()];
					double weight = propActive - altPropActive;

					for (int i = 0; i < mol.getAtomCount(); i++)
					{
						IAtom atom = mol.getAtom(i);
						if (atoms.contains(i))
							atom.setProperty(WEIGHT_PROP,
									atom.getProperty(WEIGHT_PROP, Double.class) + weight);
					}
					for (int i = 0; i < mol.getBondCount(); i++)
					{
						IBond bond = mol.getBond(i);
						boolean matchingOne = false;
						boolean matchingAll = true;
						for (int j = 0; j < bond.getAtomCount(); j++)
							if (atoms.contains(mol.getAtomNumber(bond.getAtom(j))))
								matchingOne = true;
							else
								matchingAll = false;
						if ((cfp.getCFPType().isECFP() && matchingOne) || matchingAll)
							bond.setProperty(WEIGHT_PROP,
									bond.getProperty(WEIGHT_PROP, Double.class) + weight);
					}
				}
			}
			//step 2: normalize
			double maxAbs = 0;
			for (IChemObject c : AtomContainerUtil.getAtomsAndBonds(mol))
				maxAbs = Math.max(maxAbs, Math.abs(c.getProperty(WEIGHT_PROP, Double.class)));
			if (maxAbs == 0)
				for (IChemObject c : AtomContainerUtil.getAtomsAndBonds(mol))
					c.setProperty(WEIGHT_PROP, 0.5);
			else
				for (IChemObject c : AtomContainerUtil.getAtomsAndBonds(mol))
				{
					double w = c.getProperty(WEIGHT_PROP, Double.class);
					//				System.out.println(c.toString() + "\nweight " + w);
					w /= maxAbs; // normalize to [-1,0,1]
					//				System.out.println("normalized " + w);
					w = w / 2.0 + 0.5; // normalize to [0, 0.5, 1]
					//				System.out.println("scaled " + w);
					c.setProperty(WEIGHT_PROP, w);
				}
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

}
