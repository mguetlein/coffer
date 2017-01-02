package org.kramerlab.coffer.api.impl.persistance;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.kramerlab.cfpminer.appdomain.ADInfoModel;
import org.kramerlab.coffer.api.impl.DepictService;
import org.kramerlab.coffer.api.impl.objects.AbstractModel;
import org.kramerlab.coffer.api.impl.objects.AbstractPrediction;
import org.kramerlab.coffer.api.objects.Model;
import org.mg.cdklib.cfp.CFPMiner;
import org.mg.cdklib.data.DataProvider;
import org.mg.cdklib.data.DataProvider.DataID;
import org.mg.javalib.util.ArrayUtil;
import org.mg.javalib.util.FileUtil;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

import weka.classifiers.Classifier;

public class FilePersistanceAdapter implements PersistanceAdapter
{
	private static final String FOLDER = System.getProperty("user.home")
			+ "/results/coffer/persistance";

	private static String getModelFile(String id)
	{
		return FOLDER + "/model/" + id + ".model";
	}

	private static String getModelClassifierFile(String id)
	{
		return FOLDER + "/model/" + id + ".classifier";
	}

	private static String getModelCFPFile(String id)
	{
		return FOLDER + "/model/" + id + ".cfp";
	}

	private static String getModelAppDomainFile(String id)
	{
		return FOLDER + "/model/" + id + ".appdomain";
	}

	public String getModelValidationResultsFile(String id)
	{
		return FOLDER + "/model/" + id + ".res";
	}

	public String getModelValidationImageFile(String id)
	{
		return FOLDER + "/img/" + id + ".png";
	}

	private String getDatasetWarningFile(DataID id)
	{
		return FOLDER + "/warn/" + id + ".warn";
	}

	private static String getPredictionFile(String modelId, String predictionId)
	{
		return FOLDER + "/prediction/" + modelId + "_" + predictionId + ".prediction";
	}

	public Date getPredictionDate(String modelId, String predictionId)
	{
		return new Date(new File(getPredictionFile(modelId, predictionId)).lastModified());
	}

	public boolean modelExists(String modelId)
	{
		return new File(getModelFile(modelId)).exists();
	}

	public List<String> readDatasetEndpoints(DataID dataID)
	{
		return new ArrayList<String>(DataProvider.getDataset(dataID).getEndpoints());
	}

	public List<String> readDatasetSmiles(DataID dataID)
	{
		return new ArrayList<String>(DataProvider.getDataset(dataID).getSmiles());
	}

