package fr.dz.envo.util.thread;

import java.util.ArrayList;
import java.util.List;

import fr.dz.envo.exception.EnVOException;

/**
 * Classe définissant un ensemble d'opération à exécuter en parallèle
 * @param <ReturnType>
 */
public abstract class GenericThreadManager<ReturnType> {
	
	// Constantes
	private static final long WAIT_TIME_MILLIS = 100;
	
	// Attributs
	private List<GenericTaskThread<ReturnType>> threads;

	/**
	 * Constructeur par défaut (protected pour que les classes qui en héritent puissent créer les threads elles-mêmes)
	 * @param request
	 */
	protected GenericThreadManager() {
		
	}
	
	/**
	 * Constructeur
	 * @param request
	 */
	public GenericThreadManager(List<GenericTaskThread<ReturnType>> threads) {
		init(threads);
	}
	
	/**
	 * Méthode à redéfinir si une opération est à effectuer après l'exécution de toutes les tâches
	 * @throws EnVOException
	 */
	public void postOperation(List<ReturnType> results) throws EnVOException {}
	
	/**
	 * Exécution en parallèle des différentes opérations
	 * @throws EnVOException
	 */
	public List<ReturnType> doTasks() throws EnVOException {
		
		// Démarrage des threads
		for ( GenericTaskThread<ReturnType> thread : threads ) {
			thread.start();
		}
		
		// Attente de la fin des traitements
		boolean aliveThreads = true;
		while ( aliveThreads ) {
			aliveThreads = false;
			for ( GenericTaskThread<ReturnType> thread : threads ) {
				if ( thread.isAlive() ) {
					aliveThreads = true;
					break;
				}
			}
			if ( aliveThreads ) {
				try {
					Thread.sleep(WAIT_TIME_MILLIS);
				} catch (InterruptedException e) {
					throw new EnVOException("Erreur pendant l'attente des threads", e);
				}
			}
		}
		
		// Récupération des résultats et des exceptions
		List<ReturnType> results = new ArrayList<ReturnType>();
		List<EnVOException> exceptions = new ArrayList<EnVOException>();
		for ( GenericTaskThread<ReturnType> thread : threads ) {
			results.addAll(thread.getResults());
			if ( thread.getException() != null ) {
				exceptions.add(thread.getException());
			}
		}
		
		// Traitement d'exception
		if ( ! exceptions.isEmpty() ) {
			throw new EnVOException(exceptions);
		}
		// Exécution du post traitement
		else {
			postOperation(results);
		}
		
		return results;
	}
	
	/**
	 * Initialisation déportée pour pouvoir être utilisée par les constructeurs des classes descendantes
	 * @param threads
	 */
	protected void init(List<GenericTaskThread<ReturnType>> threads) {
		this.threads = threads;
	}
}
