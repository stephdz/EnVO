package fr.dz.envo.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import fr.dz.envo.exception.EnVOException;
import fr.dz.envo.util.thread.GenericTaskThread;
import fr.dz.envo.util.thread.GenericThreadManager;


/**
 * Classe gérant la recherche parallélisée de sous-titre par de multiples sources
 */
public class SearchSubtitlesManager extends GenericThreadManager<SubtitlesResult> {
	
	// Attributs
	private SubtitlesRequest request;
	private Map<String,SubtitlesSource> sources;

	/**
	 * Constructeur
	 * @param request
	 */
	public SearchSubtitlesManager(SubtitlesRequest request) {
		super();
		this.request = request;
		
		// Initialisation du contexte Spring pour récupérer les downloaders
		ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
		this.sources = context.getBeansOfType(SubtitlesSource.class);
		
		// Création d'un thread par source
		List<GenericTaskThread<SubtitlesResult>> threads = new ArrayList<GenericTaskThread<SubtitlesResult>>(sources.size());
		for ( SubtitlesSource source : sources.values() ) {
			threads.add(new SearchSubtitlesTask(request, source));
		}
		init(threads);
	}

	/* (non-Javadoc)
	 * @see fr.dz.envo.util.GenericThreadManager#postOperation(java.util.List)
	 */
	@Override
	public void postOperation(List<SubtitlesResult> results) throws EnVOException {
		AbstractSubtitlesSource.downloadBestSubtitles(request, results);
	}
}
