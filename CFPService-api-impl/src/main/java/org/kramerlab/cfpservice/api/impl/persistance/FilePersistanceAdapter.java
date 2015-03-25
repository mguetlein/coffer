package org.kramerlab.cfpservice.api.impl.persistance;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Comparator;
import java.util.List;

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

	public String getModelHTMLFile(String modelId)
	{
		return "persistance/model/" + modelId + ".html";
	}

	private static String getPredictionFile(String modelId, String predictionId)
	{
		return "persistance/prediction/" + modelId + "_" + predictionId + ".prediction";
	}

	public String getPredictionHTMLFile(String modelId, String predictionId)
	{
		return "persistance/prediction/" + modelId + "_" + predictionId + ".html";
	}

	public String getFragmentHTMLFile(String modelId, String fragmentId)
	{
		return "persistance/fragment/" + modelId + "_" + fragmentId + ".html";
	}

	public boolean modelExists(String modelId)
	{
		return new File(getModelFile(modelId)).exists() && new File(getModelCFPFile(modelId)).exists()
				&& dataLoader.exists(modelId);
	}

	public List<String> readTrainingDataEndpoints(String modelId)
	{
		return dataLoader.getDataset(modelId).getEndpoints();
	}

	public List<String> readTrainingDataSmiles(String modelId)
	{
		return dataLoader.getDataset(modelId).getSmiles();
	}

	public ExtendedRandomForest readExtendedRandomForest(String modelId)
	{
		try
		{
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(getModelFile(modelId)));
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
				return name.endsWith(".model") && new File(getModelCFPFile(FileUtil.getFilename(name, false))).exists();
			}
		});
		Model res[] = new Model[models.length];
		for (int i = 0; i < res.length; i++)
			res[i] = Model.find(FileUtil.getFilename(models[i], false));
		return res;
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

	public String[] findLastPredictions(final String modelId)
	{
		File preds[] = new File("persistance/prediction").listFiles(new FilenameFilter()
		{
			public boolean accept(File dir, String name)
			{
				return name.endsWith(".prediction") && name.startsWith(modelId + "_");
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
			oos.writeObject(model.getExtendedRandomForest());
			oos.flush();
			oos.close();
			System.out.println("model written to " + file);

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
}
