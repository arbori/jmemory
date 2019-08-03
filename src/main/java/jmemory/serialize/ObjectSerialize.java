package jmemory.serialize;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.ConcurrentLinkedQueue;

import jmemory.ObjectConfig;

class ObjectSerialize extends Thread {
	private static int numberOfThreads = 0;
	private static ConcurrentLinkedQueue<SerializeInfo> queue;
	
	private ObjectConfig conf;

	private SerializeInfo si;
	private boolean finish = false;
	
	ObjectSerialize() {
		try {
			conf = ObjectConfig.getInstance();
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		
		if(numberOfThreads == conf.getMaxSerializaThread())
			throw new RuntimeException("Maximum number of thread to serialize objects");
		
		numberOfThreads++;
	}

	static void setQueue(ConcurrentLinkedQueue<SerializeInfo> queueInfo) {
		queue = queueInfo;
	}
	
	public void run() {
		File dir;
		File persist = null;
		ObjectOutputStream oos = null;

		while(!finish) {
			while(queue.size() == 0) {
				try {
					sleep(500);
				} catch (InterruptedException e) { }
			}

			// Get the next request to sirialization.
			if(!next()) 
				continue;
			
			synchronized (si.getObjectContainer()) {
				dir = new File(
					conf.getObjectPath() +
					File.separator +
					si.getInterfaceName().substring(0, si.getInterfaceName().lastIndexOf(".")).replace(".", File.separator));
				
				persist = new File(
						conf.getObjectPath() +
					File.separator +
					si.getInterfaceName().replace(".", File.separator) +
					".persist");
				
				try {
					if(!dir.exists())
						dir.mkdirs();
					
					if(!persist.exists() && !persist.createNewFile())
						throw new IOException("Persisted object's file cannot be created for interface " +
								si.getInterfaceName());
					
					oos = new ObjectOutputStream(new FileOutputStream(persist));
					
					oos.writeObject(si.getObjectContainer());
				} catch (Throwable e) {
					e.printStackTrace(System.err);
				} finally {
					try {
						oos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	/**
	 * Set next request serialize information in attribute si.
	 * @return Return true if si and its attributes is not null. Return false otherwise. 
	 */
	private synchronized boolean next() {
		si = queue.poll();
		
		return si != null &&
			si.getInterfaceName() != null && 
			si.getObjectContainer() != null;
	}
	
	void setFinish(boolean finish) {
		this.finish = finish;
	}
}
