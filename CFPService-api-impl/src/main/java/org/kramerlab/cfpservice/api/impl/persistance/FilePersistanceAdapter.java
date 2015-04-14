package org.kramerlab.cfpservice.api.impl.persistance;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kramerlab.cfpminer.CFPDataLoader;
import org.kramerlab.cfpminer.CFPMiner;
import org.kramerlab.cfpservice.api.impl.Model;
import org.kramerlab.cfpservice.api.impl.Prediction;
import org.kramerlab.extendedrandomforests.weka.ExtendedRandomForest;
import org.mg.javalib.util.ArrayUtil;
import org.mg.javalib.util.FileUtil;

public class FilePersistanceAdapter implements PersistanceAdapter
{
	CFPDataLoader dataLoader = new CFPDataLoader("persistance/data");

	private static String getModelFile(String id)
	{
		return "persistance/model/" + id + ".model";
	}

	private static String getModelClassifierFile(String id)
	{
		return "persistance/model/" + id + ".classifier";
	}

	private static String getModelCFPFile(String id)
	{
		return "persistance/model/" + id + ".cfp";
	}

	public String getModelValidationResultsFile(String id)
	{
		return "persistance/model/" + id + ".arff";
	}

	public String getModelValidationImageFile(String id)
	{
		return "persistance/img/" + id + ".png";
	}

	private static String getPredictionFile(String modelId, String predictionId)
	{
		return "persistance/prediction/" + modelId + "_" + predictionId + ".prediction";
	}

	public Date getPredictionDate(String modelId, String predictionId)
	{
		return new Date(new File(getPredictionFile(modelId, predictionId)).lastModified());
	}

	public boolean modelExists(String modelId)
	{
		return new File(getModelFile(modelId)).exists();
	}

	public List<String> readDatasetEndpoints(String modelId)
	{
		return dataLoader.getDataset(modelId).getEndpoints();
	}

	public List<String> readDatasetSmiles(String modelId)
	{
		return dataLoader.getDataset(modelId).getSmiles();
	}

	public Model readModel(String modelId)
	{
		try
		{
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(getModelFile(modelId)));
			Model m = (Model) ois.readObject();
			ois.close();
			return m;
		}
		catch (Exception e)
		{
			throw new PersistanceException(e);
		}
	}

	public ExtendedRandomForest readExtendedRandomForest(String modelId)
	{
		try
		{
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(getModelClassifierFile(modelId)));
			ExtendedRandomForest erf = (ExtendedRandomForest) ois.readObject();
			ois.close();
			return erf;
		}
		catch (Exception e)
		{
			throw new PersistanceException(e);
		}
	}

	public CFPMiner readCFPMiner(String modelId)
	{
		try
		{
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(getModelCFPFile(modelId)));
			CFPMiner miner = (CFPMiner) ois.readObject();
			ois.close();
			return miner;
		}
		catch (Exception e)
		{
			throw new PersistanceException(e);
		}
	}

	public Model[] readModels()
	{
		String models[] = new File("persistance/model").list(new FilenameFilter()
		{
			public boolean accept(File dir, String name)
			{
				return name.endsWith(".model");
			}
		});
		Model res[] = new Model[models.length];
		for (int i = 0; i < res.length; i++)
			res[i] = Model.find(FileUtil.getFilename(models[i], false));
		Arrays.sort(res, new Comparator<Model>()
		{
			public int compare(Model o1, Model o2)
			{
				return CFPDataLoader.CFPDataComparator.compare(o1.getId(), o2.getId());
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

	public Prediction readPrediction(String modelId, String predictionId)
	{
		try
		{
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(getPredictionFile(modelId, predictionId)));
			Prediction pred = (Prediction) ois.readObject();
			ois.close();
			return pred;
		}
		catch (Exception e)
		{
			throw new PersistanceException(e);
		}
	}

	public String[] findLastPredictions(final String... modelIds)
	{
		final HashSet<String> checked = new HashSet<String>();
		File preds[] = new File("persistance/prediction").listFiles(new FilenameFilter()
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

	public void savePrediction(Prediction prediction)
	{
		try
		{
			String file = getPredictionFile(prediction.getModelId(), prediction.getId());
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
			oos.writeObject(prediction);
			oos.flush();
			oos.close();
			System.out.println("prediction written to " + file);
		}
		catch (Exception e)
		{
			throw new PersistanceException(e);
		}
	}

	public void saveModel(Model model)
	{
		try
		{
			String file = getModelFile(model.getId());
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
			oos.writeObject(model);
			oos.flush();
			oos.close();
			System.out.println("model written to " + file);

			file = getModelClassifierFile(model.getId());
			oos = new ObjectOutputStream(new FileOutputStream(file));
			oos.writeObject(model.getExtendedRandomForest());
			oos.flush();
			oos.close();
			System.out.println("classifier written to " + file);

			file = getModelCFPFile(model.getId());
			oos = new ObjectOutputStream(new FileOutputStream(file));
			oos.writeObject(model.getCFPMiner());
			oos.flush();
			oos.close();
			System.out.println("cfps written to " + file);
		}
		catch (Exception e)
		{
			throw new PersistanceException(e);
		}
	}

	public String getModelEndpoint(String modelId)
	{
		return dataLoader.getDatasetEndpoint(modelId);
	}

	public Set<String> getModelDatasetURLs(String modelId)
	{
		return dataLoader.getDatasetURLs(modelId);
	}

	public Map<String, String> getModelDatasetCitations(String modelId)
	{
		return dataLoader.getModelDatasetCitations(modelId);
	}
}
