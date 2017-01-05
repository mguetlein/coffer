package org.kramerlab.coffer.api.impl;

import java.util.HashSet;

public class ModelServiceTasks
{
	private static HashSet<String> running = new HashSet<>();

	/**
	 * currently, the key is never removed,
	 * so this is for tasks that only have to be performed once 
	 * 
	 * @param key
	 * @param r
	 */
	public static synchronized void addTask(String key, Runnable r)
	{
		if (running.contains(key))
		{
			System.out.println("already running: " + key);
		}
		else
		{
			System.out.println("starting job: " + key);
			running.add(key);
			Thread th = new Thread(r, key);
			th.start();
		}
	}
}
