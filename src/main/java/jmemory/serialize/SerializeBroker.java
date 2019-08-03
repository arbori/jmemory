package jmemory.serialize;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import jmemory.ObjectConfig;


public class SerializeBroker {
	private static SerializeBroker instance; 
	private ConcurrentLinkedQueue<SerializeInfo> queue;
	private ArrayList<ObjectSerialize> threads;
	
	private ObjectConfig conf;

	private SerializeBroker() {
		try {
			conf = ObjectConfig.getInstance();
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}

		// Create the queue of request of serialize.
		queue = new ConcurrentLinkedQueue<SerializeInfo>();
		
		// Create list of threads
		threads = new ArrayList<ObjectSerialize>();
		
		// Create the threads to control request to serialize.
		for(int i = 0; i < conf.getMaxSerializaThread(); i++) {
			threads.add(new ObjectSerialize());
			
			threads.get(i).start();
		}
		
		// Set the queue of requests
		ObjectSerialize.setQueue(queue);
	}
	
	public static SerializeBroker getInstance() {
		if(instance == null)
			return new SerializeBroker();
		
		return instance;
	}

	public void serialize(String interfaceName, ObjectContainer objectContainer) {
		queue.add(new SerializeInfo(interfaceName, objectContainer));
	}
	
	public ObjectContainer deserialize(String interfaceName) throws IOException {
		File persist = new File(
			conf.getObjectPath() +
			File.separator +
			interfaceName.replace(".", File.separator) +
			".persist");
		
		ObjectInputStream ois = null;
			
		try {
			ois = new ObjectInputStream(new FileInputStream(persist));
			
			return (ObjectContainer) ois.readObject();
		} catch (FileNotFoundException e) {
			return null;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				if(ois != null) ois.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
