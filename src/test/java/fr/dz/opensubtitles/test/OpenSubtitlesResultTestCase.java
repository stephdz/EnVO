package fr.dz.opensubtitles.test;

import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Test;

import fr.dz.opensubtitles.OpenSubtitlesDownloader;
import fr.dz.opensubtitles.OpenSubtitlesResult;

public class OpenSubtitlesResultTestCase {

	// Constantes
	private static final String SERIE_FILENAME = "The.Big.Bang.Theory.S05E23.[LOL].avi";
	private static final String ID = "4539609";
	private static final Boolean TRUSTED = Boolean.TRUE;
	private static final String URL = "http://www.opensubtitles.org/fr/subtitleserve/sub/" + ID;
	
	@Test
	public void testSerie() throws Exception {
		
		// Test depuis un fichier HTML résultat de hasSubtitles
		OpenSubtitlesResult result = new OpenSubtitlesResult(OpenSubtitlesDownloader.getFileContent("html/"+SERIE_FILENAME+".html"));
		Assert.assertEquals("Id", ID, result.getId());
		Assert.assertEquals("URL", URL, result.getDownloadURL().toString());
		Assert.assertEquals("Trusted", TRUSTED, result.getTrusted());
		Assert.assertEquals("FileNames", Arrays.asList(
				"The.Big.Bang.Theory.S05E23.HDTV.x264-LOL.mp4", 
				"The.Big.Bang.Theory.S05E23.HDTV.XviD-AFG.avi", 
				"The.Big.Bang.Theory.S05E23.720p.HDTV.x264-DIMENSION-[Spastik", 
				"The.Big.Bang.Theory.S05E23.720p.HDTV.X264-DIMENSION(480p).mk"), result.getFileNames());
	}
}
