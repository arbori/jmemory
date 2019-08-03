package jmemory.serialize;

import java.io.IOException;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

import jmemory.bean.MemoryBean;
import jmemory.bean.MemoryBeanComparator;
import jmemory.criteria.Criteria;

public class ObjectContainer implements Serializable {
	private static final long serialVersionUID = 3910727544099554441L;
	
	private static Hashtable<String, ObjectContainer> definedContainer = new Hashtable<String, ObjectContainer>();
	
	private String interfaceName;
	private long nextObjectId;
	private TreeSet<MemoryBean> objects;
	
	/**
	 * Create a new container for objects to the specific interface.  
	 * @param interfaceName is the interface of the objects.
	 * @throws Exception if the container already exist.
	 */
	public ObjectContainer(String interfaceName) throws IOException {
		if(definedContainer.get(interfaceName) != null)
			throw new IOException("A container for " + interfaceName + " alredy exist.");
		
		definedContainer.put(interfaceName, this);
		
		this.interfaceName = interfaceName;
		nextObjectId = Long.MIN_VALUE;
		objects = new TreeSet<MemoryBean>(new MemoryBeanComparator<MemoryBean>());
	}
	
	/**
	 * @return Returns the interfaceName.
	 */
	public String getInterfaceName() {
		return interfaceName;
	}
	/**
	 * @return Returns the nextObjectId.
	 */
	public long getNextObjectId() {
		return nextObjectId++;
	}
	
	/**
	 * @return Returns the objects.
	 */
	public TreeSet<MemoryBean> getObjects() {
		return objects;
	}

	/**
	 * Retorna uma lista de objetos que atendam os crit�rios passados
	 * @param objs
	 * @param criterias
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public MemoryBean[] search(Criteria[] criterias) {
		Vector<MemoryBean> result = new Vector<MemoryBean>();
		
		MemoryBean mb;
		boolean check = true;
		
		for(Iterator i = objects.iterator(); i.hasNext();) {
			mb = (MemoryBean) i.next();
			
			for(int c = 0; c < criterias.length; c++) 
				check = check && criterias[c].check(mb);
			
			if(check) result.add(mb);
		}
		
		MemoryBean[] beans = new MemoryBean[result.size()];
		result.copyInto(beans);
		
		return beans;
	}

	// TODO - Implements the search by object identification
	public MemoryBean search(long oid) {
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof ObjectContainer) &&
			getInterfaceName().equals(((ObjectContainer) obj).getInterfaceName()) &&
			getNextObjectId() == ((ObjectContainer) obj).getNextObjectId() &&
			getObjects().size() == ((ObjectContainer) obj).getObjects().size();
	}
}
