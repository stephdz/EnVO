package fr.dz.opensubtitles.sources;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
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


public abstract class AbstractOpenSubtitlesSource implements OpenSubtitlesSource {
	
	// Constantes
	protected static final String USER_AGENT = "Mozilla/5.0 (X11; Ubuntu; Linux i686; rv:12.0) Gecko/20100101 Firefox/12.0";
	protected static final String ENCODING = "UTF-8";
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
	private void download(OpenSubtitlesResult bestResult) throws OpenSubtitlesException {
		try {
			// Enregistrement du zip dans un fichier temporaire
			File file = File.createTempFile("opensubtitle", ".zip");
			saveURL(bestResult.getDownloadURL(), file);
			
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

			// Extraction du fichier
			if ( toExtract != null ) {
				String fileName = getRequest().getFolder() + File.separator + getRequest().getFilename().substring(
						getRequest().getFilename().lastIndexOf(File.separator) + 1,
						getRequest().getFilename().lastIndexOf(".")) + "." + SRT_EXTENSION;
				saveUTF8Input(zip.getInputStream(toExtract), new File(fileName));
				
				OpenSubtitles.LOGGER.info("#####################################################################");
				OpenSubtitles.LOGGER.info("# Sous-titre sauvegardé : "+fileName);
				OpenSubtitles.LOGGER.info("#####################################################################");
			} else {
				OpenSubtitles.LOGGER.info("#####################################################################");
				OpenSubtitles.LOGGER.info("# Aucun fichier de sous-titre trouvé.");
				OpenSubtitles.LOGGER.info("#####################################################################");
			}
		} catch (IOException e) {
			throw new OpenSubtitlesException("Erreur pendant la sauvegarde de l'URL '"+bestResult.getDownloadURL()+"'", e);
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
	        reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), ENCODING));
	        if ( resultFile != null ) {
	        	writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(resultFile), ENCODING));
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
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileName)), ENCODING));
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
	 * Sauvegarde le contenu d'une URL dans un fichier
	 * @param url
	 * @param file
	 * @return
	 * @throws OpenSubtitlesException 
	 */
	public static void saveURL(URL url, File file) throws OpenSubtitlesException {
		BufferedInputStream in = null;
		BufferedOutputStream out = null;
        try {
        	// Création de la connexion
			URLConnection connection = url.openConnection();
	        connection.setRequestProperty("User-Agent", USER_AGENT);
	        connection.connect();
        	
	        // Enregistrement du fichier
			saveBinaryInput(connection.getInputStream(), file);
		} catch (Exception e) {
			throw new OpenSubtitlesException("Erreur pendant la sauvegarde de l'URL '"+url+"' dans le fichier : "+file, e);
		} finally {
			if ( in != null ) {
				try {
					in.close();
				} catch (IOException e) {
					throw new OpenSubtitlesException("Erreur pendant la sauvegarde de l'URL '"+url+"' dans le fichier : "+file, e);
				}
			}
			if ( out != null ) {
				try {
					out.close();
				} catch (IOException e) {
					throw new OpenSubtitlesException("Erreur pendant la sauvegarde de l'URL '"+url+"' dans le fichier : "+file, e);
				}
			}
		}
	}
	
	/**
	 * Sauvegarde le contenu d'un input stream dans un fichier
	 * @param in
	 * @param file
	 * @return
	 * @throws OpenSubtitlesException 
	 */
	public static void saveBinaryInput(InputStream in, File file) throws OpenSubtitlesException {
		BufferedOutputStream out = null;
        try {
        	// Enregistrement du fichier
	        in = new BufferedInputStream(in);
			out = new BufferedOutputStream(new FileOutputStream(file));
			
			// Enregistrement
			byte[] buffer = new byte[1024];
			int nbRead = 0;
			while ( (nbRead = in.read(buffer)) != -1 ) {
				out.write(buffer, 0, nbRead);
			}
		} catch (IOException e) {
			throw new OpenSubtitlesException("Erreur pendant la sauvegarde dans le fichier : "+file, e);
		} finally {
			if ( in != null ) {
				try {
					in.close();
				} catch (IOException e) {
					throw new OpenSubtitlesException("Erreur pendant la sauvegarde dans le fichier : "+file, e);
				}
			}
			if ( out != null ) {
				try {
					out.close();
				} catch (IOException e) {
					throw new OpenSubtitlesException("Erreur pendant la sauvegarde dans le fichier : "+file, e);
				}
			}
		}
	}
	
	/**
	 * Retourne un document Jsoup à partir de HTML
	 * @param content
	 * @return
	 */
	public static Document getJsoupDocument(String content) {
		return Jsoup.parse(content);
	}
	
	/**
	 * Sauvegarde le contenu d'un input stream dans un fichier
	 * FIXME Problème d'encoding dans certains cas (OpenSubtitles:"?" présent en début de fichier ; Podnapisi:accents qui ne passent pas)
	 * @param in
	 * @param file
	 * @return
	 * @throws OpenSubtitlesException 
	 */
	public static void saveUTF8Input(InputStream in, File file) throws OpenSubtitlesException {
		BufferedWriter writer = null;
		BufferedReader reader = null;
        try {
        	// Enregistrement du fichier
	        reader = new BufferedReader(new InputStreamReader(in, ENCODING));
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "Cp1252"));
			
			// Enregistrement
			String line;
			while ( (line = reader.readLine()) != null ) {
				writer.write(line);
				writer.newLine();
				writer.flush();
			}
		} catch (IOException e) {
			throw new OpenSubtitlesException("Erreur pendant la sauvegarde dans le fichier : "+file, e);
		} finally {
			if ( reader != null ) {
				try {
					reader.close();
				} catch (IOException e) {
					throw new OpenSubtitlesException("Erreur pendant la sauvegarde dans le fichier : "+file, e);
				}
			}
			if ( writer != null ) {
				try {
					writer.close();
				} catch (IOException e) {
					throw new OpenSubtitlesException("Erreur pendant la sauvegarde dans le fichier : "+file, e);
				}
			}
		}
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
