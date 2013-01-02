package fr.dz.opensubtitles.sources;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import fr.dz.opensubtitles.OpenSubtitles;
import fr.dz.opensubtitles.OpenSubtitlesRequest;
import fr.dz.opensubtitles.OpenSubtitlesResult;
import fr.dz.opensubtitles.exception.OpenSubtitlesException;
import fr.dz.opensubtitles.util.IOUtils;


public abstract class AbstractOpenSubtitlesSource implements OpenSubtitlesSource {
	
	// Constantes
	protected static final String TARGET_ENCODING = "WINDOWS-1252";
	protected static final String SRT_EXTENSION = "srt";
	protected static final String NFO_EXTENSION = "nfo";

	// La requète
	private OpenSubtitlesRequest request;
	
	// Les résultats
	private List<OpenSubtitlesResult> subtitlesResults;
	
	/**
	 * Constructeur à partir d'une recherche
	 * @param request
	 * @throws OpenSubtitlesException
	 */
	public AbstractOpenSubtitlesSource(OpenSubtitlesRequest request) throws OpenSubtitlesException {
		setRequest(request);
	}
	
	/*
	 * METHODES ABSTRAITES
	 */
	
	/**
	 * Récupère la liste des sous titres depuis la page de résultat de requète
	 * @return
	 * @throws OpenSubtitlesException 
	 */
	protected abstract List<OpenSubtitlesResult> getSubtitlesURLs() throws OpenSubtitlesException;
	
	/*
	 * METHODES PUBLIQUES
	 */
	
	@Override
	public boolean downloadFirstSubtitles() throws OpenSubtitlesException {
		
		// Récupération de la liste des URLs de sous-titres 
		subtitlesResults = getSubtitlesURLs();
		
		// Choix du meilleur scoring (le plus petit)
		OpenSubtitlesResult bestResult = null;
		for ( OpenSubtitlesResult result : subtitlesResults ) {
			if ( bestResult == null || result.getScoring() < bestResult.getScoring() ) {
				bestResult = result;
			}
		}
		
		// Téléchargement du meilleur résultat
		if ( bestResult != null ) {
			download(bestResult);
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Télécharge le résultat passé en paramètre
	 * @param bestResult
	 * @throws OpenSubtitlesException 
	 */
	protected void download(OpenSubtitlesResult bestResult) throws OpenSubtitlesException {
		String fileName = getRequest().getFolder() + File.separator + getRequest().getFilename().substring(
				getRequest().getFilename().lastIndexOf(File.separator) + 1,
				getRequest().getFilename().lastIndexOf(".")) + "." + SRT_EXTENSION;
		download(bestResult.getDownloadURL(), fileName);
	}
	
	/**
	 * Télécharge le SRT présent dans un ZIP passé en paramètre dans un fichier précis
	 * @param url
	 * @param destinationFile
	 * @throws OpenSubtitlesException 
	 */
	public static void download(URL url, String destinationFile) throws OpenSubtitlesException {
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
				
				OpenSubtitles.LOGGER.info("#####################################################################");
				OpenSubtitles.LOGGER.info("# Sous-titre sauvegardé : "+destinationFile);
				OpenSubtitles.LOGGER.info("#####################################################################");
			} else {
				OpenSubtitles.LOGGER.info("#####################################################################");
				OpenSubtitles.LOGGER.info("# Aucun fichier de sous-titre trouvé.");
				OpenSubtitles.LOGGER.info("#####################################################################");
			}
		} catch (IOException e) {
			throw new OpenSubtitlesException("Erreur pendant la sauvegarde de l'URL '"+url+"'", e);
		}
	}
	
	/*
	 * METHODES UTILITAIRES
	 */

	/**
	 * Retourne le contenu correspondant à une URL
	 * @param url
	 * @return
	 * @throws OpenSubtitlesException 
	 */
	public static String getURLContent(URL url) throws OpenSubtitlesException {
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
	
	protected OpenSubtitlesRequest getRequest() {
		return request;
	}
	
	protected void setRequest(OpenSubtitlesRequest request) {
		this.request = request;
	}

	public List<OpenSubtitlesResult> getSubtitlesResults() {
		return subtitlesResults;
	}

	public void setSubtitlesResults(List<OpenSubtitlesResult> subtitlesResults) {
		this.subtitlesResults = subtitlesResults;
	}
}
