package com.guye.orm;

/**
 * @author nieyu
 *
 */
public class DaoException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3734796776044391248L;

	public DaoException(String string) {
	    super(string);
	}

	public DaoException() {
		super();
	}

	public DaoException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public DaoException(Throwable throwable) {
		super(throwable);
	}

	public DaoException(String format, Throwable e, String message) {
		super(format +" and message:"+ message, e);
	}

}
