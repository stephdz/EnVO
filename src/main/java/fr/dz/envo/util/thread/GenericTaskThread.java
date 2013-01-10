package fr.dz.envo.util.thread;

import java.util.ArrayList;
import java.util.List;

import fr.dz.envo.exception.EnVOException;

/**
 * Classe abstraite définissant une opération éxécutable dans un thread et retournant une liste de résultats
 * @param <ReturnType>
 */
public abstract class GenericTaskThread<ReturnType> extends Thread {
	
	private List<ReturnType> results = new ArrayList<ReturnType>();
	private EnVOException exception;
	
	/**
	 * Effectue l'opération parallélisable
	 * @throws EnVOException
	 */
	public abstract List<ReturnType> doTask() throws EnVOException;
	
	/**
	 * Retourne la description de l'opération
	 * @return
	 */
	public abstract String getDescription();

	@Override
	public void run() {
		try {
			this.results = doTask();
		} catch(Throwable t) {
			this.exception = new EnVOException("Erreur pendant l'exécution de l'opération '"+getDescription()+"'", t);
		}
	}
	
	/*
	 * GETTERS
	 */

	/**
	 * @return the results
	 */
	public List<ReturnType> getResults() {
		return results;
	}

	/**
	 * @return the exception
	 */
	public EnVOException getException() {
		return exception;
	}
}
