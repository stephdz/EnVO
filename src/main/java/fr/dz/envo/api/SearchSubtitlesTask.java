package fr.dz.envo.api;

import java.util.ArrayList;
import java.util.List;

import fr.dz.envo.exception.EnVOException;
import fr.dz.envo.util.thread.GenericTaskThread;


/**
 * Tâche parallélisable recherchant des sous-titres à partir d'une source
 */
public class SearchSubtitlesTask extends GenericTaskThread<SubtitlesResult> {
	
	// Attributs
	private SubtitlesRequest request;
	private SubtitlesSource source;
	
	/**
	 * Constructeur
	 * @param request
	 * @param source 
	 */
	public SearchSubtitlesTask(SubtitlesRequest request, SubtitlesSource source) {
		this.request = request;
		this.source = source;
	}
	
	@Override
	public List<SubtitlesResult> doTask() throws EnVOException {
		List<SubtitlesResult> results = new ArrayList<SubtitlesResult>();
		source.init(request);
		if ( source.hasSubtitles() ) {
			results.addAll(source.findSubtitles());
		}
		return results;
	}

	@Override
	public String getDescription() {
		return "Recherche de sous-titres";
	}
}
