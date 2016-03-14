package org.kramerlab.cfpservice.api.impl.provider;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.kramerlab.cfpservice.api.impl.html.DefaultHtml;

@Provider
public class ErrorHandler implements ExceptionMapper<Throwable>
{
	@Context
	private HttpHeaders headers;

	@Override
	public Response toResponse(Throwable e)
	{
		String errorMsg = e.getMessage() + "\n---\n";
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		e.printStackTrace(new PrintStream(stream));
		errorMsg += stream.toString() + "\n";

		Response.Status status = Response.Status.INTERNAL_SERVER_ERROR;
		if (e instanceof IllegalArgumentException)
			status = Response.Status.BAD_REQUEST;
		String errorTitle = status + " (" + status.getStatusCode() + ")";

		System.err.println(errorTitle);
		System.err.println(errorMsg);

		String type = MediaType.TEXT_PLAIN;

		boolean acceptHtml = false;
		for (MediaType mt : headers.getAcceptableMediaTypes())
			if (mt.isCompatible(MediaType.TEXT_HTML_TYPE))
				acceptHtml = true;
		if (acceptHtml)
		{
			DefaultHtml html = new DefaultHtml(null, null, null, null);
			html.setPageTitle(errorTitle);
			html.addParagraph(errorMsg.replaceAll("\n", "<br>"));
			errorMsg = html.close();
			type = MediaType.TEXT_HTML;
		}
		else
			errorMsg = errorTitle + "\n" + errorMsg;

		return Response.status(status).entity(errorMsg).type(type).build();
	}
}
