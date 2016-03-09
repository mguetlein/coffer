package org.kramerlab.cfpservice.api.impl;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;

import org.kramerlab.cfpservice.api.impl.util.CFPDepictUtil;
import org.mg.cdklib.CDKConverter;
import org.mg.cdklib.depict.CDKDepict;
import org.mg.javalib.util.ArrayUtil;
import org.mg.javalib.util.ColorUtil;
import org.mg.javalib.util.StringUtil;
import org.openscience.cdk.exception.InvalidSmilesException;

public class DepictService
{
	public static Color ACTIVE_BRIGHT = Color.RED;
	public static Color INACTIVE_BRIGHT = Color.BLUE;
	public static Color NEUTRAL_BRIGHT = Color.WHITE;

	public static Color ACTIVE_MODERATE = ColorUtil.transparent(Color.RED, 150);
	public static Color INACTIVE_MODERATE = ColorUtil.transparent(Color.BLUE, 150);
	public static Color NEUTRAL_MODERATE = ColorUtil.transparent(Color.GRAY, 200);

	private static String FOLDER = System.getProperty("user.home")
			+ "/results/cfpservice/persistance/img/";

	public static InputStream depict(String smiles, String size)
	{
		try
		{
			String pngFile = FOLDER + StringUtil.getMD5(smiles) + "_" + size + ".png";
			if (!new File(pngFile).exists())
			{
				int s = -1;
				if (size != null)
					s = Integer.parseInt(size);
				CDKDepict.depictToPNG(pngFile, CDKConverter.parseSmiles(smiles), s);
			}
			return new FileInputStream(pngFile);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public static InputStream depictMatch(String smiles, String size, String atoms,
			String highlightOutgoingBonds, String activating, String crop)
	{
		try
		{
			String pngFile = FOLDER + StringUtil.getMD5(smiles) + "_"
					+ atoms.replaceAll(",", "-") + "_" + highlightOutgoingBonds + "_" + activating
					+ "_" + crop + "_" + size + ".png";
			if (!new File(pngFile).exists())
			{
				int a[] = ArrayUtil.toPrimitiveIntArray(ArrayUtil.parseIntegers(atoms.split(",")));
				boolean h = (highlightOutgoingBonds != null)
						&& highlightOutgoingBonds.equals("true");
				Color col = NEUTRAL_MODERATE;
				if (activating != null && activating.equals("true"))
					col = ACTIVE_MODERATE;
				else if (activating != null && activating.equals("false"))
					col = INACTIVE_MODERATE;
				boolean c = (crop != null) && crop.equals("true");
				int s = -1;
				if (size != null)
					s = Integer.parseInt(size);
				CDKDepict.depictMatchToPNG(pngFile, CDKConverter.parseSmiles(smiles), a, h, col, c,
						s);
			}
			return new FileInputStream(pngFile);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public static InputStream depictMultiMatch(String smiles, String size, String model)
	{
		try
		{
			String pngFile = FOLDER + StringUtil.getMD5(smiles) + "_" + model + "_" + size
					+ ".png";
			//			if (!new File(pngFile).exists())
			//			{

			int s = -1;
			if (size != null)
				s = Integer.parseInt(size);
			Model m = Model.find(model);
			Prediction p = Prediction.createPrediction(m, smiles, true);
			CFPDepictUtil.depictMultiMatchToPNG(pngFile, CDKConverter.parseSmiles(smiles), p, m, s);
			//			}
			return new FileInputStream(pngFile);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public static void deleteAllImagesForModel(final String modelId)
	{
		File files[] = new File(FOLDER).listFiles(new FilenameFilter()
		{
			public boolean accept(File dir, String name)
			{
				return name.contains(modelId);
			}
		});
		for (File f : files)
			f.delete();
	}

	public static void main(String[] args) throws InvalidSmilesException, Exception
	{
		System.out.println(DepictService.depictMatch("Cl.c1ccc(CCCCCC(=O)O)cc1", null, "1,2",
				"true", "true", "false"));
				//        DepictService.depict("c1ccc(CCCCCC(=O)O)cc1", null, "1,2", "false");
				//        DepictService.depict("c1ccc(CCCCCC(=O)O)cc1", null, null, null);

		//		DefaultImageProvider.drawFP("/tmp/delme.png",
		//				new SmilesParser(SilentChemObjectBuilder.getInstance()).parseSmiles("c1c(CCCCCCCCCCCCCCCC)cccc1"),
		//				new int[] { 1, 2 }, true, 100);

	}

}
