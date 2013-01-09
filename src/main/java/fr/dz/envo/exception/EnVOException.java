package fr.dz.envo.exception;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;

public class EnVOException extends Exception {

	private static final long serialVersionUID = -7645997795284164044L;
	
	private List<EnVOException> exceptions;

	/**
	 * Constructeur par défaut
	 */
	public EnVOException() {
		super();
	}
	
	/**
	 * Constructeur
	 * @param string
	 */
	public EnVOException(String message) {
		super(message);
	}

	/**
	 * Constructeur
	 * @param message
	 * @param cause
	 */
	public EnVOException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructeur
	 * @param cause
	 */
	public EnVOException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructeur d'une exception englobant plusieurs exceptions
	 * @param exceptions
	 */
	public EnVOException(List<EnVOException> exceptions) {
		super();
		this.exceptions = exceptions;
	}

	/* (non-Javadoc)
	 * @see java.lang.Throwable#printStackTrace()
	 */
	@Override
	public void printStackTrace() {
		if ( exceptions != null && ! exceptions.isEmpty() ) {
			for ( EnVOException exception : exceptions ) {
				exception.printStackTrace();
			}
		} else {
			super.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Throwable#printStackTrace(java.io.PrintStream)
	 */
	@Override
	public void printStackTrace(PrintStream s) {
		if ( exceptions != null && ! exceptions.isEmpty() ) {
			for ( EnVOException exception : exceptions ) {
				exception.printStackTrace(s);
			}
		} else {
			super.printStackTrace(s);
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Throwable#printStackTrace(java.io.PrintWriter)
	 */
	@Override
	public void printStackTrace(PrintWriter s) {
		if ( exceptions != null && ! exceptions.isEmpty() ) {
			for ( EnVOException exception : exceptions ) {
				exception.printStackTrace(s);
			}
		} else {
			super.printStackTrace(s);
		}
	}
}
