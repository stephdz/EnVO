package fr.dz.opensubtitles;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import fr.dz.opensubtitles.exception.OpenSubtitlesException;

public class OpenSubtitlesDownloader {
	
	private static final Logger logger = Logger.getLogger(OpenSubtitlesDownloader.class);
	
	// Constantes
	public static final String OPEN_SUBTITLES_DOMAIN = "http://www.opensubtitles.org";
	private static final String QUERY_URL_START = OPEN_SUBTITLES_DOMAIN + "/fr/search2";
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
	private static final String SUBTITLES_PAGE_TEXT = "<fieldset><legend>Détails des sous-titres</legend><div";
	private static final String SUBTITLE_URL_PREFIX = "/fr/subtitleserve/sub/";
	private static final String SUBTITLE_URL_START = "<a href=\"" + SUBTITLE_URL_PREFIX;
	private static final String SUBTITLE_URL_END = "\"";
	
	// La requète
	private OpenSubtitlesRequest request;
	private URL queryURL;
	private String queryResultPage;
	private List<OpenSubtitlesResult> subtitlesResults;
	
	/**
	 * Constructeur à partir d'une recherche dans OpenSubtitles
	 * @param request
	 * @throws OpenSubtitlesException
	 */
	public OpenSubtitlesDownloader(OpenSubtitlesRequest request) throws OpenSubtitlesException {
		setRequest(request);
	}
	
	/**
	 * Constructeur à partir d'une page de résultat de OpenSubtitles (pour simplifier les tests)
	 * @param htmlFile
	 * @throws OpenSubtitlesException
	 */
	public OpenSubtitlesDownloader(String htmlFile) throws OpenSubtitlesException {
		this.queryResultPage = getFileContent(htmlFile);
	}

	/**
	 * Exécute la recherche de sous-titres et retourne true si au moins un résultat a été trouvé, false sinon
	 * @return
	 * @throws OpenSubtitlesException
	 */
	public boolean hasSubtitles() throws OpenSubtitlesException {
		
		logger.debug("Exécution de la requète : "+queryURL);
		logger.debug("Recherche de : "+NO_RESULT_STRING);
        
        // Récupération de la page de résultat de la requète
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd-hhmm");
        File saveFolder = new File("html");
        saveFolder.mkdirs();
        File html = new File(saveFolder.getPath()+"/"+request.getFile()+"-"+format.format(new Date())+".html");
        this.queryResultPage = getURLContent(queryURL, html);
        
        return queryResultPage.indexOf(NO_RESULT_STRING) != -1;
	}

	/**
	 * Télécharge les premiers sous titres correspondants
	 * @throws OpenSubtitlesException
	 */
	public void downloadFirstSubtitles() throws OpenSubtitlesException {
		
		// Récupération de la liste des URLs de sous-titres 
		this.subtitlesResults = getSubtitlesURLs();
	}
	
	/**
	 * Retourne le contenu correspondant à une URL
	 * @param url
	 * @return
	 * @throws OpenSubtitlesException 
	 */
	public static String getURLContent(URL url) throws OpenSubtitlesException {
		return getURLContent(url, null);
	}
	
	/**
	 * Retourne le contenu correspondant à une URL et stocke le résultat dans un fichier à des fins de logs
	 * @param url
	 * @param resultFile
	 * @return
	 * @throws OpenSubtitlesException 
	 */
	public static String getURLContent(URL url, File resultFile) throws OpenSubtitlesException {
		BufferedReader reader = null;
		BufferedWriter writer = null;
		try {
			// Création de la connexion
			URLConnection connection = url.openConnection();
	        connection.setRequestProperty("User-Agent", USER_AGENT);
	        connection.connect();
	        
	        // Récupération de la page de résultat de la requète
	        reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
	        if ( resultFile != null ) {
	        	writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(resultFile), "UTF-8"));
	        }
	        String line = null;
	        StringBuffer pageBuffer = new StringBuffer();
	        while ( (line=reader.readLine()) != null ) {
	        	if ( writer != null ) {
		        	writer.write(line);
		        	writer.newLine();
	        	}
	        	pageBuffer.append(line+"\n");
	        }
	        
	        return pageBuffer.toString();
		} catch(IOException e) {
			throw new OpenSubtitlesException("Erreur pendant l'exécution de la requète : "+url, e);
		} finally {
			if ( reader != null ) {
				try {
					reader.close();
				} catch (IOException e) {
					throw new OpenSubtitlesException("Erreur pendant l'exécution de la requète : "+url, e);
				}
			}
			if ( writer != null ) {
				try {
					writer.close();
				} catch (IOException e) {
					throw new OpenSubtitlesException("Erreur pendant l'exécution de la requète : "+url, e);
				}
			}
		}
	}
	
	/**
	 * Retourne le contenu d'un fichier sous forme de chaîne
	 * @param fileName
	 * @return
	 * @throws OpenSubtitlesException 
	 */
	public static String getFileContent(String fileName) throws OpenSubtitlesException {
		BufferedReader reader = null;
        try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileName)), "UTF-8"));
			String line = null;
	        StringBuffer fileBuffer = new StringBuffer();
	        while ( (line=reader.readLine()) != null ) {
	        	fileBuffer.append(line+"\n");
	        }
	        
	        return fileBuffer.toString();
		} catch (IOException e) {
			throw new OpenSubtitlesException("Erreur pendant la lecture du fichier : "+fileName, e);
		} finally {
			if ( reader != null ) {
				try {
					reader.close();
				} catch (IOException e) {
					throw new OpenSubtitlesException("Erreur pendant la lecture du fichier : "+fileName, e);
				}
			}
		}
	}
	
	/**
	 * Récupère la liste des sous titres depuis la page de résultat de requète
	 * @return
	 * @throws OpenSubtitlesException 
	 */
	private List<OpenSubtitlesResult> getSubtitlesURLs() throws OpenSubtitlesException {
		List<OpenSubtitlesResult> result = new ArrayList<OpenSubtitlesResult>();
		
		// Cas 1 : un seul résultat, on est déjà sur la bonne page
		if ( isSubtitlesPage(queryResultPage) ) {
			result.add(new OpenSubtitlesResult(queryResultPage));
		}
		// Cas 2 : plusieurs résultats, on est sur la liste des fichiers de sous-titres
		else {
			// Recherche des occurences du préfixe d'URL de sous-titre
			int start = 0;
			while ( (start = queryResultPage.indexOf(SUBTITLE_URL_START, start)) != -1 ) {
				start += SUBTITLE_URL_START.length();
				int end = queryResultPage.indexOf(SUBTITLE_URL_END, start);
				String subtitleId = queryResultPage.substring(start, end);
				logger.debug("Subtitle id : "+subtitleId);
				try {
					result.add(new OpenSubtitlesResult(new URL(OPEN_SUBTITLES_DOMAIN+SUBTITLE_URL_PREFIX+subtitleId)));
				} catch (MalformedURLException e) {
					throw new OpenSubtitlesException("URL invalide : "+OPEN_SUBTITLES_DOMAIN+SUBTITLE_URL_PREFIX+subtitleId);
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Retourne true si le contenu de la page passé en paramètres correspond à une page de sous-titres
	 * @param pageContent
	 * @return
	 */
	private boolean isSubtitlesPage(String pageContent) {
		return pageContent.indexOf(SUBTITLES_PAGE_TEXT) != -1;
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

	/**
	 * @return the subtitlesResults
	 */
	public List<OpenSubtitlesResult> getSubtitlesResults() {
		return subtitlesResults;
	}
}
