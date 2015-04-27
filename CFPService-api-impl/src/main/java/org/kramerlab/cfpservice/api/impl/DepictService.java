package org.kramerlab.cfpservice.api.impl;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URLEncoder;

import org.kramerlab.cfpservice.api.impl.html.DefaultImageProvider;
import org.kramerlab.cfpservice.api.impl.html.ImageProvider;
import org.mg.javalib.util.ArrayUtil;
import org.openscience.cdk.exception.InvalidSmilesException;

public class DepictService implements ImageProvider
{
	public String drawCompound(String smiles, int size) throws Exception
	{
		String sizeStr = "";
		if (size != -1)
			sizeStr = "&size=" + size;
		return "/depict?smiles=" + URLEncoder.encode(smiles, "UTF8") + sizeStr;
	}

	public String hrefCompound(String smiles) throws Exception
	{
		return drawCompound(smiles, -1);
	}

	public String drawCompoundWithFP(String smiles, int[] atoms, boolean highlightOutgoingBonds, boolean crop, int size)
			throws Exception
	{
		String sizeStr = "";
		if (size != -1)
			sizeStr = "&size=" + size;
		String cropStr = "&crop=" + crop;
		if (atoms == null || atoms.length == 0)
			throw new IllegalArgumentException("atoms missing");
		String atomsStr = "&atoms=" + ArrayUtil.toString(ArrayUtil.toIntegerArray(atoms), ",", "", "", "");
		String highlightOutgoingBondsStr = "&highlightOutgoingBonds=" + highlightOutgoingBonds;
		return "/depict?smiles=" + URLEncoder.encode(smiles, "UTF8") + sizeStr + atomsStr + highlightOutgoingBondsStr
				+ cropStr;
	}

	public String hrefCompoundWithFP(String smiles, int[] atoms, boolean highlightOutgoingBonds) throws Exception
	{
		return drawCompoundWithFP(smiles, atoms, highlightOutgoingBonds, false, -1);
	}

	public String hrefModel(String modelName)
	{
		return "/" + modelName;
	}

	public String hrefFragment(String modelName, int fp)
	{
		return "/" + modelName + "/fragment/" + (fp + 1);
	}

	public static InputStream depict(String smiles, String size, String atoms, String highlightOutgoingBonds,
			String crop)
	{
		try
		{
			int s = -1;
			if (size != null)
				s = Integer.parseInt(size);
			if (atoms != null)
			{
				int a[] = ArrayUtil.toPrimitiveIntArray(ArrayUtil.parseIntegers(atoms.split(",")));
				boolean h = (highlightOutgoingBonds != null) && highlightOutgoingBonds.equals("true");
				boolean c = (crop != null) && crop.equals("true");
				return new FileInputStream(new DefaultImageProvider("persistance/img/").drawCompoundWithFP(smiles, a,
						h, c, s));
			}
			else
				return new FileInputStream(new DefaultImageProvider("persistance/img/").drawCompound(smiles, s));
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) throws InvalidSmilesException, Exception
	{
		DepictService.depict("Cl.c1ccc(CCCCCC(=O)O)cc1", null, "1,2", "true", "false");
		//        DepictService.depict("c1ccc(CCCCCC(=O)O)cc1", null, "1,2", "false");
		//        DepictService.depict("c1ccc(CCCCCC(=O)O)cc1", null, null, null);

		//		DefaultImageProvider.drawFP("/tmp/delme.png",
		//				new SmilesParser(SilentChemObjectBuilder.getInstance()).parseSmiles("c1c(CCCCCCCCCCCCCCCC)cccc1"),
		//				new int[] { 1, 2 }, true, 100);

	}
}