	public List<String> readModelDatasetWarnings(DataID dataID)
	{
		try
		{
			String f = getDatasetWarningFile(dataID);
			if (!new File(f).exists())
			{
				List<String> warnings = DataProvider.getDataset(dataID).getWarnings();
				System.out.println("Write warnings to " + f);
				IOUtils.writeLines(warnings, null, new FileOutputStream(f));
				return warnings;
			}
			else
				return IOUtils.readLines(new FileInputStream(f));
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	Map<String, byte[]> cache = new HashMap<>();

	/**
	 * limit hard-drive access by caching raw data (could get potentially large!) 
	 * serialization is done with FST which is a faster implementation of java serialization
	 * 
	 * @param file
	 * @return
	 */
	private Object readFile(String file)
	{
		try
		{
			synchronized (file)
			{
				if (!cache.containsKey(file))
					cache.put(file, IOUtils.toByteArray(new FileInputStream(file)));
			}
			FSTObjectInput in = new FSTObjectInput(new ByteArrayInputStream(cache.get(file)));
			Object o = in.readObject();
			in.close();
			return o;
		}
		catch (Exception e)
		{
			throw new PersistanceException(e);
		}
	}

	private void writeFile(Object o, String file)
	{
		try
		{
			synchronized (file)
			{
				cache.remove(file);
			}
			FSTObjectOutput out = new FSTObjectOutput(new FileOutputStream(file));
			out.writeObject(o);
			out.close();
		}
		catch (Exception e)
		{
			throw new PersistanceException(e);
		}
	}

	@Override
	public AbstractModel readModel(String modelId)
	{
		return (AbstractModel) readFile(getModelFile(modelId));
	}

	@Override
	public Classifier readClassifier(String modelId)
	{
		return (Classifier) readFile(getModelClassifierFile(modelId));
	}

	@Override
	public CFPMiner readCFPMiner(String modelId)
	{
		return (CFPMiner) readFile(getModelCFPFile(modelId));
	}

	@Override
	public ADInfoModel readAppDomain(String modelId)
	{
		return (ADInfoModel) readFile(getModelAppDomainFile(modelId));
	}

	public Model[] readModels()
	{
		String models[] = new File(FOLDER + "/model").list(new FilenameFilter()
		{
			public boolean accept(File dir, String name)
			{
				return name.endsWith(".model");
			}
		});
		Model res[] = new AbstractModel[models.length];
		for (int i = 0; i < res.length; i++)
			res[i] = AbstractModel.find(FileUtil.getFilename(models[i], false));
		Arrays.sort(res, new Comparator<Model>()
		{
			public int compare(Model o1, Model o2)
			{
				return DataProvider.CFPDataComparator.compare(o1.getId(), o2.getId());
			}
		});
		return res;
	}

	public boolean predictionExists(String modelId, String predictionId)
	{
		return new File(getPredictionFile(modelId, predictionId)).exists();
	}

	public void updateDate(String modelId, String predictionId)
	{
		new File(getPredictionFile(modelId, predictionId)).setLastModified(new Date().getTime());
	}

	public AbstractPrediction readPrediction(String modelId, String predictionId)
	{
		return (AbstractPrediction) readFile(getPredictionFile(modelId, predictionId));
	}

	public String[] findAllPredictions(final String... modelIds)
	{
		final HashSet<String> checked = new HashSet<String>();
		File preds[] = new File(FOLDER + "/prediction").listFiles(new FilenameFilter()
		{
			public boolean accept(File dir, String name)
			{
				if (!name.endsWith(".prediction"))
					return false;
				String predId = ArrayUtil.last(FileUtil.getFilename(name, false).split("_"));
				if (checked.contains(predId))
					return false;
				checked.add(predId);
				for (String modelId : modelIds)
					if (!predictionExists(modelId, predId))
						return false;
				return true;
			}
		});
		preds = ArrayUtil.sort(File.class, preds, new Comparator<File>()
		{
			public int compare(File o1, File o2)
			{
				return Long.valueOf(o2.lastModified()).compareTo(Long.valueOf(o1.lastModified()));
			}
		});
		String last[] = new String[preds.length];
		for (int i = 0; i < last.length; i++)
			last[i] = ArrayUtil.last(FileUtil.getFilename(preds[i].getName(), false).split("_"));
		return last;
	}

	public void savePrediction(AbstractPrediction prediction)
	{
		String file = getPredictionFile(prediction.getModelId(), prediction.getId());
		writeFile(prediction, file);
		System.out.println("prediction written to " + file);
	}

	public void deleteModel(String id)
	{
		System.out.println("deleting model '" + id + "' and all its predictions");
		for (String pId : findAllPredictions(id))
			deletePrediction(id, pId);
		new File(getModelCFPFile(id)).delete();
		new File(getModelClassifierFile(id)).delete();
		new File(getModelFile(id)).delete();
		new File(getModelAppDomainFile(id)).delete();
		DepictService.deleteAllImagesForModel(id);
		if (new File(getModelValidationResultsFile(id)).exists())
			new File(getModelValidationResultsFile(id)).delete();
		if (new File(getModelValidationImageFile(id)).exists())
			new File(getModelValidationImageFile(id)).delete();
	}

	public void deletePrediction(String modelId, String predictionId)
	{
		new File(getPredictionFile(modelId, predictionId)).delete();
	}

	public void saveModel(AbstractModel model)
	{
		String file = getModelFile(model.getId());
		writeFile(model, file);
		System.out.println("model written to " + file);

		file = getModelClassifierFile(model.getId());
		writeFile(model.getClassifier(), file);
		System.out.println("classifier written to " + file);

		file = getModelCFPFile(model.getId());
		writeFile(model.getCFPMiner(), file);
		System.out.println("cfps written to " + file);

		file = getModelAppDomainFile(model.getId());
		writeFile(model.getAppDomain(), file);
		System.out.println("appDomain written to " + file);

		readModelDatasetWarnings(model.getDatasetID());
	}

	public String getModelDatasetEndpoint(DataID dataID)
	{
		return DataProvider.getDatasetEndpoint(dataID);
	}

	public Set<String> getModelDatasetURLs(DataID dataID)
	{
		return DataProvider.getDatasetURLs(dataID);
	}

	public Map<String, String> getModelDatasetCitations(DataID dataID)
	{
		return DataProvider.getModelDatasetCitations(dataID);
	}
}
