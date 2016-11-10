package org.kramerlab.cfpservice.api.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.mg.javalib.util.StopWatchUtil;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

public class SerializationTest
{
	public static void test(Serializable o) throws IOException, ClassNotFoundException
	{
		byte[] b;

		StopWatchUtil.setUseCpuTime(false);

		StopWatchUtil.start("write");
		{
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bos);
			out.writeObject(o);
			out.flush();
			b = bos.toByteArray();
			out.close();
			bos.close();
		}
		StopWatchUtil.stop("write");

		StopWatchUtil.start("read");
		{
			ByteArrayInputStream bis = new ByteArrayInputStream(b);
			ObjectInputStream in = new ObjectInputStream(bis);
			in.readObject();
			in.close();
			bis.close();
		}
		StopWatchUtil.stop("read");

		StopWatchUtil.start("write fst");
		{
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			FSTObjectOutput out = new FSTObjectOutput(bos);
			out.writeObject(o);
			out.flush();
			b = bos.toByteArray();
			out.close();
			bos.close();
		}
		StopWatchUtil.stop("write fst");

		StopWatchUtil.start("read fst");
		{
			ByteArrayInputStream bis = new ByteArrayInputStream(b);
			FSTObjectInput in = new FSTObjectInput(bis);
			in.readObject();
			in.close();
			bis.close();
		}
		StopWatchUtil.stop("read fst");

		StopWatchUtil.print();
		System.out.println();
		System.out.flush();
	}

}
