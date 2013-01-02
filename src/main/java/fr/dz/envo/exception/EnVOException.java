package fr.dz.envo.exception;

public class EnVOException extends Exception {

	private static final long serialVersionUID = -7645997795284164044L;

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
}
