package fr.dz.envo;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import fr.dz.envo.exception.EnVOException;
import fr.dz.envo.util.IOUtils;


public abstract class AbstractSubtitlesSource implements SubtitlesSource {
	
	// Constantes
	protected static final String TARGET_ENCODING = "WINDOWS-1252";
	protected static final String SRT_EXTENSION = "srt";
	protected static final String NFO_EXTENSION = "nfo";

	// La requète
	private SubtitlesRequest request;
	
	// Les résultats
	private List<SubtitlesResult> subtitlesResults;
	
	/**
	 * Constructeur par défaut
	 */
	public AbstractSubtitlesSource() {
		super();
	}
	
	/**
	 * Initialisation à partir d'une recherche
	 * @param request
	 * @throws EnVOException
	 */
	@Override
	public void init(SubtitlesRequest request) throws EnVOException {
		setRequest(request);
	}
	
	/*
	 * METHODES STATIQUES
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
				IOUtils.encodeInputStream(zip.getInputStream(toExtract), new File(destinationFile), TARGET_ENCODING);
				
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
	
	/*
	 * METHODES UTILITAIRES
	 */

	/**
	 * Retourne le contenu correspondant à une URL
	 * @param url
	 * @return
	 * @throws EnVOException 
	 */
	public static String getURLContent(URL url) throws EnVOException {
		return IOUtils.getURLContent(url, null);
	}
	
	/**
	 * Retourne un document Jsoup à partir de HTML
	 * @param content
	 * @return
	 */
	public static Document getJsoupDocument(String content) {
		return Jsoup.parse(content);
	}
	
	/*
	 * GETTERS & SETTERS
	 */
	
	protected SubtitlesRequest getRequest() {
		return request;
	}
	
	protected void setRequest(SubtitlesRequest request) {
		this.request = request;
	}

	public List<SubtitlesResult> getSubtitlesResults() {
		return subtitlesResults;
	}

	public void setSubtitlesResults(List<SubtitlesResult> subtitlesResults) {
		this.subtitlesResults = subtitlesResults;
	}
}
