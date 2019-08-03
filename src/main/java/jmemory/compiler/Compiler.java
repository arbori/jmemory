package jmemory.compiler;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Hashtable;

public class Compiler {
	public static final String PREFIX = "MemoryBean";
	
	/**
	 * Gera o nome do atributo a partir do método get. 
	 * @param Recebe o método para gerar o nome. 
	 * @return Retorna o nome do atributo para o método get informado ou nulo caso contrário.
	 */
	private static String makeAttributeName(String method) {
		if(method.startsWith("get") || method.startsWith("set")) {
			method = method.substring(3, 4).toLowerCase() + method.substring(4, method.length());
		} else {
			return null;
		}
		
		return method; 
	}
	
	/**
	 * Insere um atributo, na lista de atributos do bean, gerado a partir de um método get definido na interface do bean.
	 * @param attrs é a Hashtable para armazenar os atributos.
	 * @param method é o método do qual se pretende gerar um atributo.
	 */
	private static void addAttribute(Hashtable<String, String> attrs, Method method) {
		String attrName = makeAttributeName(method.getName());
		
		if(attrName != null && attrs.get(attrName) == null && !method.getReturnType().getName().equals("void"))
			attrs.put(attrName, method.getReturnType().getName());
	}

	/**
	 * Compila a implementação da interface de dados registrada no framework como uma interface de dados.
	 * @param sourcePath é o diretório onde se encontra os arquivos fontes das classes que implementam as interfaces de dados.
	 * @param classPath é o classpath da aplicação.
	 * @param outPath é o diretório onde as classes geradas serão gravadas. É recomendável que este diretório faça parte do classpath da aplicação.
	 * @param interfaceName é o nome da interface de dados. 
	 * @throws ClassNotFoundException
	 * @throws IntrospectionException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void buildClass(String sourcePath, String classPath, String outPath, String interfaceName)
		throws ClassNotFoundException, IntrospectionException, IOException, InterruptedException
	{
		buildClass(sourcePath, classPath, outPath, Class.forName(interfaceName));
	}
	
	/**
	 * Compila a implementação da interface de dados registrada no framework como uma interface de dados.
	 * @param sourcePath é o diretório onde se encontra os arquivos fontes das classes que implementam as interfaces de dados.
	 * @param classPath é o classpath da aplicação.
	 * @param outPath é o diretório onde as classes geradas serão gravadas. É recomendável que este diretório faça parte do classpath da aplicação.
	 * @param interfaceClass é o objeto Class da interface de dados. 
	 * @throws ClassNotFoundException
	 * @throws IntrospectionException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void buildClass(String sourcePath, String classPath, String outPath, Class interfaceClass)
		throws ClassNotFoundException, IntrospectionException, IOException, InterruptedException
	{
		// Não faz nada se não for uma Serializable.
		if(!(interfaceClass instanceof java.io.Serializable))
			return;
		
		//boolean compilou = false;
		String line;
		StringBuffer erroMessage;
		StringBuffer lineCommand = new StringBuffer()
			.append("javac -cp ")
			.append(classPath)
			.append(" -sourcepath ")
			.append(sourcePath)
			.append(" -d ")
			.append(outPath)
			.append(" ")
			.append(sourcePath)
			.append(File.separator)
			.append(interfaceClass.getName().replace(".", File.separator))
			.append(Compiler.PREFIX)
			.append(".java");
		
		Process p = Runtime.getRuntime().exec(lineCommand.toString());
		
		int processResult = p.waitFor(); 
		if(processResult != 0) {
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			erroMessage = new StringBuffer().append("\n");
			
			while((line = input.readLine()) != null) { 
				erroMessage.append(line);
				erroMessage.append("\n");
			}
			
			if(erroMessage.toString().trim().length() != 0)
				throw new RuntimeException(erroMessage.toString());
		}
	}

	/**
	 * Gera o arquivo fonte do classe bean que implementa a interface bean passada, no diretório de arquivos fontes, dentro do pacote apropriado. 
	 * @param sourcePath é o diretório onde os arquivos fontes estão.
	 * @param interfaceClass é a interface definida para ser implementada no bean gerado.
	 * @throws IntrospectionException
	 * @throws ClassNotFoundException
	 * @throws IOException 
	 */
	public static void buildSource(String sourcePath, String interfaceClass)
		throws IntrospectionException, ClassNotFoundException, IOException
	{
		buildSource(sourcePath, Class.forName(interfaceClass));
	}
	
