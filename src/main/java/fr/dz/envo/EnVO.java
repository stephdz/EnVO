package fr.dz.envo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import fr.dz.envo.api.AbstractSubtitlesSource;
import fr.dz.envo.api.SubtitlesRequest;
import fr.dz.envo.api.SubtitlesResult;
import fr.dz.envo.api.SubtitlesSource;
import fr.dz.envo.exception.EnVOException;

public class EnVO {
	
	public static final Logger LOGGER = Logger.getLogger(EnVO.class.getPackage().getName());

	// Constantes
	public static final String VERBOSE_OPTION = "-v";

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
		
		// Nombre d'arguments incorrects
		if ( options.size() != 2 ) {
			System.err.println("Arguments : <options> <langue> <nom_de_fichier>");
			return;
		}
		
		try {
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
