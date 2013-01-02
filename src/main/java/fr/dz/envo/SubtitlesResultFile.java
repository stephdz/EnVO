package fr.dz.envo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import fr.dz.envo.util.Levenshtein;

public class SubtitlesResultFile implements Serializable {

	private static final long serialVersionUID = 6726463780614990623L;
	
	// Constantes
	private static final Integer DEFAULT_FILENAME_SCORING = 100;
	private static final Integer DEFAULT_FILESIZE_SCORING = 50;
	private static final Integer FILENAME_COEFFICIENT = 5;
	private static final Integer FILESIZE_COEFFICIENT = 2;
	
	// Champs
	private String id;
	private Long size;
	private List<String> fileNames = new ArrayList<String>();
	private Integer scoring;
	
	/**
	 * Constructeur par défaut
	 */
	public SubtitlesResultFile() {
		
	}
	
	/**
	 * Effectue le scoring
	 * @param request
	 */
	public void doScoring(SubtitlesRequest request) {
		
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
	 * Pour debug
	 */
	public void debug() {
		EnVO.LOGGER.debug("   * Fichier concerné :");
		EnVO.LOGGER.debug("     o id : "+id);
		EnVO.LOGGER.debug("     o size : "+size);
		EnVO.LOGGER.debug("     o scoring : "+scoring);
		EnVO.LOGGER.debug("     o fileNames : ");
		for ( String filename : getFileNames() ) {
			EnVO.LOGGER.debug("         "+filename);
		}
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
		SubtitlesResultFile other = (SubtitlesResultFile) obj;
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

	/*
	 * GETTERS & SETTERS
	 */
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Long getSize() {
		return size;
	}

	public void setSize(Long size) {
		this.size = size;
	}

	public List<String> getFileNames() {
		return fileNames;
	}

	public void setFileNames(List<String> fileNames) {
		this.fileNames = fileNames;
	}

	public Integer getScoring() {
		return scoring;
	}

	public void setScoring(Integer scoring) {
		this.scoring = scoring;
	}
}