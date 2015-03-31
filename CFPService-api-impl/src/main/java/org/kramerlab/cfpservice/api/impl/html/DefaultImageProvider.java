package org.kramerlab.cfpservice.api.impl.html;

import java.io.File;

import org.kramerlab.cfpminer.CDKUtil;
import org.mg.javalib.util.ArrayUtil;
import org.mg.javalib.util.StringUtil;
import org.openscience.cdk.interfaces.IAtomContainer;

public class DefaultImageProvider implements ImageProvider
{
	String relativeImgPath = "imgs/";

	public DefaultImageProvider()
	{
	}

	public DefaultImageProvider(String path)
	{
		this.relativeImgPath = path;
	}

	public String drawCompound(String smiles, int size) throws Exception
	{
		IAtomContainer mol = CDKUtil.parseSmiles(smiles);
		String png = relativeImgPath + StringUtil.getMD5(smiles) + "_" + size + ".png";
		CDKUtil.draw(png, mol, size);
		return png;
	}

	public String drawCompoundWithFP(String smiles, int atoms[], boolean crop, int size) throws Exception
	{
		IAtomContainer mol = CDKUtil.parseSmiles(smiles);
		String pngFile = relativeImgPath + StringUtil.getMD5(smiles) + "_"
				+ ArrayUtil.toString(ArrayUtil.toIntegerArray(atoms), "-", "", "", "") + "_" + crop + "_" + size
				+ ".png";
		if (!new File(pngFile).exists())
			CDKUtil.drawFP(pngFile, mol, atoms, crop, size);
		return pngFile;
	}

	public String hrefFragment(String modelName, int fp)
	{
		return null;
	}

	public String hrefModel(String modelName)
	{
		return null;
	}

	public String hrefCompound(String smiles)
	{
		return null;
	}

	public String hrefCompoundWithFP(String smiles, int[] atoms)
	{
		return null;
	}

	public static void main(String[] args) throws Exception
	{
		new DefaultImageProvider("/tmp/").drawCompound("c1ccccc1.Cl", 100);
	}

}
