package fr.dz.opensubtitles;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import fr.dz.opensubtitles.util.Levenshtein;

public class OpenSubtitlesResult implements Serializable {

	private static final long serialVersionUID = 5630975519309879850L;
	
	// Constantes
	private static final Integer DEFAULT_SCORING = 100;
	private static final Integer TRUSTED_COEFFICIENT = 95;
	
	// Champs
	private String id;
	private URL downloadURL;
	private List<OpenSubtitlesResultFile> files = new ArrayList<OpenSubtitlesResultFile>();
	private Boolean trusted;
	private Integer scoring;

	/**
	 * Constructeur par défaut 
	 */
	public OpenSubtitlesResult() {
		
	}
	
	/**
	 * Ajoute un fichier correspondant dans la liste
	 * @param request
	 * @param file
	 */
	public void addFile(OpenSubtitlesRequest request, OpenSubtitlesResultFile file) {
		
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
	public void doScoring(OpenSubtitlesRequest request) {
		
		// On prend le score minimum des fichiers
		List<Integer> scorings = new ArrayList<Integer>();
		for ( OpenSubtitlesResultFile file : getFiles() ) {
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
		OpenSubtitles.LOGGER.debug("#####################################################################");
		OpenSubtitles.LOGGER.debug("# Résultat trouvé : ");
		OpenSubtitles.LOGGER.debug("#####################################################################");
		OpenSubtitles.LOGGER.debug(" - id : " + id);
		OpenSubtitles.LOGGER.debug(" - downloadURL : " + downloadURL);
		OpenSubtitles.LOGGER.debug(" - files : ");
		for ( OpenSubtitlesResultFile file : files ) {
			file.debug();
		}
		OpenSubtitles.LOGGER.debug(" - trusted : " + trusted);
		OpenSubtitles.LOGGER.debug(" - scoring : " + scoring);
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

	public List<OpenSubtitlesResultFile> getFiles() {
		return files;
	}

	public void setFiles(List<OpenSubtitlesResultFile> files) {
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
