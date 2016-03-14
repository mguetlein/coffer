package org.kramerlab.cfpservice.api.impl.provider;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.kramerlab.cfpservice.api.ModelService;
import org.kramerlab.cfpservice.api.ServiceObj;

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

	public void writeTo(T t, Class<?> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
			OutputStream entityStream) throws IOException, WebApplicationException
	{
		StringBuilder sb = new StringBuilder();
		if (t.getClass().isArray() && t instanceof ServiceObj[])
			for (ServiceObj element : (ServiceObj[]) t)
				sb.append(element.getURI() + "\n");
		else
			sb.append("configure uri list provider for " + t + " class: " + t.getClass());
		entityStream.write(sb.toString().getBytes("UTF8"));
	}
}