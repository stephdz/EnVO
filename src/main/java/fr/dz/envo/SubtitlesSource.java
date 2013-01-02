package fr.dz.envo;

import fr.dz.envo.exception.EnVOException;


public interface SubtitlesSource {

	/**
	 * Exécute la recherche de sous-titres et retourne true si au moins un résultat a été trouvé, false sinon
	 * @return
	 * @throws EnVOException
	 */
	public boolean hasSubtitles() throws EnVOException;

	/**
	 * Télécharge les premiers sous titres correspondants
	 * @throws EnVOException
	 */
	public boolean downloadFirstSubtitles() throws EnVOException;
}
