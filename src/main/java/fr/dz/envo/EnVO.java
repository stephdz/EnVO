package fr.dz.envo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import fr.dz.envo.api.AbstractSubtitlesSource;
import fr.dz.envo.api.SubtitlesRequest;
import fr.dz.envo.api.SubtitlesResult;
import fr.dz.envo.api.SubtitlesSource;
import fr.dz.envo.exception.EnVOException;
import fr.dz.envo.util.IOUtils;

public class EnVO {
	
	public static final Logger LOGGER = Logger.getLogger(EnVO.class.getPackage().getName());

	// Constantes
	public static final String VERBOSE_OPTION = "-v";
	public static final String TRANSCODE_OPTION = "-t";
	public static final String DEFAULT_TARGET_ENCODING = "WINDOWS-1252";

	/**
	 * Utilitaire de téléchargement de sous-titres depuis EnVO
	 * @param args Arguments : <options> <langue> <nom_de_fichier>
	 */
	public static void main(String[] args) {
		
		// Recherche d'une option verbose
		List<String> options = prepareOptions(args);
		if ( options.contains(VERBOSE_OPTION) ) {
			options.remove(VERBOSE_OPTION);
		} else {
			LOGGER.setLevel(Level.INFO);
		}
		
		try {
			// Mode transcodage, on change l'encoding du fichier pour la freebox
			if ( options.contains(TRANSCODE_OPTION) ) {
				options.remove(TRANSCODE_OPTION);
				
				// Nombre d'arguments incorrects
				if ( options.size() != 1 && options.size() != 2 ) {
					System.err.println("Arguments : <options> <fichier_sous_titre> [encoding]");
					return;
				}
				
				// Changement d'encoding, si nécessaire
				String filename = options.get(0);
				File file = new File(filename);
				String targetEncoding = options.size() < 2 ? DEFAULT_TARGET_ENCODING : options.get(1);
				String actualEncoding = IOUtils.detectEncoding(file);
				if ( ! StringUtils.isEmpty(actualEncoding) ) {
					if ( ! StringUtils.equals(actualEncoding, targetEncoding) ) {
						IOUtils.changeEncoding(file, actualEncoding, targetEncoding);
					} else {
						LOGGER.warn("Le fichier "+filename+" est déjà dans le bon encoding");
					}
				} else {
					throw new EnVOException("Impossible de détecter l'encoding du fichier "+filename);
				}
			}
			// Mode par défaut, récupération de sous-titres
			else {
		
				// Nombre d'arguments incorrects
				if ( options.size() != 2 ) {
					System.err.println("Arguments : <options> <langue> <nom_de_fichier>");
					return;
				}
			
				// Création de la requète
				SubtitlesRequest request = new SubtitlesRequest(options.get(0), options.get(1));
				
				// Initialisation du contexte Spring pour récupérer les downloaders
				ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
				Map<String,SubtitlesSource> sources = context.getBeansOfType(SubtitlesSource.class);
				
				// Recherche des sous titres existants
				List<SubtitlesResult> results = new ArrayList<SubtitlesResult>();
				for ( SubtitlesSource source : sources.values() ) {
					source.init(request);
					if ( source.hasSubtitles() ) {
						results.addAll(source.findSubtitles());
					}
				}
				
				// Téléchargement des meilleurs sous-titres
				AbstractSubtitlesSource.downloadBestSubtitles(request, results);
			}
		} catch (EnVOException e) {
			System.err.println(e.getMessage());
		}
	}

	/**
	 * Nettoie les options de ligne de commande et les retourne sous forme de liste pour simplifier
	 * leur traitement
	 * @param options
	 * @return
	 */
	public static List<String> prepareOptions(String[] options) {
		List<String> result = new ArrayList<String>();
		if ( options != null ) {
			for ( String option : options ) {
				option = option.trim();
				if ( ! option.isEmpty() ) {
					result.add(option);
				}
			}
		}
		return result;
	}
}
