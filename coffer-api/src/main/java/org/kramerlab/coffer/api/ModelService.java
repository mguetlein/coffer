package org.kramerlab.coffer.api;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.InputStream;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.kramerlab.coffer.api.objects.Compound;
import org.kramerlab.coffer.api.objects.Fragment;
import org.kramerlab.coffer.api.objects.Model;
import org.kramerlab.coffer.api.objects.Prediction;

/**
 * Java API and REST API Interface for {@value SERVICE_TITLE} ({@value SERVICE_HOME})<br>
 * REST methods are documented below.
 * 
 * @author Martin Gütlein guetlein@uni-mainz.de
 */
@Path("")
public interface ModelService
{
	public static final String SERVICE_HOME = "http://coffer.informatik.uni-mainz.de";
	public static final String SERVICE_TITLE = "CoFFer - Collision-free Filtered Circular Fingerprint-based QSARs";

	public static final String DC_NAMESPACE = "http://purl.org/dc/elements/1.1";
	public static final String DC_PREFIX = "dc";

	public static final String OPENTOX_API = "http://www.opentox.org/api/1.2";
	public static final String OPENTOX_API_PREFIX = "ot";

	public static final String RDF_NAMESPACE = "https://www.w3.org/1999/02/22-rdf-syntax-ns";
	public static final String RDF_PREFIX = "rdf";

	public static final String MEDIA_TYPE_CHEMICAL_SMILES = "chemical/x-daylight-smiles";
	public static final String MEDIA_TYPE_TEXT_URI_LIST = "text/uri-list";
	public static final String MEDIA_TYPE_HTML_UTF8 = "text/html; charset=UTF-8";

