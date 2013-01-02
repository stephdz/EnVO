package fr.dz.opensubtitles.test;

import java.io.File;
import java.net.URL;

import org.junit.BeforeClass;
import org.junit.Test;

import fr.dz.opensubtitles.sources.AbstractOpenSubtitlesSource;


public class EncodingTestCase extends AbstractOpenSubtitlesTest {

	// Constantes
	private static final String FILE_PREFIX = "target/test-files/";
	private static final String OPENSUBTITLES_ISO_8859_1_URL = "http://www.opensubtitles.org/fr/subtitleserve/sub/4579903";
	private static final String OPENSUBTITLES_UTF_8_URL = "http://www.opensubtitles.org/fr/subtitleserve/sub/4729910";
	private static final String PODNAPISI_UTF_8_URL = "http://www.podnapisi.net/fr/ppodnapisi/download/i/1610920/k/4f5b8cb6a45402feee09405946d60946425a6b28";
	private static final String PODNAPISI_ISO_8859_1_URL = "http://www.podnapisi.net/fr/ppodnapisi/download/i/2139055/k/9b2e99d33987ea6c2745ef236fa2d9fefe0c71fa";
	
	@BeforeClass
	public static void init() {
		File dir = new File(FILE_PREFIX);
		dir.mkdirs();
	}
	
	@Test
	public void testOpenSubtitlesIso88591() throws Exception {
		debug();
		AbstractOpenSubtitlesSource.download(new URL(OPENSUBTITLES_ISO_8859_1_URL), FILE_PREFIX + "open_subtitles_iso.srt");
	}
	
	@Test
	public void testOpenSubtitlesUTF8() throws Exception {
		debug();
		AbstractOpenSubtitlesSource.download(new URL(OPENSUBTITLES_UTF_8_URL), FILE_PREFIX + "open_subtitles_utf8.srt");
	}
	
	@Test
	public void testPodnapisiUTF8() throws Exception {
		debug();
		AbstractOpenSubtitlesSource.download(new URL(PODNAPISI_ISO_8859_1_URL), FILE_PREFIX + "podnapisi_utf8.srt");
	}
	
	@Test
	public void testPodnapisiIso88591() throws Exception {
		debug();
		AbstractOpenSubtitlesSource.download(new URL(PODNAPISI_UTF_8_URL), FILE_PREFIX + "podnapisi_iso.srt");
	}
}
