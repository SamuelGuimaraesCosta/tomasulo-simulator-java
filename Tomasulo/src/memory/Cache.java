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
		
		System.out.println("\nIniciando busca de instru��o na cache. Ciclo: " + currentTime + ".");

		/*
			Se h� somente cache de n�vel 1 (encontrado) retornar o valor e definir o hor�rio de t�rmino
			para a hora atual + tempo de acesso do cache l1, caso contr�rio, chame read miss e set
			na hora de t�rmino para a hora atual + hora de acesso do cache l1 + acesso mem tempo
		*/
		
		if (level == 1) {
			try {
				value = l1Cache.readFromCache(address, currentTime, readInstruction);
				
				instruction.setCacheEndTime(currentTime + l1Cache.getCycles());
				
				System.out.println("\nInstru��o encontrada na cache L1.");
				System.out.println("Ciclos gastos na busca da instru��o: " + instruction.getCacheEndTime() + ".");
			} catch (Exception e) {
				// N�o achou na Cache L1
				System.out.println("\nInstru��o n�o encontrada na cache L1.");
				
				if (e.getMessage().equals(L1Cache.NOT_FOUND)) {
					value = l1Cache.readMiss(address, currentTime);
					
					instruction.setCacheEndTime(currentTime + l1Cache.getCycles() + Memory.getAccessTime());
					
					System.out.println("Ciclos gastos procurando em L1 + Mem�ria: " + instruction.getCacheEndTime() + ".");
				} else {
					throw e;
				}
			}
			
			System.out.println("\nFinalizando busca de instru��o na cache. Ciclo: " + instruction.getCacheEndTime() + ".");
			System.out.println("\n" + instruction.toString());
			
			return value;
		} else if (level == 2) {//Se h� dois n�veis de cache, tenta em L1, se achou retorna se n�o tenta L2, se n�o achou em L2, ele d� miss.
			try {
				value = l1Cache.readFromCache(address, currentTime, readInstruction);
				
				instruction.setCacheEndTime(currentTime + l1Cache.getCycles());
				
				System.out.println("\nInstru��o encontrada na cache L1.");
				System.out.println("Ciclos gastos na busca da instru��o: " + instruction.getCacheEndTime() + ".");
			} catch (Exception e) {
				System.out.println("\nInstru��o n�o encontrada na cache L1, procurando na cache L2...");
				System.out.println("Ciclos gastos procurando em L1: " + (currentTime + l1Cache.getCycles()) + ".");
				
				if (e.getMessage().equals(L1Cache.NOT_FOUND)) {
					try {
						value = l2Cache.readFromCache(address, currentTime);
						
						instruction.setCacheEndTime(currentTime + l1Cache.getCycles() + l2Cache.getCycles());
						
						System.out.println("\nInstru��o encontrada na cache L2.");
						System.out.println("Ciclos gastos na busca da instru��o: " + instruction.getCacheEndTime() + ".");
					} catch (Exception e1) {
						// N�o achou na Cache L2
						System.out.println("\nInstru��o n�o encontrada na cache L2, procurando na mem�ria...");
						
						if (e1.getMessage().equals(L2Cache.NOT_FOUND)) {
							value = l2Cache.readMiss(address, currentTime);
							
							instruction.setCacheEndTime(currentTime + l1Cache.getCycles() + l2Cache.getCycles() + Memory.getAccessTime());
							
							System.out.println("Ciclos gastos procurando em L1 + L2 + Mem�ria: " + instruction.getCacheEndTime() + ".");
						} else {
							throw e1;
						}
					}
				} else {
					throw e;
				}
			}

			System.out.println("\nFinalizando busca de instru��o na cache. Ciclo: " + instruction.getCacheEndTime() + ".");
			System.out.println("\n" + instruction.toString());
			
			return value;
		} else { //Se h� 3 n�veis de cache, primeiro se procura na L1, depois na L2 e se n�o encontrou na L3, caso contr�rio h� miss e o tempo gasto ser� maior.
			try {
				value = l1Cache.readFromCache(address, currentTime, readInstruction);
				
				instruction.setCacheEndTime(currentTime + l1Cache.getCycles());
				
				System.out.println("\nInstru��o encontrada na cache L1.");
				System.out.println("Ciclos gastos na busca da instru��o: " + instruction.getCacheEndTime() + ".");
			} catch (Exception e) {
				System.out.println("\nInstru��o n�o encontrada na cache L1, procurando na cache L2...");
				System.out.println("Ciclos gastos procurando em L1: " + (currentTime + l1Cache.getCycles()) + ".");
				
				if (e.getMessage().equals(L1Cache.NOT_FOUND)) {
					try {
						value = l2Cache.readFromCache(address, currentTime);
						
						instruction.setCacheEndTime(currentTime + l1Cache.getCycles() + l2Cache.getCycles());
						
						System.out.println("\nInstru��o encontrada na cache L2.");
						System.out.println("Ciclos gastos na busca da instru��o: " + instruction.getCacheEndTime() + ".");
					} catch (Exception e1) {
						System.out.println("\nInstru��o n�o encontrada na cache L2, procurando na cache L3...");
						System.out.println("Ciclos gastos procurando em L2 + L1: " + (currentTime + l1Cache.getCycles() + l2Cache.getCycles()) + ".");
						
						if (e1.getMessage().equals(L2Cache.NOT_FOUND)) {
							try {
								value = l3Cache.readFromCache(address, currentTime);
								
								instruction.setCacheEndTime(currentTime + l1Cache.getCycles() + l2Cache.getCycles() + l3Cache.getCycles());
								
								System.out.println("\nInstru��o encontrada na cache L3.");
								System.out.println("Ciclos gastos na busca da instru��o: " + instruction.getCacheEndTime() + ".");
							} catch (Exception e2) {
								// N�o achou na Cache L3
								System.out.println("\nInstru��o n�o encontrada na cache L3, procurando na mem�ria...");
								
								if (e2.getMessage().equals(L3Cache.NOT_FOUND)) {
									value = l3Cache.readMiss(address, currentTime);
									
									instruction.setCacheEndTime(currentTime + l1Cache.getCycles() + l2Cache.getCycles() + l3Cache.getCycles() + Memory.getAccessTime());
									
									System.out.println("Ciclos gastos procurando em L1 + L2 + L3 + Mem�ria: " + instruction.getCacheEndTime() + ".");
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

			System.out.println("\nFinalizando busca de instru��o na cache. Ciclo: " + instruction.getCacheEndTime() + ".");
			System.out.println("\n" + instruction.toString());
			
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
