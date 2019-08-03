package jmemory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;

import jmemory.bean.MemoryBean;
import jmemory.criteria.Criteria;
import jmemory.serialize.ObjectContainer;
import jmemory.serialize.SerializeBroker;
import jmemory.compiler.Compiler;

public class ObjectBroker { 
	private static ObjectBroker broker;
	
	private SerializeBroker serialize;
	
	private Hashtable<String, ObjectContainer> objects;
	
	private ObjectBroker() throws IOException, ClassNotFoundException {
		serialize = SerializeBroker.getInstance();
	}
	
	/**
	 * Get the broker's instance.
	 * @return The broker's instance.
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public static ObjectBroker getInstance() throws IOException, ClassNotFoundException {
		if(broker == null) broker = new ObjectBroker();
		
		return broker;
	}
	
	/**
	 * Cria um novo objeto administrado pelo framework de persist�ncia.
	 * @param interfaceName � a interface do bean de dados.
	 * @return Retorno o objeto administrado pelo framework que implementa a interface de dados.
	 * @throws ClassNotFoundException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws SecurityException 
	 * @throws IllegalArgumentException 
	 * @throws InstantiationException
	 */
	public Object create(String interfaceName)
		throws ClassNotFoundException, IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException,
			   InvocationTargetException, NoSuchMethodException, IOException
	{
		ObjectContainer oc;
		
		if(!(Class.forName(interfaceName) instanceof java.io.Serializable))
			throw new RuntimeException(interfaceName + " is not a java.io.Serialize.");
		
		// Se o container da interface solicitada n�o estiver em mem�ria
		// ou serializado em disco, um novo container para a interface � criado.
		if((oc = objects.get(interfaceName)) == null) {
			if((oc = serialize.deserialize(interfaceName)) == null) {
				oc = new ObjectContainer(interfaceName);
				objects.put(interfaceName, oc);
			}
		}
		
		// Obt�m a classe que implementa a interface passada.
		Class<?> c = Class.forName(interfaceName + Compiler.PREFIX);
		
		// Cria um novo objeto de dados definindo seu identificador.
		MemoryBean result = (MemoryBean) c.getConstructor(long.class).newInstance(
			oc.getNextObjectId());
		
		// Insere o objeto no controle de persist�ncia.
		oc.getObjects().add(result);
		
		// Persiste o container com o novo objeto.
		serialize.serialize(interfaceName, oc);
		
		return result; 
	}
	
	/**
	 * Serializa o objeto e permite que o usu�rio deixe de us�-lo.
	 * @param obj
	 */
	public void leave(String interfaceName, Object obj) {
		// N�o faz nada se for n�o for uma classe que implemente Serialize. 
		if(!(obj instanceof jmemory.bean.MemoryBean)) return;

		// Se o objeto n�o foi alterado, n�o � preciso sincroniza-lo.
		if(((MemoryBean) obj).isSincronized())
			return;
		
		// Adiciona uma solicita��o para serializa��o.
		serialize.serialize(interfaceName, objects.get(interfaceName));
	}

	/**
	 * Serializa o conjunto de objetos e permite que o usu�rio deixe de us�-los.
	 * @param obj
	 */
	public void leave(String interfaceName, Object[] objs) {
		for(Object o: objs)
			leave(interfaceName, o);
	}

	/**
	 * Retrieve a set of objects from persistence framework.
	 * @param interfaceName
	 * @param criterias
	 * @return
	 */
	public Object[] search(String interfaceName, Criteria[] criterias) {
		Object[] o = new Object[0];
		
		if(objects.get(interfaceName) == null)
			return o;
		
		o = objects.get(interfaceName).search(criterias);
		
		return o;
	}
	
	/**
	 * Erased the object from persistence framwork.
	 * @param interfaceName
	 * @param criterias
	 * @return
	 */
	public boolean delete(String interfaceName, Object o) {
		// Get container to interface. 
		if(objects.get(interfaceName) == null)
			return false;

		// Remove the object from container
		if(!objects.get(interfaceName).getObjects().remove(o))
			return false;
			
		// Persiste o container com o novo objeto.
		serialize.serialize(interfaceName, objects.get(interfaceName));

		// Return objects retrived.
		return true;
	}

	/**
	 * Retrieve object erased from persistence framwork.
	 * @param interfaceName
	 * @param criterias
	 * @return
	 */
	public Object[] delete(String interfaceName, Criteria[] criterias) {
		Object[] o = new Object[0];
		
		// Get container to interface. 
		if(objects.get(interfaceName) == null)
			return o;

		// Retrieve objects
		o = objects.get(interfaceName).search(criterias);
		
		// Remove objects from container
		for(Object obj : o)
			objects.remove(obj);
			
		// Persiste o container com o novo objeto.
		serialize.serialize(interfaceName, objects.get(interfaceName));

		// Return objects retrived.
		return o;
	}
}
