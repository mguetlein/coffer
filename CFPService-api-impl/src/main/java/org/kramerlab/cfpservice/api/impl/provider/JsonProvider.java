package org.kramerlab.cfpservice.api.impl.provider;

import java.util.HashMap;

import org.eclipse.persistence.jaxb.rs.MOXyJsonProvider;
import org.kramerlab.cfpservice.api.ModelService;

public class JsonProvider extends MOXyJsonProvider
{
	public JsonProvider()
	{
		setFormattedOutput(true);
		HashMap<String, String> map = new HashMap<>();
		map.put(ModelService.OPENTOX_API, ModelService.OPENTOX_API_PREFIX);
		map.put(ModelService.DC_NAMESPACE, ModelService.DC_PREFIX);
		setNamespacePrefixMapper(map);
	}

}
