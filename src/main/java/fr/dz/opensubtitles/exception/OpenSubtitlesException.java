package fr.dz.opensubtitles.exception;

public class OpenSubtitlesException extends Exception {

	private static final long serialVersionUID = -7645997795284164044L;

	/**
	 * Constructeur par défaut
	 */
	public OpenSubtitlesException() {
		super();
	}
	
	/**
	 * Constructeur
	 * @param string
	 */
	public OpenSubtitlesException(String message) {
		super(message);
	}

	/**
	 * Constructeur
	 * @param message
	 * @param cause
	 */
	public OpenSubtitlesException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructeur
	 * @param cause
	 */
	public OpenSubtitlesException(Throwable cause) {
		super(cause);
	}
}
