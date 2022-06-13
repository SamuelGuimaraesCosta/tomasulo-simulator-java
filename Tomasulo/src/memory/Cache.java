package memory;

public class Cache {
	static int level;
	
	static L1Cache l1Cache;
	static L2Cache l2Cache;
	static L3Cache l3Cache;

	public Cache(int lev, L1Cache l1, L2Cache l2, L3Cache l3) {
		level = lev;
		l1Cache = l1;
		l2Cache = l2;
		l3Cache = l3;
	}

	public static Object read(int address, long currentTime, Instruction instruction, boolean readInstruction) throws Exception {
		instruction.setCacheStartTime(currentTime);
		Object value;

		/*
			Se há somente cache de nível 1 (encontrado) retornar o valor e definir o horário de término
			para a hora atual + tempo de acesso do cache l1, caso contrário, chame read miss e set
			na hora de término para a hora atual + hora de acesso do cache l1 + acesso mem tempo
		*/
		if (level == 1) {
			try {
				value = l1Cache.readFromCache(address, currentTime, readInstruction);
				
				instruction.setCacheEndTime(currentTime + l1Cache.getCycles());
				
				System.out.println("Encontrou o valor na cache L1.");
			} catch (Exception e) {
				// Não achou na Cache L1
				System.out.println("Não encontrou o valor na cache L1: " + e.getMessage());
				
				if (e.getMessage().equals(L1Cache.NOT_FOUND)) {
					value = l1Cache.readMiss(address, currentTime);
					
					instruction.setCacheEndTime(currentTime + l1Cache.getCycles() + Memory.getAccessTime());
				} else {
					throw e;
				}
			}
			return value;
		} else
		/*
		 	Se há dois níveis de cache, tenta em L1, se achou retorna se não tenta L2,
		 	se não achou em L2, ele dá miss.
		*/
		if (level == 2) {
			try {
				value = l1Cache.readFromCache(address, currentTime, readInstruction);
				
				instruction.setCacheEndTime(currentTime + l1Cache.getCycles());
				
				System.out.println("Encontrou o valor na cache L1.");
			} catch (Exception e) {
				System.out.println("Não encontrou o valor na cache L1: " + e.getMessage());
				
				if (e.getMessage().equals(L1Cache.NOT_FOUND)) {
					try {
						value = l2Cache.readFromCache(address, currentTime);
						
						instruction.setCacheEndTime(currentTime + l1Cache.getCycles() + l2Cache.getCycles());
						
						System.out.println("Encontrou o valor na cache L2.");
					} catch (Exception e1) {
						// Não achou na Cache L2
						System.out.println("Não encontrou o valor na cache L2: " + e.getMessage());
						
						if (e1.getMessage().equals(L2Cache.NOT_FOUND)) {
							value = l2Cache.readMiss(address, currentTime);
							
							instruction.setCacheEndTime(currentTime + l1Cache.getCycles() + l2Cache.getCycles() + Memory.getAccessTime());
						} else {
							throw e1;
						}
					}
				} else {
					throw e;
				}
			}
			return value;
		}
		/*
		 	Se há 3 níveis de cache, primeiro se procura na L1, depois na L2 e se não encontrou na L3, caso contrário há
		 	miss e o tempo gasto será maior.
		 */

		else {
			try {
				value = l1Cache.readFromCache(address, currentTime, readInstruction);
				
				instruction.setCacheEndTime(currentTime + l1Cache.getCycles());
				
				System.out.println("Encontrou o valor na cache L1.");
			} catch (Exception e) {
				System.out.println("Não encontrou o valor na cache L1: " + e.getMessage());
				
				if (e.getMessage().equals(L1Cache.NOT_FOUND)) {
					try {
						value = l2Cache.readFromCache(address, currentTime);
						
						instruction.setCacheEndTime(currentTime + l1Cache.getCycles() + l2Cache.getCycles());
						
						System.out.println("Encontrou o valor na cache L2.");
					} catch (Exception e1) {
						System.out.println("Não encontrou o valor na cache L2: " + e1.getMessage());
						
						if (e1.getMessage().equals(L2Cache.NOT_FOUND)) {
							try {
								value = l3Cache.readFromCache(address, currentTime);
								
								instruction.setCacheEndTime(currentTime + l1Cache.getCycles() + l2Cache.getCycles() + l3Cache.getCycles());
								
								System.out.println("Encontrou o valor na cache L3.");
							} catch (Exception e2) {
								// Não achou na Cache L3
								System.out.println("Não encontrou o valor na cache L3: " + e2.getMessage());
								
								if (e2.getMessage().equals(L3Cache.NOT_FOUND)) {
									value = l3Cache.readMiss(address, currentTime);
									
									instruction.setCacheEndTime(currentTime + l1Cache.getCycles() + l2Cache.getCycles() + l3Cache.getCycles() + Memory.getAccessTime());
								} else {
									throw e2;
								}
							}
						} else {
							throw e1;
						}
					}
				} else {
					throw e;
				}
			}
			return value;
		}
	}

	public static void write(int address, Object value, long currentTime, Instruction instruction, boolean readInstruction) throws Exception {
		l1Cache.writeToCache(address, value, currentTime, instruction, readInstruction);
	}

	public static void main(String[] args) throws Exception {
		L1Cache nc = new L1Cache(L1Cache.WRITE_BACK, 10, 256, 32, 2);
		L2Cache nc2 = new L2Cache(L1Cache.WRITE_BACK, 10, 256, 64, 2);
		L3Cache nc3 = new L3Cache(L1Cache.WRITE_BACK, 10, 512, 128, 2);
		
		Memory mem = new Memory(1024, 100);
		
		nc3.setL1(nc);
		nc3.setL2(nc2);
		nc2.setL3(nc3);
		
		nc2.setL1(nc);
		nc.setL2(nc2);
		nc.setL3(nc3);
		
		Cache c = new Cache(3, nc, nc2, nc3);
		
		Memory.store(0, 4);
		Memory.store(1, 6);
		Memory.store(2, 8);
		Memory.store(3, 9);
		Memory.store(4, 0);
		Memory.store(5, 3);
		Memory.store(6, 7);
		Memory.store(7, 6);
		Instruction s = new Instruction();

		Cache.write(24, 1999, 1, s, false);
		//System.out.println(nc3);
		//System.out.println(nc2);
		//System.out.println(nc);
		//System.out.println(mem);

		//System.out.println("*****************");
		System.out.println(nc3);
		System.out.println(nc2);
		System.out.println(nc);
		System.out.println(mem);
		
		c.toString();
	}
}
