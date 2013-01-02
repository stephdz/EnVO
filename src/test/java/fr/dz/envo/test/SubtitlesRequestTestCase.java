package fr.dz.envo.test;

import java.io.File;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import fr.dz.envo.SubtitlesRequest;

public class SubtitlesRequestTestCase extends AbstractEnVOTest {

	// Constantes
	private static final String LANG = "fre";
	private static final String FILE_PREFIX = "target/test-files/";
	private static final String SERIE_FILENAME = FILE_PREFIX + "The.Big.Bang.Theory.S05E23.[LOL].avi";
	private static final String FILM_FILENAME = FILE_PREFIX + "Wild.Wild.West.[LOL].avi";
	private static final String SERIE = "the big bang theory";
	private static final String FILM = "wild wild west [lol]";
	private static final Integer SEASON = 5;
	private static final Integer EPISODE = 23;
	
	@BeforeClass
	public static void init() {
		File dir = new File(FILE_PREFIX);
		dir.mkdirs();
	}
	
	@Test
	public void testNull() throws Exception {
		debug();
		SubtitlesRequest request = new SubtitlesRequest(null, null);
		Assert.assertNull("Lang", request.getLang());
		Assert.assertNull("File", request.getFilename());
		Assert.assertNull("Query", request.getQuery());
		Assert.assertNull("Season", request.getSeason());
		Assert.assertNull("Episode", request.getEpisode());
		Assert.assertNull("Filesize", request.getFilesize());
	}
	
	@Test
	public void testNonExistingSerieFile() throws Exception {
		debug();
		SubtitlesRequest request = new SubtitlesRequest(LANG, SERIE_FILENAME);
		Assert.assertEquals("Lang", LANG, request.getLang());
		Assert.assertEquals("File", SERIE_FILENAME, FILE_PREFIX + request.getFilename());
		Assert.assertEquals("Query", SERIE, request.getQuery());
		Assert.assertEquals("Season", SEASON, request.getSeason());
		Assert.assertEquals("Episode", EPISODE, request.getEpisode());
		Assert.assertNull("Filesize", request.getFilesize());
	}
	
	@Test
	public void testNonExistingSerieFilePrefix() throws Exception {
		debug();
		SubtitlesRequest request = new SubtitlesRequest(LANG, "/home/steph/"+SERIE_FILENAME);
		Assert.assertEquals("Lang", LANG, request.getLang());
		Assert.assertEquals("File", SERIE_FILENAME, FILE_PREFIX + request.getFilename());
		Assert.assertEquals("Query", SERIE, request.getQuery());
		Assert.assertEquals("Season", SEASON, request.getSeason());
		Assert.assertEquals("Episode", EPISODE, request.getEpisode());
		Assert.assertNull("Filesize", request.getFilesize());
	}
	
	@Test
	public void testNonExistingNonSerieFile() throws Exception {
		debug();
		SubtitlesRequest request = new SubtitlesRequest(LANG, FILM_FILENAME);
		Assert.assertEquals("Lang", LANG, request.getLang());
		Assert.assertEquals("File", FILM_FILENAME, FILE_PREFIX + request.getFilename());
		Assert.assertEquals("Query", FILM, request.getQuery());
		Assert.assertNull("Season", request.getSeason());
		Assert.assertNull("Episode", request.getEpisode());
		Assert.assertNull("Filesize", request.getFilesize());
	}
}
