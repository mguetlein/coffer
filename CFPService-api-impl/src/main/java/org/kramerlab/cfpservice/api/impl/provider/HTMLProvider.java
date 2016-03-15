package org.kramerlab.cfpservice.api.impl.provider;

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

import org.kramerlab.cfpservice.api.impl.ModelServiceImpl;
import org.kramerlab.cfpservice.api.impl.objects.AbstractModel;
import org.kramerlab.cfpservice.api.impl.objects.AbstractPrediction;

@Provider
@Produces(MediaType.TEXT_HTML)
public class HTMLProvider<T> implements MessageBodyWriter<T>
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
			ModelServiceImpl.HOST = "http://" + headers.getHeaderString(HttpHeaders.HOST);

		StringBuilder sb = new StringBuilder();
		if (t instanceof String)
			sb.append(t);
		else if (t instanceof HTMLOwner)
			sb.append(((HTMLOwner) t).getHTML());
		else if (t.getClass().isArray())
		{
			if (t instanceof AbstractModel[])
				sb.append(AbstractModel.getModelListHTML((AbstractModel[]) t));
			else if (t instanceof AbstractPrediction[])
				sb.append(AbstractPrediction.getPredictionListHTML((AbstractPrediction[]) t));
			else
				sb.append("configure html provider for array of " + t + " class: " + t.getClass());
		}
		else
			sb.append("configure html provider for " + t + " class: " + t.getClass());
		entityStream.write(sb.toString().getBytes("UTF8"));
	}
}
