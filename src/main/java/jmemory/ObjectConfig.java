package jmemory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class ObjectConfig {
	private String outClass;
	private String outSource;
	private String objectPath;
	private int    maxSerializaThread;
	private String jarMemory;

	private File config;
	private Properties prop;
	private static ObjectConfig instance;
	
	private ObjectConfig() throws FileNotFoundException, IOException {
		config = new File("config.object");
		prop = new Properties();

		prop.load(new FileInputStream(config));

		outClass           = prop.getProperty("OUTCLASS");
		outSource          = prop.getProperty("OUTSOURCE");
		objectPath         = prop.getProperty("OBJECTPATH");
		maxSerializaThread = Integer.parseInt(prop.getProperty("MAXSERIALIZETHREAD"));
		jarMemory          = prop.getProperty("JARMEMORY");
	}

	public static ObjectConfig getInstance() throws FileNotFoundException, IOException {
		if(instance == null)
			instance = new ObjectConfig();
		
		return instance;
	}
	
	public String getJarMemory() {
		return jarMemory;
	}

	public int getMaxSerializaThread() {
		return maxSerializaThread;
	}

	public String getObjectPath() {
		return objectPath;
	}

	public String getOutClass() {
		return outClass;
	}

	public String getOutSource() {
		return outSource;
	}
}
