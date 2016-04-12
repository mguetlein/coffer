package org.kramerlab.coffer.api.impl.provider;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.eclipse.persistence.jaxb.rs.MOXyJsonProvider;
import org.kramerlab.coffer.api.ModelService;
import org.kramerlab.coffer.api.impl.ModelServiceImpl;
import org.kramerlab.coffer.api.impl.ot.CompoundImpl;
import org.kramerlab.coffer.api.impl.ot.FragmentImpl;
import org.kramerlab.coffer.api.impl.ot.ModelImpl;
import org.kramerlab.coffer.api.impl.ot.PredictionImpl;
import org.kramerlab.coffer.api.ot.OpenToxCompound;
import org.kramerlab.coffer.api.ot.OpenToxFragment;
import org.kramerlab.coffer.api.ot.OpenToxModel;
import org.kramerlab.coffer.api.ot.OpenToxPrediction;

@Provider
@Produces(MediaType.APPLICATION_JSON)
public class JsonProvider extends MOXyJsonProvider
{
	public JsonProvider()
	{
		setFormattedOutput(true);
		HashMap<String, String> map = new HashMap<>();
		map.put(ModelService.OPENTOX_API, ModelService.OPENTOX_API_PREFIX);
		map.put(ModelService.DC_NAMESPACE, ModelService.DC_PREFIX);
		map.put(ModelService.RDF_NAMESPACE, ModelService.RDF_PREFIX);
		setNamespacePrefixMapper(map);
	}

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations,
			MediaType mediaType)
	{
		return true;
	}

	@Context
	private HttpHeaders headers;

	@Override
	public void writeTo(Object object, Class<?> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
			OutputStream entityStream) throws IOException, WebApplicationException
	{
		if (ModelServiceImpl.HOST == null)
			ModelServiceImpl.HOST = "http://" + headers.getHeaderString(HttpHeaders.HOST);

		// TODO: hack, solve this otherwise
		if (object instanceof CompoundImpl)
			genericType = OpenToxCompound.class;
		if (object instanceof ModelImpl)
			genericType = OpenToxModel.class;
		if (object instanceof PredictionImpl)
			genericType = OpenToxPrediction.class;
		if (object instanceof FragmentImpl)
			genericType = OpenToxFragment.class;

		super.writeTo(object, type, genericType, annotations, mediaType, httpHeaders, entityStream);
	}

}
