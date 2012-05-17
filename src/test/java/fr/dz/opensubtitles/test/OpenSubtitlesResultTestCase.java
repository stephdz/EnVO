package fr.dz.opensubtitles.test;

import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Test;

import fr.dz.opensubtitles.OpenSubtitlesDownloader;
import fr.dz.opensubtitles.OpenSubtitlesRequest;
import fr.dz.opensubtitles.OpenSubtitlesResult;
import fr.dz.opensubtitles.OpenSubtitlesResult.OpenSubtitleResultFile;

public class OpenSubtitlesResultTestCase extends AbstractOpenSubtitlesTest {

	// Constantes
	private static final String LANG = "fre";
	private static final String SERIE_FILENAME = "The.Big.Bang.Theory.S05E23.[LOL].avi";
	private static final String ID = "4539609";
	private static final Boolean TRUSTED = Boolean.TRUE;
	private static final String URL = "http://www.opensubtitles.org/fr/subtitleserve/sub/" + ID;
	
	@Test
	public void testSerie() throws Exception {
		debug();
		
		// Test depuis un fichier HTML résultat de hasSubtitles
		OpenSubtitlesRequest request = new OpenSubtitlesRequest(LANG, SERIE_FILENAME);
		OpenSubtitlesResult result = new OpenSubtitlesResult(request, OpenSubtitlesDownloader.getFileContent("html/"+SERIE_FILENAME+".html"));
		Assert.assertEquals("Id", ID, result.getId());
		Assert.assertEquals("URL", URL, result.getDownloadURL().toString());
		Assert.assertEquals("Trusted", TRUSTED, result.getTrusted());
		Assert.assertEquals("FileNames", Arrays.asList(
					createFile("4674444", 146058808L, "The.Big.Bang.Theory.S05E23.HDTV.x264-LOL.mp4", "the.big.bang.theory.523.hdtv-lol.mp4"),
					createFile("4674625", 186561852L, "The.Big.Bang.Theory.S05E23.HDTV.XviD-AFG.avi"),
					createFile("4676766", 575151152L, "The.Big.Bang.Theory.S05E23.720p.HDTV.x264-DIMENSION-[SpastikusTV].mkv", "The.Big.Bang.Theory.S05E23.720p.HDTV.X264-DIMENSION.mkv"),
					createFile("4684146", 74149570L, "The.Big.Bang.Theory.S05E23.720p.HDTV.X264-DIMENSION(480p).mkv")),
				result.getFiles());
	}

	/**
	 * Création d'un OpenSubtitleResultFile
	 * @param request
	 * @param id
	 * @param size
	 * @param filenames
	 * @return
	 */
	private OpenSubtitleResultFile createFile(String id, Long size, String... filenames) {
		OpenSubtitleResultFile result = new OpenSubtitleResultFile(id, size);
		if ( filenames != null ) {
			for ( String filename : filenames ) {
				result.getFileNames().add(filename);
			}
		}
		return result;
	}
}
