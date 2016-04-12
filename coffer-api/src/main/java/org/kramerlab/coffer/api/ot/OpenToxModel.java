package org.kramerlab.coffer.api.ot;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.kramerlab.coffer.api.ModelService;

@XmlRootElement
@XmlType(name = "")
public interface OpenToxModel
{
	@XmlAttribute(name = "identifier", namespace = ModelService.DC_NAMESPACE)
	public String getURI();

	@XmlAttribute(namespace = ModelService.RDF_NAMESPACE)
	public String[] getType();

	@XmlAttribute(namespace = ModelService.OPENTOX_API)
	public String getDependentVariables();

	@XmlAttribute(namespace = ModelService.OPENTOX_API)
	public String[] getPredictedVariables();
}
