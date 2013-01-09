package fr.dz.envo.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import fr.dz.envo.api.AbstractSubtitlesSource;
import fr.dz.envo.api.SubtitlesRequest;
import fr.dz.envo.api.SubtitlesResult;
import fr.dz.envo.api.SubtitlesSource;
import fr.dz.envo.exception.EnVOException;

public class DownloaderThreadManager {
	
	// Constantes
	private static final long WAIT_TIME_MILLIS = 100;
	
	// Attributs
	private SubtitlesRequest request;
	private Map<String,SubtitlesSource> sources;

	/**
	 * Constructeur
	 * @param request
	 */
	public DownloaderThreadManager(SubtitlesRequest request) {
		this.request = request;
		
		// Initialisation du contexte Spring pour récupérer les downloaders
		ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
		this.sources = context.getBeansOfType(SubtitlesSource.class);
	}

	/**
	 * Téléchargement des sous-titres : 1 thread par source et attente de la fin du traitement
	 * @throws EnVOException
	 */
	public void downloadSubtitles() throws EnVOException {
		
		// Création d'un thread par source
		List<DownloaderThread> threads = new ArrayList<DownloaderThread>(sources.size());
		for ( SubtitlesSource source : sources.values() ) {
			DownloaderThread thread = new DownloaderThread(request, source);
			thread.start();
			threads.add(thread);
		}
		
		// Attente de la fin des traitements
		boolean aliveThreads = true;
		while ( aliveThreads ) {
			aliveThreads = false;
			for ( DownloaderThread thread : threads ) {
				if ( thread.isAlive() ) {
					aliveThreads = true;
					break;
				}
			}
			if ( aliveThreads ) {
				try {
					Thread.sleep(WAIT_TIME_MILLIS);
				} catch (InterruptedException e) {
					throw new EnVOException("Erreur pendant l'attente des sources", e);
				}
			}
		}
		
		// Récupération des résultats et des exceptions
		List<SubtitlesResult> results = new ArrayList<SubtitlesResult>();
		List<EnVOException> exceptions = new ArrayList<EnVOException>();
		for ( DownloaderThread thread : threads ) {
			results.addAll(thread.getResults());
			if ( thread.getException() != null ) {
				exceptions.add(thread.getException());
			}
		}
		
		// Traitement d'exception
		if ( ! exceptions.isEmpty() ) {
			throw new EnVOException(exceptions);
		}
		// Téléchargement des meilleurs sous-titres
		else {
			AbstractSubtitlesSource.downloadBestSubtitles(request, results);
		}
	}

}
