package fr.dz.opensubtitles;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.dz.opensubtitles.exception.OpenSubtitlesException;

public class OpenSubtitlesRequest implements Serializable {

	private static final long serialVersionUID = 1039194045321136919L;

	// Les expressions régulières utilisées
	private static final String SERIE_REG_EXP = "^([A-Za-z0-9 .]*)S([0-9]{1,2})E([0-9]{1,2}).*$";
	private static final int QUERY_GROUP = 1;
	private static final int SEASON_GROUP = 2;
	private static final int EPISODE_GROUP = 3;
	
	// Champs du bean
	private String lang;
	private String file;
	private String query;
	private Integer season;
	private Integer episode;
	private Long filesize;
	
	/**
	 * Constructeur
	 * @param lang La langue
	 * @param file Le fichier pour lequel il faut chercher des sous-titres
	 * @throws OpenSubtitlesException
	 */
	public OpenSubtitlesRequest(String lang, String file) {
		setLang(lang);
		setFile(file);
	}
	
	/**
	 * Renseigne les différents champs à partir du nom du fichier
	 * @param file the file to set
	 */
	public void setFile(String file) {
		this.file = file;
		
		// Utilisation d'un expression régulière pour récupérer les infos à partir du nom de fichier
		if ( file != null ) {
			Pattern pattern = Pattern.compile(SERIE_REG_EXP);
			Matcher matcher = pattern.matcher(file);
			if ( matcher.find() ) {
				if ( matcher.group(QUERY_GROUP) != null ) {
					setQuery(matcher.group(QUERY_GROUP).replaceAll("\\.", " ").trim().toLowerCase());
				}
				if ( matcher.group(SEASON_GROUP) != null ) {
					setSeason(Integer.parseInt(matcher.group(SEASON_GROUP)));
				}
				if ( matcher.group(EPISODE_GROUP) != null ) {
					setEpisode(Integer.parseInt(matcher.group(EPISODE_GROUP)));
				}
			} else {
				if ( file.lastIndexOf(".") != -1 ) {
					query = file.substring(0, file.lastIndexOf(".")); 
				} else {
					query = file;
				}
				query = query.replaceAll("\\.", " ").trim().toLowerCase();
			}
		}
	}

	/*
	 * GETTERS & SETTERS
	 */
	
	/**
	 * @return the lang
	 */
	public String getLang() {
		return lang;
	}

	/**
	 * @param lang the lang to set
	 */
	public void setLang(String lang) {
		this.lang = lang;
	}

	/**
	 * @return the file
	 */
	public String getFile() {
		return file;
	}

	/**
	 * @return the query
	 */
	public String getQuery() {
		return query;
	}

	/**
	 * @param query the query to set
	 */
	public void setQuery(String query) {
		this.query = query;
	}

	/**
	 * @return the season
	 */
	public Integer getSeason() {
		return season;
	}

	/**
	 * @param season the season to set
	 */
	public void setSeason(Integer season) {
		this.season = season;
	}

	/**
	 * @return the episode
	 */
	public Integer getEpisode() {
		return episode;
	}

	/**
	 * @param episode the episode to set
	 */
	public void setEpisode(Integer episode) {
		this.episode = episode;
	}

	/**
	 * @return the filesize
	 */
	public Long getFilesize() {
		return filesize;
	}

	/**
	 * @param filesize the filesize to set
	 */
	public void setFilesize(Long filesize) {
		this.filesize = filesize;
	}

}
