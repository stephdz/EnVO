package fr.dz.envo.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import fr.dz.envo.EnVO;
import fr.dz.envo.exception.EnVOException;
import fr.dz.envo.util.IOUtils;


public abstract class AbstractSubtitlesSource implements SubtitlesSource {
	
	// Constantes
	protected static final String SRT_EXTENSION = "srt";
	protected static final String NFO_EXTENSION = "nfo";

	// La requète
	private SubtitlesRequest request;
	
	// Les résultats
	private List<SubtitlesResult> subtitlesResults;
	
	// La table de correspondance pour les codes langue
	private Properties languages;
	
	// L'URL de la requète
	private URL queryURL;
	
	// Le document contenu de la page résultat de recherche
	private Document queryResultDocument;
	
	
	/**
	 * Constructeur par défaut
	 */
	public AbstractSubtitlesSource() {
		super();
	}
	
	/*
	 * METHODES ABSTRAITES A REDEFINIR
	 */
	
	/**
	 * Construit l'URL de la requête de recherche de sous-titres
	 * @return
	 * @throws EnVOException
	 */
	public abstract URL buildQueryURL() throws EnVOException;
	
	/**
	 * Retourne true s'il y a un résultat de recherche, false sinon
	 * @param resultPage
	 * @return
	 * @throws EnVOException
	 */
	public abstract boolean hasResults(Document resultPage) throws EnVOException;
	
	/**
	 * Retourne la liste des URLs correspondantes aux pages de résultat
	 * @param resultPage
	 * @return
	 * @throws EnVOException
	 */
	public abstract List<URL> getResultsURLs(Document resultPage) throws EnVOException;
	
	/**
	 * Crée un objet résultat à partir d'une page de téléchargement
	 * @param downloadPage
	 * @return
	 * @throws EnVOException
	 */
	public abstract SubtitlesResult createResult(Document downloadPage) throws EnVOException;
	
	/*
	 * METHODES DE L'INTERFACE
	 */
	
	/**
	 * Initialisation à partir d'une recherche
	 * @param request
	 * @throws EnVOException
	 */
	@Override
	public void init(SubtitlesRequest request) throws EnVOException {
		
		// Copie de la request
		this.request = request;
		
		// Chargement du fichier properties pour les codes langue
		InputStream in = null;
		try {
			
			in = getClass().getResourceAsStream("/lang/"+getSourceId()+".properties");
			if ( in != null ) {
				languages = new Properties();
				languages.load(in);
			}
		} catch (IOException e) {
			throw new EnVOException("Erreur pendant le chargement du fichier de langue", e);
		} finally {
			if ( in != null ) {
				try {
					in.close();
				} catch (IOException e) {
					throw new EnVOException("Erreur pendant le chargement du fichier de langue", e);
				}
			}
		}
		
		// Construction de l'URL de recherche
		this.queryURL = buildQueryURL();
	}
	
	@Override
	public boolean hasSubtitles() throws EnVOException {
		
		EnVO.LOGGER.debug("#####################################################################");
		EnVO.LOGGER.debug("# Exécution de la requète : "+queryURL);
		EnVO.LOGGER.debug("#####################################################################");
		
        // Récupération de la page de résultat de la requète
        this.queryResultDocument = getJsoupDocument(queryURL);
        
        return hasResults(queryResultDocument);
	}
	
	@Override
	public List<SubtitlesResult> findSubtitles() throws EnVOException {
		this.subtitlesResults = new ArrayList<SubtitlesResult>();
		
		// Récupération des URLs de résultat
		List<URL> results = getResultsURLs(queryResultDocument);
		
		// Pour chaque page résultat, création d'un objet résultat
		// TODO Rendre cette action multithread
		for ( URL resultURL : results ) {
			subtitlesResults.add(createResult(getJsoupDocument(resultURL)));
		}
		
		return subtitlesResults;
	}

	/*
	 * METHODES UTILITAIRES
	 */

	/**
	 * Récupère le code langage spécifique à la source dans le fichier de properties lang/<source>.properties 
	 * Si pas de fichier properties, renvoie le code iso
	 * @param isoCode
	 * @return
	 */
	public String getSpecificLanguageCode(String isoCode) {
		if ( languages != null ) {
			return languages.getProperty(isoCode);
		} else {
			return isoCode;
		}
	}
	
