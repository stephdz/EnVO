package fr.dz.opensubtitles;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import fr.dz.opensubtitles.exception.OpenSubtitlesException;

public class OpenSubtitles {
	
	public static final Logger LOGGER = Logger.getLogger(OpenSubtitles.class.getPackage().getName());

	/**
	 * Utilitaire de téléchargement de sous-titres depuis OpenSubtitles
	 * @param args Arguments : <langue> <nom_de_fichier>
	 */
	public static void main(String[] args) {
		
		LOGGER.setLevel(Level.INFO);
		
		// Nombre d'arguments incorrects
		if ( args.length != 2 ) {
			System.err.println("Arguments : <langue> <nom_de_fichier>");
			return;
		}
		
		try {
			// Création de la requète
			OpenSubtitlesRequest request = new OpenSubtitlesRequest(args[0], args[1]);
			
			// Recherche des sous titres existants
			OpenSubtitlesDownloader downloader = new OpenSubtitlesDownloader(request);
			if ( downloader.hasSubtitles() ) {
				downloader.downloadFirstSubtitles();
			}
		} catch (OpenSubtitlesException e) {
			System.err.println(e.getMessage());
		}
	}
}
