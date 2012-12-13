package fr.dz.opensubtitles.sources;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import fr.dz.opensubtitles.OpenSubtitles;
import fr.dz.opensubtitles.OpenSubtitlesRequest;
import fr.dz.opensubtitles.OpenSubtitlesResult;
import fr.dz.opensubtitles.OpenSubtitlesResultFile;
import fr.dz.opensubtitles.exception.OpenSubtitlesException;

public class PodnapisiDownloader extends AbstractOpenSubtitlesSource {
	
	// Constantes pour construire l'URL de recherche
	public static final String PODNAPISI_DOMAIN = "http://www.podnapisi.net";
	private static final String QUERY_URL_START = PODNAPISI_DOMAIN + "/fr/ppodnapisi/search?sT=1";
	private static final String FILENAMES_URL_PREFIX = PodnapisiDownloader.PODNAPISI_DOMAIN + "/fr/moviefilename?idsubmoviefile=";
	private static final String PARAM_NAME_VALUE_SEPARATOR = "=";
	private static final String PARAM_SEPARATOR = "&";
	private static final String LANGUAGE_PARAM_NAME = "sJ";
	private static final String SEASON_PARAM_NAME = "sTS";
	private static final String EPISODE_PARAM_NAME = "sTE";
	private static final String MOVIE_PARAM_NAME = "sK";
	
	// Constantes de sélection CSS
	private static final String HAS_RESULT_SELECTOR = ".premium_download";
	private static final String RESULT_SELECTOR = ".subtitle_page_link";
	private static final String HREF_ATTRIBUTE = "href";
	
	// TODO Constantes à supprimer
	private static final String TRUSTED_IMAGE = "http://static.opensubtitles.org/gfx/icons/ranks/trusted.png";
		
	// L'URL de la requète
	private URL queryURL;
	
	// Le contenu de la page résultat de recherche
	private String queryResultPage;
	
	// Le document contenu de la page résultat de recherche
	private Document queryResultDocument;
	
