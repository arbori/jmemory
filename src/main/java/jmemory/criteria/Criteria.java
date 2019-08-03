package jmemory.criteria;

public abstract class Criteria<B, V> {
	String interfaceName;
	String property;
	String method;
	V value;
	
	public Criteria(String interfaceName, String property, V value) {
		this.interfaceName = interfaceName;
		this.property = property;
		this.method = "get" + property.substring(0, 1).toUpperCase() + property.substring(1, property.length());
		this.value = value;
	}
	
	public boolean check(B mb) {
		java.lang.reflect.Method m;
		V value;
		
		try {
			if((m = mb.getClass().getDeclaredMethod(method, new Class[0])) == null)
				return false;
			
			value = (V) m.invoke(mb, new Object[0]);
			
			return checkCriteria(value);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	protected abstract boolean checkCriteria(V value); 
}
