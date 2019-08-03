package jmemory;

import java.beans.IntrospectionException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jmemory.compiler.Compiler;

public class ObjectRecord {
	/**
	 * Registra as interfaces contidas na origem indicada  de 
	 * @param classPath
	 */
	public static void record(String classPath)
		throws ClassNotFoundException, IntrospectionException, IOException, InterruptedException
	{
		ObjectConfig conf = ObjectConfig.getInstance();
		
		File dir = new File(classPath);
		File markFile = new File(classPath + File.separator + ".recorded");

		if(!dir.isDirectory())
			throw new RuntimeException("The classpath must be a diretory.");
		
		if(markFile.exists())
			return;
		
		List<String> fileFullName = buildClassFileList(classPath);
		for(int i = 0; i < fileFullName.size(); i++)
			Compiler.buildSource(conf.getOutSource(), makeClassName(classPath, fileFullName.get(i).toString()));
		
		for(int i = 0; i < fileFullName.size(); i++) {
			Compiler.buildClass(
					conf.getOutSource(),
				"%CLASSPATH%" + File.pathSeparator + conf.getJarMemory() + File.pathSeparator + conf.getOutClass(),
				conf.getOutClass(),
				makeClassName(classPath, fileFullName.get(i).toString()));
		}
		
		markFile.createNewFile();
	}
	
	protected static String makeClassName(String classPath, String classFullFileName) {
		return classFullFileName
			.substring(classPath.length() + 1, classFullFileName.indexOf(".class"))
			.replace(File.separator, ".");
	}
	
	protected static List<String> buildClassFileList(String classPath) {
		ArrayList<String> result = new ArrayList<String>();
		
		File dir = new File(classPath);
		File file;
		
		String[] files = dir.list();
		for(int i = 0; i < files.length; i++) {
			file = new File(dir.toString() + File.separator + files[i]);
			
			if(file.isFile())
				result.add(file.toString());
			else if(file.isDirectory())
				result.addAll(buildClassFileList(file.toString()));
		}
		
		return result;
	}
}
