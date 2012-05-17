package fr.dz.opensubtitles;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import fr.dz.opensubtitles.exception.OpenSubtitlesException;
import fr.dz.opensubtitles.util.Levenshtein;

public class OpenSubtitlesResult implements Serializable {

	private static final long serialVersionUID = 5630975519309879850L;
	
	// Constantes
	private static final String TRUSTED_IMAGE = "http://static.opensubtitles.org/gfx/icons/ranks/trusted.png";
	private static final String SUBTITLE_URL_PREFIX = "/fr/subtitles/";
	private static final String SUBTITLE_URL_START = "<link rel=\"bookmark\" href=\"" + SUBTITLE_URL_PREFIX;
	private static final String SUBTITLE_URL_END = "/";
	private static final String DOWNLOAD_SUBTITLE_URL_PREFIX = OpenSubtitlesDownloader.OPEN_SUBTITLES_DOMAIN + "/fr/subtitleserve/sub/";
	private static final String SIZE_BEFORE_STRING = "<a class=\"none\" title=\"Taille";
	private static final String SIZE_FILENAME_START = ">";
	private static final String SIZE_FILENAME_END = "</a>";
	private static final String ID_START = "<a class=\"none\" href=\"javascript:void(0)\" onclick=\"ToggleMovieFileName('";
	private static final String ID_END = "'";
	private static final String FILENAMES_URL_PREFIX = OpenSubtitlesDownloader.OPEN_SUBTITLES_DOMAIN + "/fr/moviefilename?idsubmoviefile=";
	private static final String FILENAMES_BEFORE_STRING = "<a href='http://www.opensubtitles.org/addons/servead.php?weblang=fr&file=";
	private static final Integer DEFAULT_FILENAME_SCORING = 100;
	private static final Integer DEFAULT_FILESIZE_SCORING = 50;
	private static final Integer FILENAME_COEFFICIENT = 5;
	private static final Integer FILESIZE_COEFFICIENT = 2;
	private static final Integer TRUSTED_COEFFICIENT = 95;
	
	// Champs
	private String id;
	private URL downloadURL;
	private List<OpenSubtitleResultFile> files;
	private Boolean trusted;
	private Integer scoring;

	/**
	 * Constructeur à partir d'une page de sous-titre
	 * @param pageContent le contenu de la page
	 * @throws OpenSubtitlesException 
	 */
	public OpenSubtitlesResult(OpenSubtitlesRequest request, String pageContent) throws OpenSubtitlesException {
		setId(pageContent);
		setDownloadURL(pageContent);
		setFiles(pageContent, request);
		setTrusted(pageContent);
		setScoring(request);
		debug();
	}

