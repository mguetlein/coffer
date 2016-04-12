package org.kramerlab.coffer.api.ot;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.kramerlab.coffer.api.ModelService;

@XmlRootElement
@XmlType(name = "")
public interface OpenToxFragment
{
	@XmlAttribute(name = "identifier", namespace = ModelService.DC_NAMESPACE)
	public String getURI();

	@XmlAttribute(namespace = ModelService.RDF_NAMESPACE)
	public String[] getType();
}
