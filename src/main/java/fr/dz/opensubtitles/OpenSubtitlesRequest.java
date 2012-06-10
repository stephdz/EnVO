package fr.dz.opensubtitles;

import java.io.File;
import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.dz.opensubtitles.exception.OpenSubtitlesException;

public class OpenSubtitlesRequest implements Serializable {
	
	private static final long serialVersionUID = 1039194045321136919L;

	// Les expressions régulières utilisées
	private static final String SERIE_REG_EXP = "^([A-Za-z0-9 .]*)[Ss]([0-9]{1,2})[EeXx]([0-9]{1,2}).*$";
	private static final int QUERY_GROUP = 1;
	private static final int SEASON_GROUP = 2;
	private static final int EPISODE_GROUP = 3;
	
	// Champs du bean
	private String lang;
	private String folder;
	private String filename;
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
		this.lang = lang;
		init(file);
	}
	
	/**
	 * Renseigne les différents champs à partir du nom du fichier
	 * @param file the file to set
	 */
	public void init(String file) {
		if ( file != null ) {
			
			// Récupération du fichier
			int folderIndex = file.lastIndexOf(File.separator);
			if ( folderIndex != -1 ) {
				this.folder = file.substring(0, folderIndex);
				this.filename = file.substring(folderIndex + File.separator.length());
			} else {
				this.folder = ".";
				this.filename = file;
			}
			
			// Utilisation d'un expression régulière pour récupérer les infos à partir du nom de fichier
			Pattern pattern = Pattern.compile(SERIE_REG_EXP);
			Matcher matcher = pattern.matcher(filename);
			if ( matcher.find() ) {
				if ( matcher.group(QUERY_GROUP) != null ) {
					this.query = matcher.group(QUERY_GROUP).replaceAll("\\.", " ").trim().toLowerCase();
				}
				if ( matcher.group(SEASON_GROUP) != null ) {
					this.season = Integer.parseInt(matcher.group(SEASON_GROUP));
				}
				if ( matcher.group(EPISODE_GROUP) != null ) {
					this.episode = Integer.parseInt(matcher.group(EPISODE_GROUP));
				}
			} else {
				if ( filename.lastIndexOf(".") != -1 ) {
					query = filename.substring(0, filename.lastIndexOf(".")); 
				} else {
					query = filename;
				}
				query = query.replaceAll("\\.", " ").trim().toLowerCase();
			}
			
			// Récupération de la taille du fichier
			File fileHandler = new File(file);
			if ( fileHandler.exists() ) {
				this.filesize = fileHandler.length();
			}
		}
		
		// Affichage des infos de debug
		debug(file);
	}

	/**
	 * Affichage des infos de debug
	 */
	private void debug(String file) {
		OpenSubtitles.LOGGER.debug("#####################################################################");
		OpenSubtitles.LOGGER.debug("# Requète : "+file+" (langue="+lang+")");
		OpenSubtitles.LOGGER.debug("#####################################################################");
		OpenSubtitles.LOGGER.debug(" - lang : "+lang);
		OpenSubtitles.LOGGER.debug(" - folder : "+folder);
		OpenSubtitles.LOGGER.debug(" - filename : "+filename);
		OpenSubtitles.LOGGER.debug(" - query : " + query);
		OpenSubtitles.LOGGER.debug(" - season : " + season);
		OpenSubtitles.LOGGER.debug(" - episode : " + episode);
		OpenSubtitles.LOGGER.debug(" - filesize : " + filesize);
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
	 * @return the folder
	 */
	public String getFolder() {
		return folder;
	}

	/**
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * @return the query
	 */
	public String getQuery() {
		return query;
	}

	/**
	 * @return the season
	 */
	public Integer getSeason() {
		return season;
	}

	/**
	 * @return the episode
	 */
	public Integer getEpisode() {
		return episode;
	}

	/**
	 * @return the filesize
	 */
	public Long getFilesize() {
		return filesize;
	}
}
