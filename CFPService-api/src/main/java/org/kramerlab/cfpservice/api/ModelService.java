package org.kramerlab.cfpservice.api;

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

/**
 * Java API and REST API Interface for {@value SERVICE_TITLE} ({@value SERVICE_HOME})<br>
 * REST methods are documented below.
 * 
 * @author Martin Gütlein guetlein@uni-mainz.de
 */
@Path("")
public interface ModelService
{
	public static String SERVICE_HOME = "http://ucfps.informatik.uni-mainz.de";
	public static String SERVICE_TITLE = "Unfolded Circular Fingerprints";

	/**
	 * <b>request:</b> GET {@value SERVICE_HOME}/<br>
	 * <b>content-type:</b> application/json<br>
	 * <b>returns:</b> list of models
	 */
	@Path("")
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	ModelObj[] getModels();

	/**
	 * <b>request:</b> POST {@value SERVICE_HOME}/<br>
	 * <b>params:</b> </i>compound</i> SMILES String<br>
	 * <b>returns:</b> redirect to prediction with all models
	 */
	@Path("")
	@POST
	Response predict(@FormParam("compound") String compound);

	@Path("")
	@GET
	@Produces({ MediaType.TEXT_HTML })
	String getModelsHTML();

	@Path("doc")
	@GET
	@Produces({ MediaType.TEXT_HTML })
	String getDocHTML();

	/**
	 * <b>request:</b> GET {@value SERVICE_HOME}/<i>modelId</i><br>
	 * <b>content-type:</b> application/json<br>
	 * <b>returns:</b> model
	 */
	@Path("{modelId}")
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	ModelObj getModel(@PathParam("modelId") String modelId);

	@Path("{modelId}")
	@GET
	@Produces({ MediaType.TEXT_HTML })
	String getModelHTML(@PathParam("modelId") String modelId);

	/**
	 * <b>request:</b> POST {@value SERVICE_HOME}/<i>modelId</i><br>
	 * <b>params:</b> <i>compound</i> SMILES String<br>
	 * <b>returns:</b> redirect to single-model prediction
	 */
	@Path("{modelId}")
	@POST
	Response predict(@PathParam("modelId") String modelId, @FormParam("compound") String compound);

	@Path("{modelId}/validation")
	@GET
	@Produces({ "image/png" })
	InputStream getValidationChart(@PathParam("modelId") String modelId);

	/**
	 * <b>request:</b> GET {@value SERVICE_HOME}/prediction/<i>modelId</i><br>
	 * <b>content-type:</b> application/json<br>
	 * <b>returns:</b> list of single-model predictions
	 */
	@Path("prediction/{predictionId}")
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	PredictionObj[] getPredictions(@PathParam("predictionId") String predictionId, @FormParam("wait") String wait);

	@Path("prediction/{predictionId}")
	@GET
	@Produces({ MediaType.TEXT_HTML })
	String getPredictionsHTML(@PathParam("predictionId") String predictionId, @FormParam("wait") String wait);

	/**
	 * <b>request:</b> GET {@value SERVICE_HOME}/<i>modelId</i>/prediction/<i>predictionId</i><br>
	 * <b>content-type:</b> application/json<br>
	 * <b>returns:</b> single-model prediction
	 */
	@Path("{modelId}/prediction/{predictionId}")
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	PredictionObj getPrediction(@PathParam("modelId") String modelId, @PathParam("predictionId") String predictionId);

	@Path("{modelId}/prediction/{predictionId}")
	@GET
	@Produces({ MediaType.TEXT_HTML })
	String getPredictionHTML(@PathParam("modelId") String modelId, @PathParam("predictionId") String predictionId,
			@FormParam("size") String size);

	/**
	 * <b>request:</b> GET {@value SERVICE_HOME}/<i>modelId</i>/fragment/<i>fragmentId</i><br>
	 * <b>content-type:</b> application/json<br>
	 * <b>returns:</b> fragment
	 */
	@Path("{modelId}/fragment/{fragmentId}")
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	FragmentObj getFragment(@PathParam("modelId") String modelId, @PathParam("fragmentId") String fragmentId);

	@Path("{modelId}/fragment/{fragmentId}")
	@GET
	@Produces({ MediaType.TEXT_HTML })
	String getFragmentHTML(@PathParam("modelId") String modelId, @PathParam("fragmentId") String fragmentId,
			@FormParam("size") String size);

	@Path("info/{service}/{smiles}")
	@GET
	@Produces({ MediaType.TEXT_HTML })
	String getCompoundInfo(@PathParam("service") String service, @PathParam("smiles") String smiles);

	/**
	 * <b>request:</b> GET {@value SERVICE_HOME}/depict<br>
	 * <b>content-type:</b> image/png<br>
	 * <b>params:</b> <i>smiles</i> compound smiles string<br>
	 * <i>size</i> integer to specify width and height (optional), otherwise depends on compound with default bond size<br>
	 * <i>atoms</i> comma separated atom indices (optional) to be highlighted<br>
	 * <i>highlightOutgoingBonds</i> true (optional) if outgoing bonds of fragment should be highlighted<br> 
	 * <i>crop</i> true (optional) if image should be cropped around matching atoms<br>
	 * <b>returns:</b> image
	 */
	@Path("depict")
	@GET
	@Produces({ "image/png" })
	InputStream depict(@FormParam("smiles") String smiles, @FormParam("size") String size);

	@Path("depictMatch")
	@GET
	@Produces({ "image/png" })
	InputStream depictMatch(@FormParam("smiles") String smiles, @FormParam("size") String size,
			@FormParam("atoms") String atoms, @FormParam("highlightOutgoingBonds") String highlightOutgoingBonds,
			@FormParam("activating") String activating, @FormParam("crop") String crop);

	@Path("depictMultiMatch")
	@GET
	@Produces({ "image/png" })
	InputStream depictMultiMatch(@FormParam("smiles") String smiles, @FormParam("size") String size,
			@FormParam("prediction") String prediction, @FormParam("model") String model);

}
