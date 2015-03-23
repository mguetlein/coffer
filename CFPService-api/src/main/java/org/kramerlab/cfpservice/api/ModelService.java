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

@Path("")
public interface ModelService
{
	@Path("")
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	ModelObj[] getModels();

	@Path("")
	@GET
	@Produces({ MediaType.TEXT_HTML })
	String getModelsHTML();

	@Path("{id}")
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	ModelObj getModel(@PathParam("id") String id);

	@Path("{id}")
	@GET
	@Produces({ MediaType.TEXT_HTML })
	InputStream getModelHTML(@PathParam("id") String id);

	@Path("{id}")
	@POST
	Response predict(@PathParam("id") String id, @FormParam("compound") String compound);

	@Path("{id}/validation")
	@GET
	@Produces({ "image/png" })
	InputStream getValidationChart(@PathParam("id") String id);

	@Path("{id}/prediction/{pId}")
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	PredictionObj getPrediction(@PathParam("id") String modelId, @PathParam("pId") String pId);

	@Path("{id}/prediction/{pId}")
	@GET
	@Produces({ MediaType.TEXT_HTML })
	InputStream getPredictionHTML(@PathParam("id") String modelId, @PathParam("pId") String pId);

	//	@Path("{id}/fragment/{pId}")
	//	@GET
	//	@Produces({ MediaType.APPLICATION_JSON })
	//	Fragment getFragment(@PathParam("id") String modelId, @PathParam("pId") String predictionId);
	//
	@Path("{id}/fragment/{fId}")
	@GET
	@Produces({ MediaType.TEXT_HTML })
	InputStream getFragmentHTML(@PathParam("id") String modelId, @PathParam("fId") String fragmentId);
}