	/**
	 * Constructeur à partir d'une URL de page de sous-titre
	 * @param url l'URL de la page de sous-titre
	 * @throws OpenSubtitlesException 
	 */
	public OpenSubtitlesResult(OpenSubtitlesRequest request, URL url) throws OpenSubtitlesException {
		this(request, OpenSubtitlesDownloader.getURLContent(url));
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
	private void setFiles(String pageContent, OpenSubtitlesRequest request) {
		this.files = new ArrayList<OpenSubtitleResultFile>();
		int globalStart = 0;
		while ( (globalStart = pageContent.indexOf(SIZE_BEFORE_STRING, globalStart)) != -1 ) {
			
			// Recherche de la taille du fichier
			int sizeEnd = pageContent.indexOf(SIZE_FILENAME_END, globalStart);
			String subString = pageContent.substring(globalStart, sizeEnd);
			int sizeStart = subString.lastIndexOf(SIZE_FILENAME_START) + SIZE_FILENAME_START.length();
			Long size = Long.valueOf(subString.substring(sizeStart));
			
			// Recherche de l'id du fichier
			int idStart = pageContent.indexOf(ID_START, globalStart) + ID_START.length();
			int idEnd = pageContent.indexOf(ID_END, idStart);
			String id = pageContent.substring(idStart, idEnd);
			
			// Création du fichier résultat
			OpenSubtitleResultFile file = new OpenSubtitleResultFile(id, size);
			files.add(file);
			
			// Récupération des noms de fichier
			try {
				String fileNamesContent = OpenSubtitlesDownloader.getURLContent(new URL(FILENAMES_URL_PREFIX+id));
				int filenameStart = 0;
				while ( (filenameStart = fileNamesContent.indexOf(FILENAMES_BEFORE_STRING, filenameStart)) != -1 ) {
					int filenameEnd = fileNamesContent.indexOf(SIZE_FILENAME_END, filenameStart);
					subString = fileNamesContent.substring(filenameStart, filenameEnd);
					int start = subString.lastIndexOf(SIZE_FILENAME_START) + SIZE_FILENAME_START.length();
					String filename = subString.substring(start);
					file.getFileNames().add(filename);
					filenameStart += start;
				}
			} catch (Exception e) {
				e.printStackTrace(); // Ne devrait pas arriver
			}
			
			// Scoring
			file.doScoring(request);
			
			// Fichier suivant
			globalStart = idStart;
		}
	}
	
	/**
	 * Methode qui note le résultat en fonction de la requète initiale
	 * Plus la valeur est faible, mieux c'est
	 * @param request
	 */
	private void setScoring(OpenSubtitlesRequest request) {
		
		// On prend le score minimum des fichiers
		List<Integer> scorings = new ArrayList<Integer>();
		for ( OpenSubtitleResultFile file : getFiles() ) {
			scorings.add(file.getScoring());
		}
		this.scoring = Levenshtein.min(scorings.toArray(new Integer[0]));
		
		// Si le fichier est trusted, on accorde 5% de bonus
		if ( trusted ) {
			this.scoring = this.scoring * TRUSTED_COEFFICIENT / 100;
		}
	}
	
	
	public static class OpenSubtitleResultFile implements Serializable {

		private static final long serialVersionUID = 6726463780614990623L;
		
		private String id;
		private Long size;
		private List<String> fileNames;
		private Integer scoring;
		
		public OpenSubtitleResultFile(String id, Long size) {
			this.id = id;
			this.size = size;
			this.fileNames = new ArrayList<String>();
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result	+ ((fileNames == null) ? 0 : fileNames.hashCode());
			result = prime * result + ((size == null) ? 0 : size.hashCode());
			result = prime * result + ((id == null) ? 0 : id.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			OpenSubtitleResultFile other = (OpenSubtitleResultFile) obj;
			if (fileNames == null) {
				if (other.fileNames != null)
					return false;
			} else if (!fileNames.equals(other.fileNames))
				return false;
			if (size == null) {
				if (other.size != null)
					return false;
			} else if (!size.equals(other.size))
				return false;
			if (id == null) {
				if (other.id != null)
					return false;
			} else if (!id.equals(other.id))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "OpenSubtitleResultFile [id=" + id + ", size=" + size + ", fileNames=" + fileNames + "]";
		}
		
		public void doScoring(OpenSubtitlesRequest request) {
			
			// Nom de fichier : on prend le minimum de la distance entre le fichier cherché et ceux trouvés
			// Et on essaye d'obtenir un pourcentage par rapport au nombre de caractères de la chaîne de base
			List<Integer> distances = new ArrayList<Integer>();
			Integer fileNameScoring = DEFAULT_FILENAME_SCORING;
			if ( getFileNames() != null ) {
				for ( String filename : getFileNames() ) {
					distances.add(Levenshtein.distance(request.getFilename(), filename));
				}
			}
			if ( ! distances.isEmpty() ) {
				Integer minDistance = Levenshtein.min(distances.toArray(new Integer[0]));
				fileNameScoring = minDistance * 100 / request.getFilename().length();
			}
			
			// Taille de fichier : on fait la différence en valeur absolue
			Integer fileSizeScoring = DEFAULT_FILESIZE_SCORING;
			if ( request.getFilesize() != null ) {
				Long minDifference = Math.abs(request.getFilesize() - getSize());
				fileSizeScoring = minDifference.intValue() * 100 / request.getFilesize().intValue();
			}
			
			// Application des coefficients pour avoir le score final
			this.scoring = (fileNameScoring * FILENAME_COEFFICIENT + fileSizeScoring * FILESIZE_COEFFICIENT)
					/ (FILENAME_COEFFICIENT + FILESIZE_COEFFICIENT);
		}

		/**
		 * @return the id
		 */
		public String getId() {
			return id;
		}
		
		/**
		 * @return the size
		 */
		public Long getSize() {
			return size;
		}

		/**
		 * @return the fileNames
		 */
		public List<String> getFileNames() {
			return fileNames;
		}
		
		/**
		 * @return the scoring
		 */
		public Integer getScoring() {
			return scoring;
		}

		public void debug() {
			OpenSubtitles.LOGGER.debug("   * Fichier concerné :");
			OpenSubtitles.LOGGER.debug("     o id : "+id);
			OpenSubtitles.LOGGER.debug("     o size : "+size);
			OpenSubtitles.LOGGER.debug("     o scoring : "+scoring);
			OpenSubtitles.LOGGER.debug("     o fileNames : ");
			for ( String filename : getFileNames() ) {
				OpenSubtitles.LOGGER.debug("         "+filename);
			}
		}
	}
	
	/**
	 * Récupère l'indicateur "source fiable" dans le contenu de la page
	 * @param pageContent
	 */
	private void setTrusted(String pageContent) {
		this.trusted = pageContent.indexOf(TRUSTED_IMAGE) != -1;
	}
	
	/**
	 * Affichage des infos de debug
	 */
	private void debug() {
		OpenSubtitles.LOGGER.debug("#####################################################################");
		OpenSubtitles.LOGGER.debug("# Résultat trouvé : ");
		OpenSubtitles.LOGGER.debug("#####################################################################");
		OpenSubtitles.LOGGER.debug(" - id : " + id);
		OpenSubtitles.LOGGER.debug(" - downloadURL : " + downloadURL);
		OpenSubtitles.LOGGER.debug(" - files : ");
		for ( OpenSubtitleResultFile file : files ) {
			file.debug();
		}
		OpenSubtitles.LOGGER.debug(" - trusted : " + trusted);
		OpenSubtitles.LOGGER.debug(" - scoring : " + scoring);
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
	 * @return the files
	 */
	public List<OpenSubtitleResultFile> getFiles() {
		return files;
	}

	/**
	 * @return the trusted
	 */
	public Boolean getTrusted() {
		return trusted;
	}
	
	/**
	 * @return the scoring
	 */
	public Integer getScoring() {
		return scoring;
	}
}
