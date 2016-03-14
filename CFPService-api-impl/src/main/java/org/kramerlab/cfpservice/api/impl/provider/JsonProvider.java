package org.kramerlab.cfpservice.api.impl.provider;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.eclipse.persistence.jaxb.rs.MOXyJsonProvider;
import org.kramerlab.cfpservice.api.ModelService;
import org.kramerlab.cfpservice.api.ServiceObj;

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

	@Context
	private HttpHeaders headers;

	@Override
	public void writeTo(Object object, Class<?> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
			OutputStream entityStream) throws IOException, WebApplicationException
	{
		if (ServiceObj.HOST == null)
			ServiceObj.HOST = "http://" + headers.getHeaderString(HttpHeaders.HOST);
		super.writeTo(object, type, genericType, annotations, mediaType, httpHeaders, entityStream);
	}

}