	/**
	 * Gera o arquivo fonte do classe bean que implementa a interface bean passada, no diretório de arquivos fontes, dentro do pacote apropriado. 
	 * @param sourcePath é o diretório onde os arquivos fontes que implementa as interfaces de dados serão gerados.
	 * @param interfaceClass é a interface de dados a ser implementada no bean persistido em memória.
	 * @throws IntrospectionException
	 * @throws ClassNotFoundException
	 * @throws IOException 
	 */
	public static void buildSource(String sourcePath, Class interfaceClass)
		throws IntrospectionException, ClassNotFoundException, IOException
	{
		// Não faz nada se não for uma Serializable.
		if(!(interfaceClass instanceof java.io.Serializable))
			return;
		
		StringBuffer diretory = new StringBuffer();
		Package pack = interfaceClass.getPackage();
		
		diretory.append(sourcePath);
		diretory.append(File.separator);
		if(pack != null) diretory.append(pack.getName().replace(".", File.separator));
		diretory.append(File.separator);
		
		// Define o arqui
		File dir = new File(diretory.toString());

		// Se o diretorio não existir, cria um novo.
		if(!dir.exists())
			if(!dir.mkdirs()) throw new IOException("Não criou o diretório " + dir);
		
		// Define o output stream para o arquivo fonte gerado.
		PrintStream out = new PrintStream(new File(
			sourcePath + File.separator +
			interfaceClass.getName().replace(".", File.separator) + Compiler.PREFIX + ".java")); 

		// Lista de atributos da classe.
		Hashtable<String,String> attributes = new Hashtable<String,String>();
		// Chave da lista de atributos.
		String key;

		// Informações sobre a interface bean.
		BeanInfo bi = Introspector.getBeanInfo(interfaceClass);
		// Lista de métodos definidos na interface.
		MethodDescriptor[] methods = bi.getMethodDescriptors();
		// Lista de parâmetros do método.
		Class[] params; 
		
		// Monta a lista de atributos.
		for(int m = 0; m < methods.length; m++) {
			addAttribute(attributes, methods[m].getMethod());
		}

		// Monta o cabeçalho da classe.
		if(pack != null) out.println("package " + pack.getName() + ";");
		
		out.print("public class " + interfaceClass.getSimpleName() + Compiler.PREFIX);
		out.print(" extends br.com.jmemory.bean.MemoryBean");
		out.println(" implements " + interfaceClass.getName() + " {");

		// Coloca os atributos da lista de atributos
		for(Enumeration keys = attributes.keys(); keys.hasMoreElements(); ) {
			key = (String) keys.nextElement();
			
			out.print("\tprivate ");
			out.println(attributes.get(key) + " " + key + ";");
		}

		// Monta o construtor default.
		out.print("\tpublic ");
		out.println(interfaceClass.getSimpleName() + Compiler.PREFIX + "(long oid) { ");
		out.println("\t\tobjectIdentification = oid;");
		out.println("\t}");
		
		// Monta o construtor de atribuição dos atributos.
		out.print("\tpublic ");
		out.print(interfaceClass.getSimpleName() + Compiler.PREFIX + "(long oid");
		for(Enumeration keys = attributes.keys(); keys.hasMoreElements(); ) {
			key = (String) keys.nextElement();
			
			out.print(", ");
			out.print(attributes.get(key) + " " + key);
		}
		out.println(") {");
		out.println("\t\tobjectIdentification = oid;");
		for(Enumeration keys = attributes.keys(); keys.hasMoreElements(); ) {
			key = (String) keys.nextElement();
			
			out.println("\t\tthis." + key + " = " + key + ";");
		}
		out.println("\t}");

		// Monta os métodos get e set.
		for(int m = 0; m < methods.length; m++) {
			params = methods[m].getMethod().getParameterTypes();

			out.print("\tpublic "); // Todos os métodos serão públicos.
			out.print(methods[m].getMethod().getReturnType().getName() + " ");
			out.print(methods[m].getMethod().getName() + " (");
			// Se houver parametro(s) definido(s), apenas um é colocado.
			if(params != null && params.length > 0) out.print(params[0].getName() + " value");
			out.println(") {");
			
			key = makeAttributeName(methods[m].getMethod().getName()); // Pega o atributo referente ao método.
			if(key == null)
				continue;
			
			// Se for um método get, escreve o código para retorno do valor.
			if(attributes.get(key) != null && methods[m].getMethod().getName().startsWith("get")) {
				out.println("\t\treturn " + key + ";");
			}
			// Se for um método set, escreve o código para atribuição do valor.
			else if(attributes.get(key) != null && methods[m].getMethod().getName().startsWith("set")) {
				out.println("\t\t" + "sinchronized = false;");
				out.println("\t\t" + key + " = value;");
			}
				
			// Fecha a chave do método.	
			out.println("\t}");
		}
		
		// Fecha a chave da classe.	
		out.println("}");
	}
}
