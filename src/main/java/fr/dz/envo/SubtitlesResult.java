package fr.dz.envo;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import fr.dz.envo.util.Levenshtein;

public class SubtitlesResult implements Serializable {

	private static final long serialVersionUID = 5630975519309879850L;
	
	// Constantes
	private static final Integer DEFAULT_SCORING = 100;
	private static final Integer TRUSTED_COEFFICIENT = 95;
	
	// Champs
	private String id;
	private URL downloadURL;
	private List<SubtitlesResultFile> files = new ArrayList<SubtitlesResultFile>();
	private Boolean trusted;
	private Integer scoring;

	/**
	 * Constructeur par défaut 
	 */
	public SubtitlesResult() {
		
	}
	
	/**
	 * Ajoute un fichier correspondant dans la liste
	 * @param request
	 * @param file
	 */
	public void addFile(SubtitlesRequest request, SubtitlesResultFile file) {
		
		// Ajout dans la liste
		this.files.add(file);
		
		// Scoring
		file.doScoring(request);
	}
	
	/**
	 * Methode qui note le résultat en fonction de la requète initiale
	 * Plus la valeur est faible, mieux c'est
	 * @param request
	 */
	public void doScoring(SubtitlesRequest request) {
		
		// On prend le score minimum des fichiers
		List<Integer> scorings = new ArrayList<Integer>();
		for ( SubtitlesResultFile file : getFiles() ) {
			scorings.add(file.getScoring());
		}
		this.scoring = Levenshtein.min(scorings.toArray(new Integer[0]));
		if ( this.scoring == null ) {
			this.scoring = DEFAULT_SCORING;
		}
		
		// Si le fichier est trusted, on accorde 5% de bonus
		if ( trusted ) {
			this.scoring = this.scoring * TRUSTED_COEFFICIENT / 100;
		}
		
		// Debug
		debug();
	}	
	
	/**
	 * Affichage des infos de debug
	 */
	private void debug() {
		EnVO.LOGGER.debug("#####################################################################");
		EnVO.LOGGER.debug("# Résultat trouvé : ");
		EnVO.LOGGER.debug("#####################################################################");
		EnVO.LOGGER.debug(" - id : " + id);
		EnVO.LOGGER.debug(" - downloadURL : " + downloadURL);
		EnVO.LOGGER.debug(" - files : ");
		for ( SubtitlesResultFile file : files ) {
			file.debug();
		}
		EnVO.LOGGER.debug(" - trusted : " + trusted);
		EnVO.LOGGER.debug(" - scoring : " + scoring);
	}
	
	/*
	 * GETTERS & SETTERS
	 */

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public URL getDownloadURL() {
		return downloadURL;
	}

	public void setDownloadURL(URL downloadURL) {
		this.downloadURL = downloadURL;
	}

	public List<SubtitlesResultFile> getFiles() {
		return files;
	}

	public void setFiles(List<SubtitlesResultFile> files) {
		this.files = files;
	}

	public Boolean getTrusted() {
		return trusted;
	}

	public void setTrusted(Boolean trusted) {
		this.trusted = trusted;
	}

	public Integer getScoring() {
		return scoring;
	}

	public void setScoring(Integer scoring) {
		this.scoring = scoring;
	}
}
