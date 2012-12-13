package fr.dz.opensubtitles;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import fr.dz.opensubtitles.exception.OpenSubtitlesException;
import fr.dz.opensubtitles.sources.OpenSubtitlesDownloader;

public class OpenSubtitles {
	
	public static final Logger LOGGER = Logger.getLogger(OpenSubtitles.class.getPackage().getName());

	// Constantes
	public static final String VERBOSE_OPTION = "-v";

	/**
	 * Utilitaire de téléchargement de sous-titres depuis OpenSubtitles
	 * @param args Arguments : <options> <langue> <nom_de_fichier>
	 */
	public static void main(String[] args) {
		
		// Recherche d'une option verbose
		List<String> options = prepareOptions(args);
		if ( options.contains(VERBOSE_OPTION) ) {
			options.remove(VERBOSE_OPTION);
		} else {
			LOGGER.setLevel(Level.INFO);
		}
		
		// Nombre d'arguments incorrects
		if ( options.size() != 2 ) {
			System.err.println("Arguments : <options> <langue> <nom_de_fichier>");
			return;
		}
		
		try {
			// Création de la requète
			OpenSubtitlesRequest request = new OpenSubtitlesRequest(options.get(0), options.get(1));
			
			// Recherche des sous titres existants
			OpenSubtitlesDownloader downloader = new OpenSubtitlesDownloader(request);
			if ( downloader.hasSubtitles() ) {
				downloader.downloadFirstSubtitles();
			}
		} catch (OpenSubtitlesException e) {
			System.err.println(e.getMessage());
		}
	}

	/**
	 * Nettoie les options de ligne de commande et les retourne sous forme de liste pour simplifier
	 * leur traitement
	 * @param options
	 * @return
	 */
	public static List<String> prepareOptions(String[] options) {
		List<String> result = new ArrayList<String>();
		if ( options != null ) {
			for ( String option : options ) {
				option = option.trim();
				if ( ! option.isEmpty() ) {
					result.add(option);
				}
			}
		}
		return result;
	}
}
