package fr.dz.envo.util;

import java.util.ArrayList;
import java.util.List;

import fr.dz.envo.api.SubtitlesRequest;
import fr.dz.envo.api.SubtitlesResult;
import fr.dz.envo.api.SubtitlesSource;
import fr.dz.envo.exception.EnVOException;

public class DownloaderThread extends Thread {
	
	private SubtitlesRequest request;
	private SubtitlesSource source;
	private List<SubtitlesResult> results;
	private EnVOException exception;
	
	/**
	 * Constructeur
	 * @param request
	 * @param source 
	 */
	public DownloaderThread(SubtitlesRequest request, SubtitlesSource source) {
		this.request = request;
		this.source = source;
		this.results = new ArrayList<SubtitlesResult>();
	}

	@Override
	public void run() {
		try {
			source.init(request);
			if ( source.hasSubtitles() ) {
				results.addAll(source.findSubtitles());
			}
		} catch(EnVOException e) {
			this.exception = new EnVOException("Erreur pendant la récupération des résultats de la source "+source.getClass().getSimpleName(), e);
		}
	}
	
	/*
	 * GETTERS
	 */

	/**
	 * @return the results
	 */
	public List<SubtitlesResult> getResults() {
		return results;
	}

	/**
	 * @return the exception
	 */
	public EnVOException getException() {
		return exception;
	}

	/**
	 * @return the source
	 */
	public SubtitlesSource getSource() {
		return source;
	}
}
