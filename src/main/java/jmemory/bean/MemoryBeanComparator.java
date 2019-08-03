package jmemory.bean;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Classe utilizada para comparar dois beans de dados utilizados no framework.
 * @author Marcelo Arbori Nogueira - marcelo.arbori@gmail.com
 */
public class MemoryBeanComparator<MB> implements Comparator<MB>, Serializable {
	private static final long serialVersionUID = -1583184587882278455L;

	public int compare(MB obj1, MB obj2) {
		MemoryBean o1 = (MemoryBean) obj1;
		MemoryBean o2 = (MemoryBean) obj2;
			
		return (int) (o1.objectIdentification() - o2.objectIdentification());
	}
}
