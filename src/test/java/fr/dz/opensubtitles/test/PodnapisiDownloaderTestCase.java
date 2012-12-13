package fr.dz.opensubtitles.test;

import java.io.File;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import fr.dz.opensubtitles.OpenSubtitlesRequest;
import fr.dz.opensubtitles.sources.PodnapisiDownloader;


public class PodnapisiDownloaderTestCase extends AbstractOpenSubtitlesTest {

	// Constantes
	private static final String LANG = "fre";
	private static final String FILE_PREFIX = "target/test-files/";
	private static final String SERIE_RESULT_FILENAME = FILE_PREFIX + "The.Big.Bang.Theory.S05E23.[LOL].avi";
	//private static final String FILM_FILENAME = FILE_PREFIX + "Wild.Wild.West.[LOL].avi"; TODO A faire marcher
	private static final String FILM_FILENAME = FILE_PREFIX + "Wild.Wild.West.avi";
	private static final String SERIE_URL = "http://www.podnapisi.net/fr/ppodnapisi/search?sT=1&sJ=8&sTS=5&sTE=23&sK=the+big+bang+theory";
	private static final String FILM_URL = "http://www.podnapisi.net/fr/ppodnapisi/search?sT=1&sJ=8&sK=wild+wild+west";
	
	@BeforeClass
	public static void init() {
		File dir = new File(FILE_PREFIX);
		dir.mkdirs();
	}
	
	@Test
	public void testHasSubtitlesSerieFile() throws Exception {
		debug();
		
		// Test de l'URL
		OpenSubtitlesRequest request = new OpenSubtitlesRequest(LANG, SERIE_RESULT_FILENAME);
		PodnapisiDownloader downloader = new PodnapisiDownloader(request);
		Assert.assertEquals("URL", SERIE_URL, downloader.getQueryURL().toString());
		
		// Test de la présence de sous-titres
		Assert.assertTrue("Sous titres attendus pour "+SERIE_URL, downloader.hasSubtitles());
	}
	
	@Test
	public void testHasSubtitlesNonSerieFile() throws Exception {
		debug();
		
		// Test de l'URL
		OpenSubtitlesRequest request = new OpenSubtitlesRequest(LANG, FILM_FILENAME);
		PodnapisiDownloader downloader = new PodnapisiDownloader(request);
		Assert.assertEquals("URL", FILM_URL, downloader.getQueryURL().toString());
		
		// Test de la présence de sous-titres
		Assert.assertTrue("Sous titres attendus pour "+FILM_URL, downloader.hasSubtitles());
	}
	
	@Test
	public void testDownloadSerieFileResult() throws Exception {
		debug();
		
		// Test depuis un fichier HTML résultat de hasSubtitles
		OpenSubtitlesRequest request = new OpenSubtitlesRequest(LANG, SERIE_RESULT_FILENAME);
		PodnapisiDownloader downloader = new PodnapisiDownloader(request);
		if ( downloader.hasSubtitles() ) {
			downloader.downloadFirstSubtitles();
		}
		Assert.assertEquals("Nombre de sous-titres invalides pour "+SERIE_RESULT_FILENAME, 5, downloader.getSubtitlesResults().size());
	}
}
