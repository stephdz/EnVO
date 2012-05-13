package fr.dz.opensubtitles.test;

import junit.framework.Assert;

import org.junit.Test;

import fr.dz.opensubtitles.OpenSubtitlesDownloader;
import fr.dz.opensubtitles.OpenSubtitlesRequest;

public class OpenSubtitlesDownloaderTestCase {

	// Constantes
	private static final String LANG = "fre";
	private static final String SERIE_FILENAME = "The.Big.Bang.Theory.S05E23.[LOL].avi";
	private static final String FILM_FILENAME = "Wild.Wild.West.[LOL].avi";
	private static final String SERIE_URL = "http://www.opensubtitles.org/fr/search2/sublanguageid-fre/season-5/episode-23/subformat-srt/moviename-the+big+bang+theory";
	private static final String FILM_URL = "http://www.opensubtitles.org/fr/search2/sublanguageid-fre/subformat-srt/moviename-wild+wild+west+%5Blol%5D";
	
	@Test
	public void testNonExistingSerieFile() throws Exception {
		
		// Test de l'URL
		OpenSubtitlesRequest request = new OpenSubtitlesRequest(LANG, SERIE_FILENAME);
		OpenSubtitlesDownloader downloader = new OpenSubtitlesDownloader(request);
		Assert.assertEquals("URL", SERIE_URL, downloader.getQueryURL().toString());
		
		// Test de la présence de sous-titres
		Assert.assertTrue("Sous titres attendus pour "+SERIE_URL, downloader.hasSubtitles());
	}
	
	@Test
	public void testNonExistingNonSerieFile() throws Exception {
		
		// Test de l'URL
		OpenSubtitlesRequest request = new OpenSubtitlesRequest(LANG, FILM_FILENAME);
		OpenSubtitlesDownloader downloader = new OpenSubtitlesDownloader(request);
		Assert.assertEquals("URL", FILM_URL, downloader.getQueryURL().toString());
		
		// Test de la présence de sous-titres
		Assert.assertFalse("Pas de sous titres attendus pour "+FILM_URL, downloader.hasSubtitles());
	}
}
