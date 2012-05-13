package fr.dz.opensubtitles;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.apache.log4j.Logger;

import fr.dz.opensubtitles.exception.OpenSubtitlesException;

public class OpenSubtitlesDownloader {
	
	private static final Logger logger = Logger.getLogger(OpenSubtitlesDownloader.class);
	
	// Constantes
	private static final String QUERY_URL_START = "http://www.opensubtitles.org/fr/search2";
	private static final String PARAM_NAME_VALUE_SEPARATOR = "-";
	private static final String PARAM_SEPARATOR = "/";
	private static final String LANGUAGE_PARAM_NAME = "sublanguageid";
	private static final String SEASON_PARAM_NAME = "season";
	private static final String EPISODE_PARAM_NAME = "episode";
	private static final String FORMAT_PARAM_NAME = "subformat";
	private static final String FORMAT_PARAM_VALUE = "srt";
	private static final String MOVIE_PARAM_NAME = "moviename";
	private static final String USER_AGENT = "Mozilla/5.0 (X11; Ubuntu; Linux i686; rv:12.0) Gecko/20100101 Firefox/12.0";
	private static final String NO_RESULT_STRING = "<div class=\"msg warn\"><b>Aucun résultat</b> trouvé";
	
	// La requète
	private OpenSubtitlesRequest request;
	private URL queryURL;
	private String queryResultPage;
	
	/**
	 * Constructeur
	 * @param request
	 * @throws OpenSubtitlesException
	 */
	public OpenSubtitlesDownloader(OpenSubtitlesRequest request) throws OpenSubtitlesException {
		setRequest(request);
	}

	/**
	 * Exécute la recherche de sous-titres et retourne true si au moins un résultat a été trouvé, false sinon
	 * @return
	 * @throws OpenSubtitlesException
	 */
	public boolean hasSubtitles() throws OpenSubtitlesException {
		BufferedReader reader = null;
		try {
			logger.debug("#################################################################");
			logger.debug("###### Exécution de la requète : "+queryURL);
			logger.debug("#################################################################");
			
			// Création de la connexion
			URLConnection connection = queryURL.openConnection();
	        connection.setRequestProperty("User-Agent", USER_AGENT);
	        connection.connect();
	        
	        // Récupération de la page de résultat de la requète
	        reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	        boolean hasSubtitles = true;
	        String line = null;
	        StringBuffer queryResultPageBuffer = new StringBuffer();
	        while ( (line=reader.readLine()) != null ) {
	        	queryResultPageBuffer.append(line+"\n");
	        	logger.debug(line);
	        	if ( line.indexOf(NO_RESULT_STRING) != -1 ) {
	        		hasSubtitles = false;
	        	}
	        }
	        
	        return hasSubtitles;
		} catch(IOException e) {
			throw new OpenSubtitlesException("Erreur pendant l'exécution de la requète : "+queryURL.toString(), e);
		} finally {
			if ( reader != null ) {
				try {
					reader.close();
				} catch (IOException e) {
					throw new OpenSubtitlesException("Erreur pendant l'exécution de la requète : "+queryURL.toString(), e);
				}
			}
		}
	}

	/**
	 * Télécharge les premiers sous titres correspondants
	 * @throws OpenSubtitlesException
	 */
	public void downloadFirstSubtitles() throws OpenSubtitlesException {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Renseigne les différents champs à partir de la requète
	 * @param request the request to set
	 */
	private void setRequest(OpenSubtitlesRequest request) throws OpenSubtitlesException {
		this.request = request;
		
		// Construction de l'URL de la requète
		StringBuffer queryURLBuffer = new StringBuffer();
		queryURLBuffer.append(QUERY_URL_START);
		if ( request.getLang() != null ) {
			appendParameter(queryURLBuffer, LANGUAGE_PARAM_NAME, request.getLang());
		} else {
			throw new OpenSubtitlesException("La langue est obligatoire pour le fichier "+request.getFile());
		}
		if ( request.getSeason() != null ) {
			appendParameter(queryURLBuffer, SEASON_PARAM_NAME, request.getSeason());
		}
		if ( request.getEpisode() != null ) {
			appendParameter(queryURLBuffer, EPISODE_PARAM_NAME, request.getEpisode());
		}
		appendParameter(queryURLBuffer, FORMAT_PARAM_NAME, FORMAT_PARAM_VALUE);
		if ( request.getQuery() != null ) {
			try {
				appendParameter(queryURLBuffer, MOVIE_PARAM_NAME, URLEncoder.encode(request.getQuery(), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				throw new OpenSubtitlesException("Erreur pendant l'encodage de '"+request.getQuery()+"' pour l'URL", e);
			}
		} else {
			throw new OpenSubtitlesException("La requète est obligatoire pour le fichier "+request.getFile());
		}
		try {
			this.queryURL = new URL(queryURLBuffer.toString());
		} catch (MalformedURLException e) {
			throw new OpenSubtitlesException("URL générée invalide : "+queryURLBuffer.toString(), e);
		}
	}
	
	/**
	 * Ajoute un paramètre à la requète
	 * @param buf
	 * @param param
	 * @param value
	 */
	private void appendParameter(StringBuffer buf, String param, Object value) {
		buf.append(PARAM_SEPARATOR);
		buf.append(param);
		buf.append(PARAM_NAME_VALUE_SEPARATOR);
		buf.append(value);
	}
	
	/*
	 * GETTERS & SETTERS
	 */
	
	/**
	 * @return the request
	 */
	public OpenSubtitlesRequest getRequest() {
		return request;
	}

	/**
	 * @return the queryURL
	 */
	public URL getQueryURL() {
		return queryURL;
	}

	/**
	 * @return the queryResultPage
	 */
	public String getQueryResultPage() {
		return queryResultPage;
	}
}
