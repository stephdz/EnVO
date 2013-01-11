package fr.dz.envo.sources;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import fr.dz.envo.api.AbstractSubtitlesSource;
import fr.dz.envo.api.SubtitlesResult;
import fr.dz.envo.api.SubtitlesResultFile;
import fr.dz.envo.exception.EnVOException;

/**
 * Recherche sur opensubtitles.org
 */
@Service("opensubtitles")
public class OpenSubtitlesDownloader extends AbstractSubtitlesSource {
	
	// Constantes pour construire l'URL de recherche
	private static final String OPEN_SUBTITLES_DOMAIN = "http://www.opensubtitles.org";
	private static final String QUERY_URL_START = OPEN_SUBTITLES_DOMAIN + "/fr/search2";
	private static final String FILENAMES_URL_PREFIX = OpenSubtitlesDownloader.OPEN_SUBTITLES_DOMAIN + "/fr/moviefilename?idsubmoviefile=";
	private static final String PARAM_NAME_VALUE_SEPARATOR = "-";
	private static final String PARAM_SEPARATOR = "/";
	private static final String LANGUAGE_PARAM_NAME = "sublanguageid";
	private static final String SEASON_PARAM_NAME = "season";
	private static final String EPISODE_PARAM_NAME = "episode";
	private static final String FORMAT_PARAM_NAME = "subformat";
	private static final String FORMAT_PARAM_VALUE = "srt";
	private static final String MOVIE_PARAM_NAME = "moviename";
	
	// Constantes de sélection CSS
	private static final String NO_RESULT_SELECTOR = ".msg.warn";
	private static final String RESULT_SELECTOR = ".change.expandable a.bnone";
	private static final String HREF_ATTRIBUTE = "href";
	private static final String DOWNLOAD_LINK_SELECTOR = "#app_link";
	
	// TODO Constantes à supprimer
	private static final String TRUSTED_IMAGE = "http://static.opensubtitles.org/gfx/icons/ranks/trusted.png";
	
	// L'URL de la requète
	private URL queryURL;
		
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
		appendParameter(queryURLBuffer, FORMAT_PARAM_NAME, FORMAT_PARAM_VALUE);
		if ( getRequest().getQuery() != null ) {
			try {
				appendParameter(queryURLBuffer, MOVIE_PARAM_NAME, URLEncoder.encode(getRequest().getQuery(), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				throw new EnVOException("Erreur pendant l'encodage de '"+getRequest().getQuery()+"' pour l'URL", e);
			}
		} else {
			throw new EnVOException("La requète est obligatoire pour le fichier "+getRequest().getFilename());
		}
		try {
			// On stocke l'URL quelque part pour pouvoir la récupérer dans le cas d'un résultat unique
			this.queryURL = new URL(queryURLBuffer.toString());
			return queryURL;
		} catch (MalformedURLException e) {
			throw new EnVOException("URL générée invalide : "+queryURLBuffer.toString(), e);
		}
	}

	@Override
	public boolean hasResults(Document resultPage) throws EnVOException {
        // Recherche du warning indiquant qu'il n'y a pas de résultat
        return resultPage.select(NO_RESULT_SELECTOR).size() == 0;
	}
	
	@Override
	public List<URL> getResultsURLs(Document resultPage) throws EnVOException {
		List<URL> result = new ArrayList<URL>();
		
		// Cas 1 : un seul résultat, on est déjà sur la bonne page
		if ( isSubtitlesPage(resultPage) ) {
			result.add(queryURL);
		}
		// Cas 2 : plusieurs résultats, on est sur la liste des fichiers de sous-titres
		else {
			// Recherche des URL pour chaque résultat de recherche
			Elements elements = resultPage.select(RESULT_SELECTOR);
			for ( Element link : elements ) {
				String url = OPEN_SUBTITLES_DOMAIN+link.attr(HREF_ATTRIBUTE);
				try {
					result.add(new URL(url));
				} catch (MalformedURLException e) {
					throw new EnVOException("URL invalide : "+url);
				}
			}
		}
		
		return result;
	}
	
	@Override
	public SubtitlesResult createResult(Document downloadPage) throws EnVOException {
		SubtitlesResult result = new SubtitlesResult();
		
		// Récupération de l'id (dernière partie de l'URL)
		String url = downloadPage.select(".msg h1 a").attr(HREF_ATTRIBUTE);
		String id = url.substring(url.lastIndexOf("/") + 1);
		result.setId(id);
		
		// Récupération de l'URL
		try {
			result.setDownloadURL(new URL(OPEN_SUBTITLES_DOMAIN+url));
		} catch (MalformedURLException e) {
			throw new EnVOException("URL invalide : "+OPEN_SUBTITLES_DOMAIN+url, e);
		}
		
		// Récupération des fichiers correspondants
		// Pour chaque élément du div contenant les descriptions des fichiers
		Elements links = downloadPage.select("#sub_subtitle_preview + div img:not([style])[alt] ~ a[title*=Taille]");
		for ( Element link : links ) {
			
			// Taille
			Long size = Long.valueOf(link.text());
			
			// Id
			Element expandLink = link.nextElementSibling();
			String onclick = expandLink.attr("onclick");
			Pattern datePatt = Pattern.compile(".*ToggleMovieFileName\\('(\\d+)'\\);.*");
			Matcher m = datePatt.matcher(onclick);
			String fileId = null;
			if ( m.matches() ) {
				fileId = m.group(1);
			}
			
			// Noms des fichiers à partir du div les listant
			List<String> filenames = new ArrayList<String>();
			try {
				Elements files = getJsoupDocument(new URL(FILENAMES_URL_PREFIX+fileId)).select("a");
				for ( Element file : files ) {
					filenames.add(file.text());
				}
			} catch ( Exception e ) {
				throw new EnVOException("Impossible de récupérer les noms de fichiers depuis : "+FILENAMES_URL_PREFIX+fileId, e);
			}
			
			// Création du fichier résultat
			SubtitlesResultFile file = new SubtitlesResultFile();
			file.setId(fileId);
			file.setSize(size);
			file.setFileNames(filenames);
			result.addFile(getRequest(), file);
		}
		
		// Récupération de l'info "Posteur de confiance"
		// FIXME Gérer les différents statuts d'OpenSubtitles
		result.setTrusted(false);
		
		// Scoring
		result.doScoring(getRequest());
		
		return result;
	}

	/**
	 * Retourne true si le contenu de la page passé en paramètres correspond à une page de sous-titres
	 * @param pageContent
	 * @return
	 */
	protected boolean isSubtitlesPage(Document page) {
		return page.select(DOWNLOAD_LINK_SELECTOR).size() > 0;
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
