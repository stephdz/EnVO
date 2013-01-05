package fr.dz.envo.test;

import junit.framework.Assert;

import org.junit.Test;

import fr.dz.envo.api.SubtitlesRequest;
import fr.dz.envo.sources.OpenSubtitlesDownloader;
import fr.dz.envo.sources.PodnapisiDownloader;


public class LanguageTestCase extends AbstractEnVOTest {
	
	// Contantes
	private static final String SERIE_FILENAME = "The.Big.Bang.Theory.S05E23.avi";
	private static final String[] ISO639_2_LANGUAGES = {"fre","eng","ger","spa","chi"};
	private static final String[] OPENSUBTITLES_LANGUAGES = ISO639_2_LANGUAGES;
	private static final String[] PODNAPISI_LANGUAGES = {"8","2","5","28","17"};
	
	@Test
	public void testOpenSubtitlesLanguage() throws Exception {
		debug();
		OpenSubtitlesDownloader downloader = new OpenSubtitlesDownloader();
		downloader.init(new SubtitlesRequest("fre", SERIE_FILENAME)); // Juste pour éviter une NullPointerException
		for ( int i = 0; i < ISO639_2_LANGUAGES.length; i++ ) {
			Assert.assertEquals("Code langue incorrect pour "+ISO639_2_LANGUAGES[i], OPENSUBTITLES_LANGUAGES[i], downloader.getSpecificLanguageCode(ISO639_2_LANGUAGES[i]));
		}
	}
	
	@Test
	public void testPodnapisiLanguage() throws Exception {
		debug();
		PodnapisiDownloader downloader = new PodnapisiDownloader();
		downloader.init(new SubtitlesRequest("fre", SERIE_FILENAME)); // Juste pour éviter une NullPointerException
		for ( int i = 0; i < ISO639_2_LANGUAGES.length; i++ ) {
			Assert.assertEquals("Code langue incorrect pour "+ISO639_2_LANGUAGES[i], PODNAPISI_LANGUAGES[i], downloader.getSpecificLanguageCode(ISO639_2_LANGUAGES[i]));
		}
	}
}
