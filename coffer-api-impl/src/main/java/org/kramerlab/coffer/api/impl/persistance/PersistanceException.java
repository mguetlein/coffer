package org.kramerlab.coffer.api.impl.persistance;

public class PersistanceException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

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
