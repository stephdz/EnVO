package fr.dz.envo.api;

import java.util.List;

import fr.dz.envo.exception.EnVOException;


public interface SubtitlesSource {

	/**
	 * Initialisation de la source à partir d'une requète
	 * @param request
	 * @throws EnVOException
	 */
	public void init(SubtitlesRequest request) throws EnVOException;
	
	/**
	 * Exécute la recherche de sous-titres et retourne true si au moins un résultat a été trouvé, false sinon
	 * @return
	 * @throws EnVOException
	 */
	public boolean hasSubtitles() throws EnVOException;

	/**
	 * Récupère la liste des sous titres depuis la page de résultat de requète
	 * @return
	 * @throws EnVOException 
	 */
	public List<SubtitlesResult> findSubtitles() throws EnVOException;
}
