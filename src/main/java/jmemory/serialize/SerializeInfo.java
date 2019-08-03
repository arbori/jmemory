package jmemory.serialize;

class SerializeInfo {
	SerializeInfo() {}
	
	SerializeInfo(String interfaceName, ObjectContainer objectContainer) {
		this.interfaceName = interfaceName;
		this.objectContainer = objectContainer;
	}
	
	private String interfaceName;
	public String getInterfaceName() { return interfaceName; }
	public void setInterfaceName(String interfaceName) { this.interfaceName = interfaceName; }

	private ObjectContainer objectContainer;
	public ObjectContainer getObjectContainer() { return objectContainer; }
	public void setObjectContainer(ObjectContainer objectContainer) { this.objectContainer = objectContainer; }
}

