package org.kramerlab.coffer.api.objects;

import java.util.List;
import java.util.Map;

public interface Model extends ServiceResource
{
	public String getId();

	public int getActiveClassIdx();

	public String[] getClassValues();

	public String getName();

	public String getTarget();

	public Map<String, String> getDatasetCitations();

	public List<String> getDatasetWarnings();

	public String getEndpointsSummary();

	public String getClassifierName();

	public String getNiceFragmentDescription();

	public int getNumFragments();
}
