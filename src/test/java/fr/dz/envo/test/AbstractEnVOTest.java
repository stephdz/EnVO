package fr.dz.envo.test;

import org.apache.log4j.Logger;

public abstract class AbstractEnVOTest {

	private static final Logger LOGGER = Logger.getLogger(AbstractEnVOTest.class);
	
	public void debug() {
		StackTraceElement ste = getTestStackTraceElement();
		LOGGER.debug("#####################################################################");
		LOGGER.debug("# Début du test : "+getSimpleName(ste.getClassName())+"."+ste.getMethodName());
		LOGGER.debug("#####################################################################");
	}
	
	private String getSimpleName(String className) {
		return className.substring(className.lastIndexOf(".")+1);
	}

	private StackTraceElement getTestStackTraceElement() {
		Exception ex = new Exception();
		StackTraceElement[] ste = ex.getStackTrace();
		int i = 0;
		while ( ste[i].getClassName().indexOf(AbstractEnVOTest.class.getSimpleName()) != -1 ) {
			i++;
		}
		return ste[i]; 
	}
}
