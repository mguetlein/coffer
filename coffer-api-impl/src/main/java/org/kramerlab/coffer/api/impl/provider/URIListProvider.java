package org.kramerlab.coffer.api.impl.provider;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.kramerlab.coffer.api.ModelService;
import org.kramerlab.coffer.api.impl.ModelServiceImpl;
import org.kramerlab.coffer.api.objects.ServiceResource;

@Provider
@Produces(ModelService.MEDIA_TYPE_TEXT_URI_LIST)
public class URIListProvider<T> implements MessageBodyWriter<T>
{
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations,
			MediaType mediaType)
	{
		return true;
	}

	public long getSize(T t, Class<?> type, Type genericType, Annotation[] annotations,
			MediaType mediaType)
	{
		return -1; // deprecated by JAX-RS 2.0 and ignored by Jersey runtime
	}

	@Context
	private HttpHeaders headers;

	public void writeTo(T t, Class<?> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
			OutputStream entityStream) throws IOException, WebApplicationException
	{
		if (ModelServiceImpl.HOST == null)
			ModelServiceImpl.HOST = "http://" + httpHeaders.get(HttpHeaders.HOST);

		StringBuilder sb = new StringBuilder();
		if (t.getClass().isArray() && t instanceof ServiceResource[])
			for (ServiceResource element : (ServiceResource[]) t)
				sb.append(element.getURI() + "\n");
		else
			sb.append("configure uri list provider for " + t + " class: " + t.getClass());
		entityStream.write(sb.toString().getBytes("UTF8"));
	}
}
