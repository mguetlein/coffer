package org.kramerlab.coffer.api.impl;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.kramerlab.coffer.api.impl.objects.AbstractModel;
import org.kramerlab.coffer.api.impl.objects.AbstractPrediction;
import org.kramerlab.coffer.api.impl.util.ActiveImageIcon;
import org.kramerlab.coffer.api.impl.util.CFPDepictUtil;
import org.kramerlab.coffer.api.objects.Model;
import org.kramerlab.coffer.api.objects.Prediction;
import org.mg.cdklib.CDKConverter;
import org.mg.cdklib.depict.CDKDepict;
import org.mg.javalib.freechart.FreeChartUtil;
import org.mg.javalib.util.ArrayUtil;
import org.mg.javalib.util.StringUtil;
import org.openscience.cdk.exception.InvalidSmilesException;

public class DepictService
{

	public static String ACTIVE_AS_TEXT = "red";
	public static String INACTIVE_AS_TEXT = "blue";

	public static Color ACTIVE_BRIGHT = new Color(255, 51, 51); // red, a less intense
	public static Color INACTIVE_BRIGHT = new Color(51, 51, 255); // blue, a less intense
	public static Color NEUTRAL_BRIGHT = Color.WHITE;

	public static Color ACTIVE_MODERATE = new Color(255, 102, 102); // light red
	public static Color INACTIVE_MODERATE = new Color(102, 102, 255); // light blue
	public static Color NEUTRAL_MODERATE = Color.LIGHT_GRAY;

	private static String FOLDER = System.getProperty("user.home")
			+ "/results/coffer/persistance/img/";

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
			String pngFile = FOLDER + StringUtil.getMD5(smiles) + "_" + atoms.replaceAll(",", "-")
					+ "_" + highlightOutgoingBonds + "_" + activating + "_" + crop + "_" + size
					+ ".png";
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
						s, true);
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
			String pngFile = FOLDER + StringUtil.getMD5(smiles) + "_" + model + "_" + size + ".png";
			if (!new File(pngFile).exists())
			{
				int s = -1;
				if (size != null)
					s = Integer.parseInt(size);
				Model m = AbstractModel.find(model);
				Prediction p = AbstractPrediction.createPrediction(m, smiles, true);
				CFPDepictUtil.depictMultiMatchToPNG(pngFile, CDKConverter.parseSmiles(smiles), p, m,
						s);
			}
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
		//		System.out.println(DepictService.depictMatch("Cl.c1ccc(CCCCCC(=O)O)cc1", null, "1,2",
		//				"true", "true", "false"));
		//        DepictService.depict("c1ccc(CCCCCC(=O)O)cc1", null, "1,2", "false");
		//        DepictService.depict("c1ccc(CCCCCC(=O)O)cc1", null, null, null);

		//		DefaultImageProvider.drawFP("/tmp/delme.png",
		//				new SmilesParser(SilentChemObjectBuilder.getInstance()).parseSmiles("c1c(CCCCCCCCCCCCCCCC)cccc1"),
		//				new int[] { 1, 2 }, true, 100);

		//depictAppDomain(AbstractModel.find("CPDBAS_Mouse"), "CCC");

		depictMultiMatch("C1=CC(=CC=C1NC(=O)C2=CSC(=C2)[N+](=O)[O-])Cl", null, "AMES");
	}

	public static InputStream depictAppDomain(Model model, String smiles)
	{
		try
		{
			String smi = "";
			if (smiles != null)
				smi = "_" + StringUtil.getMD5(smiles);
			String pngFile = FOLDER + "appDomain" + "_" + model.getId() + smi + ".png";

			if (!new File(pngFile).exists())
			{
				if (smiles != null)
					((AbstractModel) model).getAppDomain()
							.setCFPMiner(((AbstractModel) model).getCFPMiner());
				FreeChartUtil.toPNGFile(pngFile,
						((AbstractModel) model).getAppDomain().getPlot(smiles),
						new Dimension(400 * 16 / 9, 400));
			}
			return new FileInputStream(pngFile);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public static InputStream depictActiveIcon(double probability, ImageIcon helpIcon)
	{
		try
		{
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			ImageIcon ic;
			if (helpIcon != null)
				ic = new ActiveImageIcon(helpIcon, probability);
			else
				ic = new ActiveImageIcon(14, probability);
			ImageIO.write((RenderedImage) ic.getImage(), "png", os);
			return new ByteArrayInputStream(os.toByteArray());
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

}
