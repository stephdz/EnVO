package fr.dz.opensubtitles;

import fr.dz.opensubtitles.exception.OpenSubtitlesException;

public class OpenSubtitles {

	/**
	 * Utilitaire de téléchargement de sous-titres depuis OpenSubtitles
	 * @param args Arguments : <langue> <nom_de_fichier>
	 */
	public static void main(String[] args) {
		
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
