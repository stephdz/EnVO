package fr.dz.envo.test;

import java.io.File;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import fr.dz.envo.SubtitlesRequest;
import fr.dz.envo.sources.OpenSubtitlesDownloader;


public class OpenSubtitlesDownloaderTestCase extends AbstractEnVOTest {

	// Constantes
	private static final String LANG = "fre";
	private static final String FILE_PREFIX = "target/test-files/";
	private static final String SERIE_1_RESULT_FILENAME = FILE_PREFIX + "The.Big.Bang.Theory.S05E23.[LOL].avi";
	private static final String SERIE_2_RESULTS_FILENAME = FILE_PREFIX + "The.Big.Bang.Theory.S05E22.[LOL].avi";
	private static final String FILM_FILENAME = FILE_PREFIX + "Wild.Wild.West.[LOL].avi";
	private static final String SERIE_1_URL = "http://www.opensubtitles.org/fr/search2/sublanguageid-fre/season-5/episode-23/subformat-srt/moviename-the+big+bang+theory";
	private static final String SERIE_2_URL = "http://www.opensubtitles.org/fr/search2/sublanguageid-fre/season-5/episode-22/subformat-srt/moviename-the+big+bang+theory";
	private static final String FILM_URL = "http://www.opensubtitles.org/fr/search2/sublanguageid-fre/subformat-srt/moviename-wild+wild+west+%5Blol%5D";
	
	@BeforeClass
	public static void init() {
		File dir = new File(FILE_PREFIX);
		dir.mkdirs();
	}
	
	@Test
	public void testHasSubtitlesSerieFile() throws Exception {
		debug();
		
		// Test de l'URL
		SubtitlesRequest request = new SubtitlesRequest(LANG, SERIE_1_RESULT_FILENAME);
		OpenSubtitlesDownloader downloader = new OpenSubtitlesDownloader(request);
		Assert.assertEquals("URL", SERIE_1_URL, downloader.getQueryURL().toString());
		
		// Test de la présence de sous-titres
		Assert.assertTrue("Sous titres attendus pour "+SERIE_1_URL, downloader.hasSubtitles());
	}
	
	@Test
	public void testHasSubtitlesSerieFileMultipleResults() throws Exception {
		debug();
		
		// Test de l'URL
		SubtitlesRequest request = new SubtitlesRequest(LANG, SERIE_2_RESULTS_FILENAME);
		OpenSubtitlesDownloader downloader = new OpenSubtitlesDownloader(request);
		Assert.assertEquals("URL", SERIE_2_URL, downloader.getQueryURL().toString());
		
		// Test de la présence de sous-titres
		Assert.assertTrue("Sous titres attendus pour "+SERIE_2_URL, downloader.hasSubtitles());
	}
	
	@Test
	public void testHasSubtitlesNonSerieFile() throws Exception {
		debug();
		
		// Test de l'URL
		SubtitlesRequest request = new SubtitlesRequest(LANG, FILM_FILENAME);
		OpenSubtitlesDownloader downloader = new OpenSubtitlesDownloader(request);
		Assert.assertEquals("URL", FILM_URL, downloader.getQueryURL().toString());
		
		// Test de la présence de sous-titres
		Assert.assertTrue("Sous titres attendus pour "+FILM_URL, downloader.hasSubtitles());
	}
	
	@Test
	public void testDownloadSerieFileSingleResult() throws Exception {
		debug();
		
		// Test depuis un fichier HTML résultat de hasSubtitles
		SubtitlesRequest request = new SubtitlesRequest(LANG, SERIE_1_RESULT_FILENAME);
		OpenSubtitlesDownloader downloader = new OpenSubtitlesDownloader(request);
		if ( downloader.hasSubtitles() ) {
			downloader.downloadFirstSubtitles();
		}
		Assert.assertEquals("Nombre de sous-titres invalides pour "+SERIE_1_RESULT_FILENAME, 2, downloader.getSubtitlesResults().size());
	}
	
	@Test
	public void testDownloadSerieFileMultipleResult() throws Exception {
		debug();
		
		// Test depuis un fichier HTML résultat de hasSubtitles
		SubtitlesRequest request = new SubtitlesRequest(LANG, SERIE_2_RESULTS_FILENAME);
		OpenSubtitlesDownloader downloader = new OpenSubtitlesDownloader(request);
		if ( downloader.hasSubtitles() ) {
			downloader.downloadFirstSubtitles();
		}
		Assert.assertEquals("Nombre de sous-titres invalides pour "+SERIE_2_RESULTS_FILENAME, 5, downloader.getSubtitlesResults().size());
	}
}
