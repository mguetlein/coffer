package org.kramerlab.cfpservice.api.impl.html;

public interface ImageProvider
{
	public String drawCompound(String smiles, int size) throws Exception;

	public String drawCompoundWithFP(String smiles, int atoms[], boolean highlightOutgoingBonds, boolean crop, int size)
			throws Exception;

	public String hrefModel(String modelName);

	public String hrefCompound(String smiles) throws Exception;

	public String hrefCompoundWithFP(String smiles, int atoms[], boolean highlightOutgoingBonds) throws Exception;

	public String hrefFragment(String modelName, int fp);
}