	/**
	 * Constructeur à partir d'une recherche dans OpenSubtitles
	 * @param request
	 * @throws OpenSubtitlesException
	 */
	public PodnapisiDownloader(OpenSubtitlesRequest request) throws OpenSubtitlesException {
		super(request);
		
		// Construction de l'URL de la requète
		StringBuffer queryURLBuffer = new StringBuffer();
		queryURLBuffer.append(QUERY_URL_START);
		if ( request.getLang() != null ) {
			appendParameter(queryURLBuffer, LANGUAGE_PARAM_NAME, getLanguageId(request.getLang()));
		} else {
			throw new OpenSubtitlesException("La langue est obligatoire pour le fichier "+request.getFilename());
		}
		if ( request.getSeason() != null ) {
			appendParameter(queryURLBuffer, SEASON_PARAM_NAME, request.getSeason());
		}
		if ( request.getEpisode() != null ) {
			appendParameter(queryURLBuffer, EPISODE_PARAM_NAME, request.getEpisode());
		}
		if ( request.getQuery() != null ) {
			try {
				// FIXME Podnapisi n'est pas très permissif => virer les [LOL] ou choses du genre du nom du fichier pour les films
				appendParameter(queryURLBuffer, MOVIE_PARAM_NAME, URLEncoder.encode(request.getQuery(), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				throw new OpenSubtitlesException("Erreur pendant l'encodage de '"+request.getQuery()+"' pour l'URL", e);
			}
		} else {
			throw new OpenSubtitlesException("La requète est obligatoire pour le fichier "+request.getFilename());
		}
		try {
			this.queryURL = new URL(queryURLBuffer.toString());
		} catch (MalformedURLException e) {
			throw new OpenSubtitlesException("URL générée invalide : "+queryURLBuffer.toString(), e);
		}
	}

	@Override
	public boolean hasSubtitles() throws OpenSubtitlesException {
		
		OpenSubtitles.LOGGER.debug("#####################################################################");
		OpenSubtitles.LOGGER.debug("# Exécution de la requète : "+queryURL);
		OpenSubtitles.LOGGER.debug("#####################################################################");
		
        // Récupération de la page de résultat de la requète
        this.queryResultPage = getURLContent(queryURL);
        this.queryResultDocument = getJsoupDocument(queryResultPage);
        
        // Recherche du warning indiquant qu'il n'y a pas de résultat
        boolean resultat = queryResultDocument.select(HAS_RESULT_SELECTOR).size() > 0;
        
        return resultat;
	}
	
	@Override
	protected List<OpenSubtitlesResult> getSubtitlesURLs() throws OpenSubtitlesException {
		List<OpenSubtitlesResult> result = new ArrayList<OpenSubtitlesResult>();
		
		// Recherche des URL pour chaque résultat de recherche
		Elements elements = queryResultDocument.select(RESULT_SELECTOR);
		for ( Element link : elements ) {
			String url = PODNAPISI_DOMAIN+link.attr(HREF_ATTRIBUTE);
			try {
				result.add(createResult(getURLContent(new URL(url))));
			} catch (MalformedURLException e) {
				throw new OpenSubtitlesException("URL invalide : "+url);
			}
		}
		
		return result;
	}
	
	/**
	 * Ajoute un paramètre à la requète
	 * @param buf
	 * @param param
	 * @param value
	 */
	protected void appendParameter(StringBuffer buf, String param, Object value) {
		buf.append(PARAM_SEPARATOR);
		buf.append(param);
		buf.append(PARAM_NAME_VALUE_SEPARATOR);
		buf.append(value);
	}
	
	/**
	 * TODO A adapter
	 * Crée un résultat à partir d'un contenu de page de sous-titre
	 * @param subtitlePage
	 * @return
	 */
	protected OpenSubtitlesResult createResult(String subtitlePage) throws OpenSubtitlesException {
		OpenSubtitlesResult result = new OpenSubtitlesResult();
		Document subtitleDocument = getJsoupDocument(subtitlePage);
		
		// Récupération de l'id (dernière partie de l'URL)
		String id = subtitleDocument.select(".information.parallel:eq(0) p:eq(0) span:eq(1)").text();
		result.setId(id);
		
		// Récupération de l'URL
		String url = subtitleDocument.select(".big.download.button").attr(HREF_ATTRIBUTE);
		try {
			result.setDownloadURL(new URL(PODNAPISI_DOMAIN+url));
		} catch (MalformedURLException e) {
			throw new OpenSubtitlesException("URL invalide : "+PODNAPISI_DOMAIN+url, e);
		}
		
		// Récupération des fichiers correspondants
		// Pour chaque élément du div contenant les descriptions des fichiers
		Elements links = subtitleDocument.select(".information.parallel + fieldset:not(.information.parallel) p a");
		for ( Element link : links ) {
			
			// Pas de taille sur Podnapisi
			Long size = null;
			
			// Un seul fichier par nom de fichier et l'id est aussi le nom de fichier (pas d'info supplémentaire)
			List<String> filenames = new ArrayList<String>();
			String filename = link.text();
			filenames.add(filename);
			String fileId = filename;
			
			// Création du fichier résultat
			OpenSubtitlesResultFile file = new OpenSubtitlesResultFile();
			file.setId(fileId);
			file.setSize(size);
			file.setFileNames(filenames);
			result.addFile(getRequest(), file);
		}
		
		// Récupération de l'info "Posteur de confiance"
		// FIXME Cette info n'est pas dispo dans Podnapisi, attention à l'effet sur le scoring => utiliser les votes
		result.setTrusted(false);
		
		// Scoring
		result.doScoring(getRequest());
		
		return result;
	}
	
	/**
	 * Rétourne l'id de la lanque sur Podnapisi à partir de son code
	 * @param lang
	 * @return
	 */
	private String getLanguageId(String lang) {
		// TODO Faire une table de correspondance
		return "8";
	}

	/*
	 * GETTERS & SETTERS
	 */
	
	public URL getQueryURL() {
		return queryURL;
	}

	public String getQueryResultPage() {
		return queryResultPage;
	}
}
