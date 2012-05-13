package fr.dz.opensubtitles.test;

import junit.framework.Assert;

import org.junit.Test;

import fr.dz.opensubtitles.OpenSubtitlesRequest;

public class OpenSubtitlesRequestTestCase {

	// Constantes
	private static final String LANG = "fre";
	private static final String SERIE_FILENAME = "The.Big.Bang.Theory.S05E23.[LOL].avi";
	private static final String FILM_FILENAME = "Wild.Wild.West.[LOL].avi";
	private static final String SERIE = "the big bang theory";
	private static final String FILM = "wild wild west [lol]";
	private static final Integer SEASON = 5;
	private static final Integer EPISODE = 23;
	
	@Test
	public void testNull() throws Exception {
		OpenSubtitlesRequest request = new OpenSubtitlesRequest(null, null);
		Assert.assertNull("Lang", request.getLang());
		Assert.assertNull("File", request.getFile());
		Assert.assertNull("Query", request.getQuery());
		Assert.assertNull("Season", request.getSeason());
		Assert.assertNull("Episode", request.getEpisode());
		Assert.assertNull("Filesize", request.getFilesize());
	}
	
	@Test
	public void testNonExistingSerieFile() throws Exception {
		OpenSubtitlesRequest request = new OpenSubtitlesRequest(LANG, SERIE_FILENAME);
		Assert.assertEquals("Lang", LANG, request.getLang());
		Assert.assertEquals("File", SERIE_FILENAME, request.getFile());
		Assert.assertEquals("Query", SERIE, request.getQuery());
		Assert.assertEquals("Season", SEASON, request.getSeason());
		Assert.assertEquals("Episode", EPISODE, request.getEpisode());
		Assert.assertNull("Filesize", request.getFilesize());
	}
	
	@Test
	public void testNonExistingNonSerieFile() throws Exception {
		OpenSubtitlesRequest request = new OpenSubtitlesRequest(LANG, FILM_FILENAME);
		Assert.assertEquals("Lang", LANG, request.getLang());
		Assert.assertEquals("File", FILM_FILENAME, request.getFile());
		Assert.assertEquals("Query", FILM, request.getQuery());
		Assert.assertNull("Season", request.getSeason());
		Assert.assertNull("Episode", request.getEpisode());
		Assert.assertNull("Filesize", request.getFilesize());
	}
}
