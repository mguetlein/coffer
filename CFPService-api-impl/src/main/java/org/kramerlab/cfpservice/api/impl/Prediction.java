package org.kramerlab.cfpservice.api.impl;

import java.io.FileInputStream;
import java.io.FileReader;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.kramerlab.cfpminer.CDKUtil;
import org.kramerlab.cfpminer.CFPtoArff;
import org.kramerlab.cfpservice.api.PredictionObj;
import org.kramerlab.cfpservice.api.impl.persistance.PersistanceAdapter;
import org.kramerlab.extendedrandomforests.html.PredictionReport;
import org.kramerlab.extendedrandomforests.weka.PredictionAttribute;
import org.mg.javalib.util.ArrayUtil;
import org.mg.javalib.util.StringUtil;
import org.openscience.cdk.interfaces.IAtomContainer;

import weka.core.Instance;
import weka.core.Instances;

@SuppressWarnings("restriction")
@XmlRootElement
public class Prediction extends PredictionObj
{
	private static final long serialVersionUID = 1L;

	@XmlAttribute
	protected List<PredictionAttribute> predictionAttributes;

	public Prediction()
	{
	}

	public void setPredictionAttributes(List<PredictionAttribute> predictionAttributes)
	{
		this.predictionAttributes = predictionAttributes;
	}

	public List<PredictionAttribute> getPredictionAttributes()
	{
		return predictionAttributes;
	}

	public IAtomContainer getMolecule()
	{
		try
		{
			return CDKUtil.parseSmiles(smiles);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public static String[] findLastPredictions(final String modelId)
	{
		return PersistanceAdapter.INSTANCE.findLastPredictions(modelId);
	}

	public static Prediction find(String modelId, String predictionId)
	{
		return PersistanceAdapter.INSTANCE.readPrediction(modelId, predictionId);
	}

	public static void main(String[] args) throws Exception
	{
		createPrediction(Model.find("DUD_vegfr2"), "c1c(CC(=O)O)cncc1");
	}

	public static Prediction createPrediction(Model m, String smiles)
	{
		try
		{
			Prediction p = new Prediction();
			p.smiles = smiles;

			String arffFile = "/tmp/dud_vegfr2.arff";
			CFPtoArff.writeTestDataset(arffFile, m.getCFPMiner(), "DUD_vegfr2", p.getMolecule());
			Instances data = new Instances(new FileReader(arffFile));
			Instance inst = data.get(0);
			data.setClassIndex(data.numAttributes() - 1);
			double dist[] = m.getExtendedRandomForest().distributionForInstance(inst);
			int maxIdx = ArrayUtil.getMaxIndex(dist);

			p.id = StringUtil.getMD5(smiles);
			p.modelId = m.getId();
			p.predictedClass = data.classAttribute().value(maxIdx);
			p.predictedDistribution = dist;
			p.setPredictionAttributes(m.getExtendedRandomForest().getPredictionAttributes(inst, dist));

			PersistanceAdapter.INSTANCE.savePrediction(p);

			return p;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public FileInputStream getHTML()
	{
		try
		{
			String file = PersistanceAdapter.INSTANCE.getPredictionHTMLFile(modelId, id);
			//			if (!new File(file).exists())
			//			{
			Model m = Model.find(modelId);
			PredictionReport rep = new PredictionReport(m.getExtendedRandomForest(), m.getCFPMiner(),
					m.getTrainingDataSmiles());
			rep.setTestInstance(getSmiles(), getPredictedDistribution(), getPredictionAttributes());
			rep.setImageProvider(new DepictServiceImpl());
			rep.setReportTitles(CFPServiceConfig.title, CFPServiceConfig.header, CFPServiceConfig.css,
					CFPServiceConfig.footer);
			rep.setModel(modelId, "/" + modelId);
			rep.buildReport(file);
			//			}
			return new FileInputStream(file);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}
