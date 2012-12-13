package fr.dz.opensubtitles.sources;

import fr.dz.opensubtitles.exception.OpenSubtitlesException;


public interface OpenSubtitlesSource {

	/**
	 * Exécute la recherche de sous-titres et retourne true si au moins un résultat a été trouvé, false sinon
	 * @return
	 * @throws OpenSubtitlesException
	 */
	public boolean hasSubtitles() throws OpenSubtitlesException;

	/**
	 * Télécharge les premiers sous titres correspondants
	 * @throws OpenSubtitlesException
	 */
	public boolean downloadFirstSubtitles() throws OpenSubtitlesException;
}
