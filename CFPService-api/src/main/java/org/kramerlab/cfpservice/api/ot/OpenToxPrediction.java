package org.kramerlab.cfpservice.api.ot;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.kramerlab.cfpservice.api.ModelService;

@XmlRootElement
@XmlType(name = "")
public interface OpenToxPrediction
{
	@XmlAttribute(name = "identifier", namespace = ModelService.DC_NAMESPACE)
	public String getURI();

	@XmlAttribute(namespace = ModelService.RDF_NAMESPACE)
	public String[] getType();

	@XmlType(name = "FeatureValue", namespace = ModelService.OPENTOX_API)
	public static interface FeatureValue
	{
		@XmlAttribute(namespace = ModelService.OPENTOX_API)
		public String getFeature();

		@XmlAttribute(namespace = ModelService.OPENTOX_API)
		public Object getValue();
	}

	@XmlType(name = "DataEntry", namespace = ModelService.OPENTOX_API)
	public static interface DataEntry
	{
		@XmlAttribute(namespace = ModelService.OPENTOX_API)
		public String getCompound();

		@XmlElement(namespace = ModelService.OPENTOX_API)
		public FeatureValue[] getValues();
	}

	@XmlElement(namespace = ModelService.OPENTOX_API)
	public DataEntry getDataEntry();
}
