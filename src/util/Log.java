package util;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;



public class Log {
	
	public static Logger logger = null;

	public synchronized static Logger getLogger() {
		if(logger==null){
			DOMConfigurator.configure(Const.LOG4J_CONFIG_PATH);
			logger = Logger.getLogger(Log.class.getClass());
		}	
		return logger;
	}

	public static void setLogger(Logger logger) {
		Log.logger = logger;
	}
	
	

}
