package fr.dz.envo.sources;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import fr.dz.envo.api.AbstractSubtitlesSource;
import fr.dz.envo.api.SubtitlesResult;
import fr.dz.envo.api.SubtitlesResultFile;
import fr.dz.envo.exception.EnVOException;

/**
 * Recherche sur podnapisi.net
 * FIXME Ne fonctionne pas pour les films : "Blood.Diamond.avi"
 */
@Service("podnapisi")
public class PodnapisiDownloader extends AbstractSubtitlesSource {
	
	// Constantes pour construire l'URL de recherche
	private static final String PODNAPISI_DOMAIN = "http://www.podnapisi.net";
	private static final String QUERY_URL_START = PODNAPISI_DOMAIN + "/fr/ppodnapisi/search?sT=1";
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
	
	@Override
	public URL buildQueryURL() throws EnVOException {
		
		// Construction de l'URL de la requète
		StringBuffer queryURLBuffer = new StringBuffer();
		queryURLBuffer.append(QUERY_URL_START);
		if ( getRequest().getLang() != null ) {
			appendParameter(queryURLBuffer, LANGUAGE_PARAM_NAME, getSpecificLanguageCode(getRequest().getLang()));
		} else {
			throw new EnVOException("La langue est obligatoire pour le fichier "+getRequest().getFilename());
		}
		if ( getRequest().getSeason() != null ) {
			appendParameter(queryURLBuffer, SEASON_PARAM_NAME, getRequest().getSeason());
		}
		if ( getRequest().getEpisode() != null ) {
			appendParameter(queryURLBuffer, EPISODE_PARAM_NAME, getRequest().getEpisode());
		}
		if ( getRequest().getQuery() != null ) {
			try {
				// FIXME Podnapisi n'est pas très permissif => virer les [LOL] ou choses du genre du nom du fichier pour les films
				appendParameter(queryURLBuffer, MOVIE_PARAM_NAME, URLEncoder.encode(getRequest().getQuery(), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				throw new EnVOException("Erreur pendant l'encodage de '"+getRequest().getQuery()+"' pour l'URL", e);
			}
		} else {
			throw new EnVOException("La requète est obligatoire pour le fichier "+getRequest().getFilename());
		}
		try {
			return new URL(queryURLBuffer.toString());
		} catch (MalformedURLException e) {
			throw new EnVOException("URL générée invalide : "+queryURLBuffer.toString(), e);
		}
	}

	@Override
	public boolean hasResults(Document resultPage) throws EnVOException {
		// Comptage du nombre de résultats
        return resultPage.select(HAS_RESULT_SELECTOR).size() > 0;
	}
	
	@Override
	public List<URL> getResultsURLs(Document resultPage) throws EnVOException {
		List<URL> result = new ArrayList<URL>();
		
		// Recherche des URL pour chaque résultat de recherche
		Elements elements = resultPage.select(RESULT_SELECTOR);
		for ( Element link : elements ) {
			String url = PODNAPISI_DOMAIN+link.attr(HREF_ATTRIBUTE);
			try {
				result.add(new URL(url));
			} catch (MalformedURLException e) {
				throw new EnVOException("URL invalide : "+url);
			}
		}
		
		return result;
	}
	
	@Override
	public SubtitlesResult createResult(Document downloadPage) throws EnVOException {
		SubtitlesResult result = new SubtitlesResult();
		
		// Récupération de l'id (dernière partie de l'URL)
		String id = downloadPage.select(".information.parallel:eq(0) p:eq(0) span:eq(1)").text();
		result.setId(id);
		
		// Récupération de l'URL
		String url = downloadPage.select(".big.download.button").attr(HREF_ATTRIBUTE);
		try {
			result.setDownloadURL(new URL(PODNAPISI_DOMAIN+url));
		} catch (MalformedURLException e) {
			throw new EnVOException("URL invalide : "+PODNAPISI_DOMAIN+url, e);
		}
		
		// Récupération des fichiers correspondants
		// Pour chaque élément du div contenant les descriptions des fichiers
		Elements links = downloadPage.select(".information.parallel + fieldset:not(.information.parallel) p a");
		for ( Element link : links ) {
			
			// Pas de taille sur Podnapisi
			Long size = null;
			
			// Un seul fichier par nom de fichier et l'id est aussi le nom de fichier (pas d'info supplémentaire)
			List<String> filenames = new ArrayList<String>();
			String filename = link.text();
			filenames.add(filename);
			String fileId = filename;
			
			// Création du fichier résultat
			SubtitlesResultFile file = new SubtitlesResultFile();
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
}
