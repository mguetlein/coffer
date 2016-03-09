package org.kramerlab.cfpservice.api.impl.util;

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

import org.kramerlab.cfpservice.api.impl.Model;
import org.kramerlab.cfpservice.api.impl.Prediction;

@Provider
@Produces(MediaType.TEXT_HTML)
public class HTMLBodyWriter<T> implements MessageBodyWriter<T>
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
		if (t instanceof String)
			sb.append(t);
		else if (t instanceof HTMLProvider)
			sb.append(((HTMLProvider) t).getHTML());
		else if (t.getClass().isArray())
		{
			if (t instanceof Model[])
				sb.append(Model.getModelListHTML((Model[]) t));
			else if (t instanceof Prediction[])
				sb.append(Prediction.getPredictionListHTML((Prediction[]) t));
			else
				sb.append("configure html provider for array of class: " + t.getClass());
		}
		else
			sb.append("configure html provider for class: " + t.getClass());
		entityStream.write(sb.toString().getBytes("UTF8"));
	}
}
