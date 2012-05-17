package fr.dz.opensubtitles.test;

import junit.framework.Assert;

import org.junit.Test;

import fr.dz.opensubtitles.OpenSubtitlesDownloader;
import fr.dz.opensubtitles.OpenSubtitlesRequest;


public class OpenSubtitlesDownloaderTestCase extends AbstractOpenSubtitlesTest {

	// Constantes
	private static final String LANG = "fre";
	private static final String SERIE_1_RESULT_FILENAME = "The.Big.Bang.Theory.S05E23.[LOL].avi";
	private static final String SERIE_2_RESULTS_FILENAME = "The.Big.Bang.Theory.S05E22.[LOL].avi";
	private static final String FILM_FILENAME = "Wild.Wild.West.[LOL].avi";
	private static final String SERIE_1_URL = "http://www.opensubtitles.org/fr/search2/sublanguageid-fre/season-5/episode-23/subformat-srt/moviename-the+big+bang+theory";
	private static final String SERIE_2_URL = "http://www.opensubtitles.org/fr/search2/sublanguageid-fre/season-5/episode-22/subformat-srt/moviename-the+big+bang+theory";
	private static final String FILM_URL = "http://www.opensubtitles.org/fr/search2/sublanguageid-fre/subformat-srt/moviename-wild+wild+west+%5Blol%5D";
	
	@Test
	public void testHasSubtitlesSerieFile() throws Exception {
		debug();
		
		// Test de l'URL
		OpenSubtitlesRequest request = new OpenSubtitlesRequest(LANG, SERIE_1_RESULT_FILENAME);
		OpenSubtitlesDownloader downloader = new OpenSubtitlesDownloader(request);
		Assert.assertEquals("URL", SERIE_1_URL, downloader.getQueryURL().toString());
		
		// Test de la présence de sous-titres
		Assert.assertTrue("Sous titres attendus pour "+SERIE_1_URL, downloader.hasSubtitles());
	}
	
	@Test
	public void testHasSubtitlesSerieFileMultipleResults() throws Exception {
		debug();
		
		// Test de l'URL
		OpenSubtitlesRequest request = new OpenSubtitlesRequest(LANG, SERIE_2_RESULTS_FILENAME);
		OpenSubtitlesDownloader downloader = new OpenSubtitlesDownloader(request);
		Assert.assertEquals("URL", SERIE_2_URL, downloader.getQueryURL().toString());
		
		// Test de la présence de sous-titres
		Assert.assertTrue("Sous titres attendus pour "+SERIE_2_URL, downloader.hasSubtitles());
	}
	
	@Test
	public void testHasSubtitlesNonSerieFile() throws Exception {
		debug();
		
		// Test de l'URL
		OpenSubtitlesRequest request = new OpenSubtitlesRequest(LANG, FILM_FILENAME);
		OpenSubtitlesDownloader downloader = new OpenSubtitlesDownloader(request);
		Assert.assertEquals("URL", FILM_URL, downloader.getQueryURL().toString());
		
		// Test de la présence de sous-titres
		Assert.assertFalse("Pas de sous titres attendus pour "+FILM_URL, downloader.hasSubtitles());
	}
	
	@Test
	public void testDownloadSerieFileSingleResult() throws Exception {
		debug();
		
		// Test depuis un fichier HTML résultat de hasSubtitles
		OpenSubtitlesRequest request = new OpenSubtitlesRequest(LANG, SERIE_1_RESULT_FILENAME);
		OpenSubtitlesDownloader downloader = new OpenSubtitlesDownloader(request);
		if ( downloader.hasSubtitles() ) {
			downloader.downloadFirstSubtitles();
		}
		Assert.assertEquals("Nombre de sous-titres invalides pour "+SERIE_1_RESULT_FILENAME, 1, downloader.getSubtitlesResults().size());
	}
	
	@Test
	public void testDownloadSerieFileMultipleResult() throws Exception {
		debug();
		
		// Test depuis un fichier HTML résultat de hasSubtitles
		OpenSubtitlesRequest request = new OpenSubtitlesRequest(LANG, SERIE_2_RESULTS_FILENAME);
		OpenSubtitlesDownloader downloader = new OpenSubtitlesDownloader(request);
		if ( downloader.hasSubtitles() ) {
			downloader.downloadFirstSubtitles();
		}
		Assert.assertEquals("Nombre de sous-titres invalides pour "+SERIE_2_RESULTS_FILENAME, 2, downloader.getSubtitlesResults().size());
	}
}
