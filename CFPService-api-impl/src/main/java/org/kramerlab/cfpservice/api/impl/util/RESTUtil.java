package org.kramerlab.cfpservice.api.impl.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class RESTUtil
{
	public static String get(String uri, String accept) throws ClientProtocolException, IOException
	{
		CloseableHttpClient client = null;
		BufferedReader rd = null;
		try
		{
			client = HttpClientBuilder.create().build();
			HttpGet request = new HttpGet(uri);
			request.setHeader("Accept", accept);
			HttpResponse response = client.execute(request);
			rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuffer sb = new StringBuffer();
			String line = null;
			while ((line = rd.readLine()) != null)
				sb.append(line);
			return sb.toString();
		}
		finally
		{
			IOUtils.closeQuietly(rd);
			IOUtils.closeQuietly(client);
		}
	}

	public static void main(String[] args) throws ClientProtocolException, IOException
	{
		System.out.println(
				get("http://lazar-services.in-silico.ch/compound/InChI=1S/C11H10/c1-9-5-4-7-10-6-2-3-8-11(9)10/h2-8H,1H3",
						"chemical/x-daylight-smiles"));
	}
}
