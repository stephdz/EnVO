package fr.dz.opensubtitles;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import fr.dz.opensubtitles.exception.OpenSubtitlesException;

public class OpenSubtitlesResult implements Serializable {

	private static final long serialVersionUID = 5630975519309879850L;
	private static final Logger logger = Logger.getLogger(OpenSubtitlesResult.class);
	
	// Constantes
	private static final String TRUSTED_IMAGE = "http://static.opensubtitles.org/gfx/icons/from_trusted.gif";
	private static final String SUBTITLE_URL_PREFIX = "/fr/subtitles/";
	private static final String SUBTITLE_URL_START = "<link rel=\"bookmark\" href=\"" + SUBTITLE_URL_PREFIX;
	private static final String SUBTITLE_URL_END = "/";
	private static final String DOWNLOAD_SUBTITLE_URL_PREFIX = OpenSubtitlesDownloader.OPEN_SUBTITLES_DOMAIN + "/fr/subtitleserve/sub/";
	private static final String FILENAME_BEFORE_STRING_1 = "sub_subtitle_preview";
	private static final String FILENAME_BEFORE_STRING_2 = "sub_movie_file_name_";
	private static final String FILENAME_START = ">";
	private static final String FILENAME_END = "</a>";
	
	// Champs
	private String id;
	private URL downloadURL;
	private List<String> fileNames;
	private Boolean trusted;

	/**
	 * Constructeur à partir d'une page de sous-titre
	 * @param pageContent le contenu de la page
	 * @throws OpenSubtitlesException 
	 */
	public OpenSubtitlesResult(String pageContent) throws OpenSubtitlesException {
		setId(pageContent);
		setDownloadURL(pageContent);
		setFileNames(pageContent);
		setTrusted(pageContent);
	}

	/**
	 * Constructeur à partir d'une URL de page de sous-titre
	 * @param url l'URL de la page de sous-titre
	 * @throws OpenSubtitlesException 
	 */
	public OpenSubtitlesResult(URL url) throws OpenSubtitlesException {
		this(OpenSubtitlesDownloader.getURLContent(url));
	}
	
	/**
	 * Récupère l'id dans le contenu de la page
	 * @param pageContent
	 */
	private void setId(String pageContent) {
		int start = pageContent.indexOf(SUBTITLE_URL_START) + SUBTITLE_URL_START.length();
		int end = pageContent.indexOf(SUBTITLE_URL_END, start);
		this.id = pageContent.substring(start, end);
	}
	
	/**
	 * Récupère l'URL de téléchargement dans le contenu de la page
	 * @param pageContent
	 * @throws OpenSubtitlesException 
	 */
	private void setDownloadURL(String pageContent) throws OpenSubtitlesException {
		try {
			this.downloadURL = new URL(DOWNLOAD_SUBTITLE_URL_PREFIX+id);
		} catch (MalformedURLException e) {
			throw new OpenSubtitlesException("URL invalide : "+DOWNLOAD_SUBTITLE_URL_PREFIX+id, e);
		}
	}
	
	/**
	 * Récupère les noms de fichiers vidéo correspondants dans le contenu de la page
	 * @param pageContent
	 */
	private void setFileNames(String pageContent) {
		this.fileNames = new ArrayList<String>();
		int globalStart = 0;
		String searched = FILENAME_BEFORE_STRING_1;
		while ( (globalStart = pageContent.indexOf(searched, globalStart)) != -1 ) {
			
			// Recherche du nom de fichier
			int end = pageContent.indexOf(FILENAME_END, globalStart);
			String subString = pageContent.substring(globalStart, end);
			int start = subString.lastIndexOf(FILENAME_START) + 1;
			globalStart += start;
			fileNames.add(subString.substring(start));
			
			// Au premier passagen, la chaine recherchée n'est pas la même
			searched = FILENAME_BEFORE_STRING_2;
		}
		
		// Le dernier élément trouvé n'est pas un nom de fichier
		fileNames.remove(fileNames.size()-1);
	}
	
	/**
	 * Récupère l'indicateur "source fiable" dans le contenu de la page
	 * @param pageContent
	 */
	private void setTrusted(String pageContent) {
		// Il faut au moins 2 occurences du texte cherché
		int start = 0;
		int found = 0;
		while ( (start = pageContent.indexOf(TRUSTED_IMAGE, start)) != -1 ) {
			start += TRUSTED_IMAGE.length();
			found++;
		}
		this.trusted = found > 1;
	}
	
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the downloadURL
	 */
	public URL getDownloadURL() {
		return downloadURL;
	}

	/**
	 * @return the fileNames
	 */
	public List<String> getFileNames() {
		return fileNames;
	}

	/**
	 * @return the trusted
	 */
	public Boolean getTrusted() {
		return trusted;
	}
}