	/**
	 * Retourne l'id Spring de la source à partir de l'annotation @Service
	 * @return
	 */
	protected String getSourceId() {
		Service service = getClass().getAnnotation(Service.class);
		if ( service != null ) {
			return service.value();
		} else {
			return null;
		}
	}
	
	/*
	 * METHODES STATIQUES
	 * FIXME Il doit être possible de les virer ou en déplacer dans IOUtils
	 */
	
	/**
	 * Télécharge les sous-titres correspondants le mieux
	 * @return
	 * @throws EnVOException
	 */
	public static boolean downloadBestSubtitles(SubtitlesRequest request, List<SubtitlesResult> results) throws EnVOException {
		
		// Choix du meilleur scoring (le plus petit)
		SubtitlesResult bestResult = null;
		for ( SubtitlesResult result : results ) {
			if ( bestResult == null || result.getScoring() < bestResult.getScoring() ) {
				bestResult = result;
			}
		}
		
		// Téléchargement du meilleur résultat
		if ( bestResult != null ) {
			download(request, bestResult);
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Télécharge le résultat passé en paramètre
	 * FIXME Plante avec "blood diamond.avi"
	 * @param request
	 * @param bestResult
	 * @throws EnVOException 
	 */
	public static void download(SubtitlesRequest request, SubtitlesResult bestResult) throws EnVOException {
		String fileName = request.getFolder() + File.separator + request.getFilename().substring(
				request.getFilename().lastIndexOf(File.separator) + 1,
				request.getFilename().lastIndexOf(".")) + "." + SRT_EXTENSION;
		download(bestResult.getDownloadURL(), fileName);
	}
	
	/**
	 * Télécharge le SRT présent dans un ZIP passé en paramètre dans un fichier précis
	 * @param url
	 * @param destinationFile
	 * @throws EnVOException 
	 */
	public static void download(URL url, String destinationFile) throws EnVOException {
		try {
			// Enregistrement du zip dans un fichier temporaire
			File file = File.createTempFile("opensubtitle", ".zip");
			IOUtils.saveURL(url, file);
			
			// Extraction du fichier SRT
			ZipFile zip = new ZipFile(file);
			ZipEntry toExtract = null;
			ZipEntry noNFO = null;
			Enumeration<? extends ZipEntry> entries = zip.entries();
			while ( entries.hasMoreElements() ) {
				ZipEntry entry = entries.nextElement();
				if ( entry.getName().endsWith(SRT_EXTENSION) ) {
					toExtract = entry;
					break;
				}
				if ( noNFO == null && ! entry.getName().endsWith(NFO_EXTENSION) ) {
					noNFO = entry;
				}
			}

			// Si on n'a pas trouvé de fichier avec la bonne extension
			if ( toExtract == null ) {
				// On prend le premier qui ne se termine pas en NFO
				toExtract = noNFO;
			}

			// Extraction du fichier et conversion dans un encoding que la Freebox accepte
			if ( toExtract != null ) {
				IOUtils.encodeInputStream(zip.getInputStream(toExtract), new File(destinationFile), EnVO.DEFAULT_TARGET_ENCODING);
				
				EnVO.LOGGER.info("#####################################################################");
				EnVO.LOGGER.info("# Sous-titre sauvegardé : "+url);
				EnVO.LOGGER.info("# Fichier : "+destinationFile);
				EnVO.LOGGER.info("#####################################################################");
			} else {
				EnVO.LOGGER.info("#####################################################################");
				EnVO.LOGGER.info("# Aucun fichier de sous-titre trouvé.");
				EnVO.LOGGER.info("#####################################################################");
			}
		} catch (IOException e) {
			throw new EnVOException("Erreur pendant la sauvegarde de l'URL '"+url+"'", e);
		}
	}
	
	/**
	 * Retourne un document Jsoup à partir de HTML
	 * @param content
	 * @return
	 * @throws EnVOException 
	 */
	public static Document getJsoupDocument(URL url) throws EnVOException {
		return Jsoup.parse(IOUtils.getURLContent(url));
	}
	
	/*
	 * GETTERS & SETTERS
	 */
	
	public SubtitlesRequest getRequest() {
		return request;
	}

	public URL getQueryURL() {
		return queryURL;
	}

	public List<SubtitlesResult> getSubtitlesResults() {
		return subtitlesResults;
	}
}
