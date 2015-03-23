package org.kramerlab.cfpservice.api.impl.persistance;

public class PersistanceException extends RuntimeException
{
	public PersistanceException(String msg)
	{
		super(msg);
	}

	public PersistanceException(Throwable cause)
	{
		super(cause);
	}

	public PersistanceException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}