	/**
	 * <b>request:</b> GET {@value SERVICE_HOME}/<br>
	 * <b>content-type:</b> application/json, text/html, {@value MEDIA_TYPE_TEXT_URI_LIST}<br>
	 * <b>returns:</b> list of models
	 */
	@Path("")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MEDIA_TYPE_HTML_UTF8, MEDIA_TYPE_TEXT_URI_LIST })
	Model[] getModels();

	public static final String PREDICT_PARAM_COMPOUND_SMILES = "compoundSmiles";
	public static final String PREDICT_PARAM_COMPOUND_URI = "compound_uri";

	/**
	 * <b>request:</b> POST {@value SERVICE_HOME}/<br>
	 * <b>params:</b><br>
	 * </i>{@value PREDICT_PARAM_COMPOUND_SMILES}:</i> SMILES String<br>
	 * <b>returns:</b> redirect to prediction with all models
	 */
	@Path("")
	@POST
	Response predict(@FormParam(PREDICT_PARAM_COMPOUND_SMILES) String compound);

	/**
	 * <b>request:</b> GET {@value SERVICE_HOME}/doc<br>
	 * <b>content-type:</b> text/html<br>
	 * <b>returns:</b> documentation
	 */
	@Path("doc")
	@GET
	@Produces({ MEDIA_TYPE_HTML_UTF8 })
	String getDocumentation();

	/**
	 * <b>request:</b> GET {@value SERVICE_HOME}/<i>modelId</i><br>
	 * <b>content-type:</b> application/json, text/html<br>
	 * <b>returns:</b> model
	 */
	@Path("{modelId}")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MEDIA_TYPE_HTML_UTF8 })
	Model getModel(@PathParam("modelId") String modelId);

	/**
	 * <b>request:</b> POST {@value SERVICE_HOME}/<i>modelId</i><br>
	 * <b>params:</b><br>
	 * <i>{@value PREDICT_PARAM_COMPOUND_SMILES}:</i> SMILES String<br>
	 * <b>returns:</b> redirect to single-model prediction
	 */
	@Path("{modelId}")
	@POST
	Response predict(@PathParam("modelId") String modelId,
			@FormParam(PREDICT_PARAM_COMPOUND_SMILES) String compound,
			@FormParam(PREDICT_PARAM_COMPOUND_URI) String compoundURI);

	/**
	 * <b>request:</b> GET {@value SERVICE_HOME}/<i>modelId</i>/validation<br>
	 * <b>content-type:</b> image/png<br>
	 * <b>returns:</b> image
	 */
	@Path("{modelId}/validation")
	@GET
	@Produces({ "image/png" })
	InputStream getValidationChart(@PathParam("modelId") String modelId);

	/**
	 * <b>request:</b> GET {@value SERVICE_HOME}/prediction/<i>predictionId</i><br>
	 * <b>content-type:</b> application/json, text/html, {@value MEDIA_TYPE_TEXT_URI_LIST}<br>
	 * <b>params:</b><br>
	 * <i>wait:</i> integer encoding the number of expected results<br> 
	 * <b>returns:</b> list of single-model predictions, empty predictions trailing if num predictions is < wait
	 */
	@Path("prediction/{predictionId}")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MEDIA_TYPE_HTML_UTF8, MEDIA_TYPE_TEXT_URI_LIST })
	Prediction[] getPredictions(@PathParam("predictionId") String predictionId,
			@FormParam("wait") String wait);

	public static final int DEFAULT_NUM_ENTRIES = 10;
	public static final String HIDE_SUPER_FRAGMENTS = "hideSuperFragments";
	public static final String HIDE_SUB_FRAGMENTS = "hideSubFragments";
	public static final String HIDE_NO_FRAGMENTS = "hideNoFragments";

	/**
	 * <b>request:</b> GET {@value SERVICE_HOME}/<i>modelId</i>/prediction/<i>predictionId</i><br>
	 * <b>content-type:</b> application/json, text/html<br>
	 * <b>params:</b><br>
	 * <i>hideFragments:</i> filter options for fragments: {@value HIDE_SUPER_FRAGMENTS}|{@value HIDE_SUB_FRAGMENTS}|{@value HIDE_NO_FRAGMENTS}<br>
	 * <i>size:</i> integer specifying the number of fragments shown (default: {@value DEFAULT_NUM_ENTRIES})<br>
	 * <b>returns:</b> single-model prediction
	 */
	@Path("{modelId}/prediction/{predictionId}")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MEDIA_TYPE_HTML_UTF8 })
	Prediction getPrediction(@PathParam("modelId") String modelId,
			@PathParam("predictionId") String predictionId,
			@FormParam("hideFragments") String hideFragments, @FormParam("size") String size);

	/**
	 * <b>request:</b> GET {@value SERVICE_HOME}/<i>modelId</i>/fragment/<i>fragmentId</i><br>
	 * <b>content-type:</b> application/json, text/html<br>
	 * <b>params:</b><br>
	 * <i>size:</i> integer specifying the number of fragments shown (default: {@value DEFAULT_NUM_ENTRIES})<br>
	 * <i>smiles:</i> a compound that the fragment is matched on (optional)<br>
	 * <b>returns:</b> fragment
	 */
	@Path("{modelId}/fragment/{fragmentId}")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MEDIA_TYPE_HTML_UTF8, MediaType.APPLICATION_XML })
	Fragment getFragment(@PathParam("modelId") String modelId,
			@PathParam("fragmentId") String fragmentId, @FormParam("size") String size,
			@FormParam("smiles") String smiles);

	/**
	 * <b>request:</b> GET {@value SERVICE_HOME}/info/<i>service</i>/<i>smiles</i><br>
	 * <b>content-type:</b> text/html<br>
	 * <b>returns:</b> compound info fetched from another service
	 */
	@Path("info/{service}/{smiles}")
	@GET
	@Produces({ MEDIA_TYPE_HTML_UTF8 })
	String getCompoundInfo(@PathParam("service") String service,
			@PathParam("smiles") String smiles);

	/**
	 * <b>request:</b> GET {@value SERVICE_HOME}/depict<br>
	 * <b>content-type:</b> image/png<br>
	 * <b>params:</b><br>
	 * <i>smiles:</i> compound smiles string<br>
	 * <i>size:</i> integer to specify width and height (optional), otherwise depends on compound with default bond size<br>
	 * <b>returns:</b> image
	 */
	@Path("depict")
	@GET
	@Produces({ "image/png" })
	InputStream depict(@FormParam("smiles") String smiles, @FormParam("size") String size);

	/**
	 * <b>request:</b> GET {@value SERVICE_HOME}/depictMatch<br>
	 * <b>content-type:</b> image/png<br>
	 * <b>params:</b><br>
	 * <i>smiles:</i> compound smiles string<br>
	 * <i>size:</i> integer to specify width and height (optional), otherwise depends on compound with default bond size<br>
	 * <i>atoms:</i> comma separated atom indices to be highlighted<br>
	 * <i>highlightOutgoingBonds:</i> true (optional) if outgoing bonds of fragment should be highlighted<br>
	 * <i>activating:</i> true/false (optional) if match should be colored as activating/de-activating instead of neutral<br> 
	 * <i>crop:</i> true (optional) if image should be cropped around matching atoms<br>
	 * <b>returns:</b> image
	 */
	@Path("depictMatch")
	@GET
	@Produces({ "image/png" })
	InputStream depictMatch(@FormParam("smiles") String smiles, @FormParam("size") String size,
			@FormParam("atoms") String atoms,
			@FormParam("highlightOutgoingBonds") String highlightOutgoingBonds,
			@FormParam("activating") String activating, @FormParam("crop") String crop);

	/**
	 * <b>request:</b> GET {@value SERVICE_HOME}/depictMultiMatch<br>
	 * <b>content-type:</b> image/png<br>
	 * <b>params:</b><br>
	 * <i>smiles:</i> compound smiles string<br>
	 * <i>size:</i> integer to specify width and height (optional), otherwise depends on compound with default bond size<br>
	 * <i>model:</i> id of the model that is used to predict the compound<br>
	 * <b>returns:</b> image
	 */
	@Path("depictMultiMatch")
	@GET
	@Produces({ "image/png" })
	InputStream depictMultiMatch(@FormParam("smiles") String smiles, @FormParam("size") String size,
			@FormParam("model") String model);

	/**
	 * <b>request:</b> GET {@value SERVICE_HOME}/compound/<i>compoundId</id><br>
	 * <b>content-type:</b> text/html, application/json, chemical/x-daylight-smiles<br>
	 * <b>returns:</b> compound 
	 */
	@Path("compound/{compoundId}")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MEDIA_TYPE_HTML_UTF8,
			ModelService.MEDIA_TYPE_CHEMICAL_SMILES })
	Compound getCompound(@PathParam("compoundId") String compoundId);

	@Path("{modelId}/appdomain")
	@GET
	@Produces({ MEDIA_TYPE_HTML_UTF8 })
	String getAppDomain(@PathParam("modelId") String modelId, @FormParam("smiles") String smiles,
			@FormParam("size") String size);

	@Path("{modelId}/appdomain")
	@POST
	Response predictAppDomain(@PathParam("modelId") String modelId,
			@FormParam(PREDICT_PARAM_COMPOUND_SMILES) String compound);

	@Path("{modelId}/depictAppdomain")
	@GET
	@Produces({ "image/png" })
	InputStream depictAppDomain(@PathParam("modelId") String modelId,
			@FormParam("smiles") String smiles);

	@Path("depictActiveIcon")
	@GET
	@Produces({ "image/png" })
	InputStream depictActiveIcon(@FormParam("probability") String probability,
			@FormParam("drawHelp") String drawHelp);

}
