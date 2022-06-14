import java.util.*;

import memory.*;
import entries.*;
import buffers.*;

public class Simulator {
	static boolean DEBUG = true;
	
	static int cycle; // Ciclo atual
	static int pc;

	static int[] regFile;
	static int[] regStatus;

	static InstructionBuffer instructionBuffer;
	static ReorderBuffer reorderBuffer;

	static ArrayList<ReservationStation> resvStations;

	static MemoryWrapper memory;

	static HashMap<InstructionType, Integer> instructionCycles;
	// N�mero de ciclos que uma instru��o demora para executar

	static boolean programDone; // Indica a pseudo sa�da
								// Instru��o j� foi buscada
	static boolean commitDone;  // Indica a �ltima instru��o j� foi commitada

	static void initializeDefault() {
		Simulator.pc = 0;
		
		Assembler assembler = new Assembler();
		
		ArrayList<InstructionEntry> instructionList = assembler.read();

		cycle = 0;

		regFile = new int[31];
		regStatus = new int[31];
		
		for (int i = 0; i < 31; i++) {
			regFile[i] = 0;
			regStatus[i] = -1;
		}

		instructionBuffer = new InstructionBuffer(256);
		reorderBuffer = new ReorderBuffer(256);

		resvStations = new ArrayList<ReservationStation>();
		resvStations.add(new ReservationStation(InstructionType.ADD));
		resvStations.add(new ReservationStation(InstructionType.ADDI));
		resvStations.add(new ReservationStation(InstructionType.MUL));
		resvStations.add(new ReservationStation(InstructionType.BEQ));
		resvStations.add(new ReservationStation(InstructionType.SW));
		resvStations.add(new ReservationStation(InstructionType.LW));
		
		System.out.println("Buscando instru��es na esta��o de reserva...\n");
		
		if(resvStations.size() > 0) {
			resvStations.forEach(rss->System.out.println(rss.getType()));
			
			System.out.println("\nInstru��es encontradas na esta��o de reseva com sucesso!\n");
		} else {
			System.out.println("N�o h� instru��es alocadas na esta��o de reserva!\n");
		}
		
		System.out.println("Mem�ria iniciada...\n");

		memory = new MemoryWrapper();
		memory.loadInstructions(instructionList, pc);
		
		System.out.println("\nInstru��es alocadas na mem�ria com sucesso!\n");
		
		System.out.println("Configura��o dos ciclos de instru��es iniciado...\n");

		instructionCycles = new HashMap<InstructionType, Integer>();
		instructionCycles.put(InstructionType.ADD, 2);
		instructionCycles.put(InstructionType.BEQ, 1);
		instructionCycles.put(InstructionType.LW, 5);
		instructionCycles.put(InstructionType.MUL, 3);
		instructionCycles.put(InstructionType.SW, 5);
		instructionCycles.put(InstructionType.ADDI, 2);
		
		System.out.println("Ciclos de instru��es configurado com sucesso!");

		programDone = false;
		commitDone = false;
		
		/*String file = "Memory.txt";
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line= "";
	
			while((line = br.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line);
				
				int add = Integer.parseInt(st.nextToken());
				int val = Integer.parseInt(st.nextToken());
				
				Simulator.memory.store(add, val);
			}
			
			br.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}*/
	}

	static boolean isMemory(InstructionType type) {
		return type == InstructionType.LW || type == InstructionType.SW;
	}

	static boolean writesToReg(InstructionType type) {
		return type == InstructionType.LW || type == InstructionType.ADD || type == InstructionType.SUB || type == InstructionType.ADDI || type == InstructionType.NAND || type == InstructionType.MUL;
	}

	static ReservationStation getEmptyRS(InstructionType type) {
		for (int i = 0; i < resvStations.size(); i++) {
			ReservationStation entry = resvStations.get(i);
			
			if (entry.busy == false && entry.type == getFunctionalUnit(type))
				return entry;
		}
		return null;
	}

	static InstructionType getFunctionalUnit(InstructionType type) {
		switch (type) {
		case ADD:
			return InstructionType.ADD;
		case ADDI:
			return InstructionType.ADDI;
		case SUB:
			return InstructionType.SUB;
		case NAND:
			return InstructionType.NAND;
		case MUL:
			return InstructionType.MUL;
		default:
			return type;
		}
	}

	static void commit() {
		if (reorderBuffer.isEmpty()) {
			if (programDone) {
				commitDone = true;
			}
				
			return; // Buffer vazio
		}

		RobEntry entry = (RobEntry) reorderBuffer.getFirst();
		
		if(!entry.isReady()) return;
		
		System.out.println("\nRealizado commit da instru��o: " + entry.getType() + ". Ciclo: " + cycle + ".");
		
		//if(DEBUG)
		//	System.out.println(entry);
		
		switch (entry.getType()) {
			case SW:
				if (memory.writeData(entry.getDest(), entry.getVal(), cycle))
					reorderBuffer.moveHead();
				break;
			case BEQ:
				if ((entry.getVal() > 0 && entry.isBranchTaken()) || (entry.getVal() < 0 && !entry.isBranchTaken())) {
					reorderBuffer.flush();
					instructionBuffer.flush();
	
					for (ReservationStation rs : resvStations)
						rs.clear();
					for (int i = 0; i < regStatus.length; i++)
						regStatus[i] = -1;
	
					pc = (entry.isBranchTaken()) ? entry.getDest() + entry.getVal()
							: entry.getDest();
					programDone = false;
				} else {
					reorderBuffer.moveHead();
				}
				
				break;
			default:
				regFile[entry.getDest()] = entry.getVal();
				reorderBuffer.moveHead();
		}
	}

	static void write() {
		for (int i = 0; i < resvStations.size(); i++) {
			ReservationStation rs = resvStations.get(i);
			
			if (rs.busy && rs.stage == Stage.EXECUTE && rs.remainingCycles <= 0) {
				// Atualizando o Buffer de Reordenamento

				Integer result = rs.run(); // Valor da Unidade Funcional
				
				if (result == null)
					return;

				rs.stage = Stage.WRITE;
				
				System.out.println("\nEcrevendo instru��o: " + rs.getType() + ", com est�gio de execu��o. Ciclo: " + cycle + ".\n");
				
				// Atualizando a entrada do buffer de reordenamento
				RobEntry robEntry = (RobEntry) reorderBuffer.get(rs.getRob());
				
				robEntry.setReady();
				robEntry.setValue(result);
				
				System.out.println(robEntry + ", Est�gio: " + rs.stage);

				if (writesToReg(rs.getType()) && regStatus[robEntry.getDest()] == rs.getRob()) {
					regStatus[robEntry.getDest()] = -1;
				}

				// Atualizando as esta��es de reservas de registradores
				for (int j = 0; j < resvStations.size(); j++) {
					ReservationStation resvStation = resvStations.get(j);
					
					if (rs.getRob() == resvStation.qj) {
						resvStation.qj = -1;
						resvStation.vj = result;
					}
					if (rs.getRob() == resvStation.qk) {
						resvStation.qk = -1;
						resvStation.vk = result;
					}
				}
				
				rs.busy = false;
				rs.clear();
			}
		}
	}

	static void execute() {
		for (int i = 0; i < resvStations.size(); i++) {
			ReservationStation entry = resvStations.get(i);
			
			if (entry.busy == false)
				continue;

			if (entry.stage == Stage.ISSUE && entry.qj == -1 && entry.qk == -1) {
				entry.stage = Stage.EXECUTE;
				entry.address = entry.vj + entry.address;
				
				System.out.println("\nExecutando instru��o: " + entry.getType() + ", com est�gio de issue(decodifica��o). Ciclo: " + cycle + ".");
			} else if (entry.stage == Stage.EXECUTE && entry.remainingCycles > 0)
				entry.remainingCycles--;
		}
	}

	static void issue() {
		if (instructionBuffer.isEmpty() || reorderBuffer.isFull())
			return;

		InstructionEntry inst = (InstructionEntry) instructionBuffer.getFirst();
		ReservationStation rs = getEmptyRS(inst.getType());

		if (rs != null) {
			// Issue(decodifica��o), preenche Buffer de reordenamento, Esta��o de Reserva e tabela de registradores
			instructionBuffer.moveHead();

			if (regStatus[inst.getRS()] != -1) {
				rs.qj = regStatus[inst.getRS()];
				rs.vj = 0;
			} else {
				// Pronto no buffer de reordenamento (est� ecrito por�m n�o commitado)
				int testRob = reorderBuffer.findDest(inst.getRS());
				
				if (testRob != -1) {
					rs.vj = testRob;
				} else {
					rs.vj = regFile[inst.getRS()];
				}
				
				rs.qj = -1;
				
				System.out.println("\nInstru��o: " + inst.getType() + " pronta no Buffer de Reordenamento, j� escrita por�m n�o commitada. Ciclo: " + cycle + ".");
			}

			switch (inst.getType()) {
				case ADD:
				case SUB:
				case MUL:
				case NAND:
					System.out.println("\nInstru��o: " + inst.getType() + " em decodifica��o, com est�gio de busca. Ciclo: " + cycle + ".");
					
					if (regStatus[inst.getRT()] != -1) {
						rs.qk = regStatus[inst.getRT()];
						rs.vk = 0;
					} else {
						int testRob = reorderBuffer.findDest(inst.getRT());
						
						if (testRob != -1) {
							rs.vk = testRob;
						} else {
							rs.vk = regFile[inst.getRT()];
						}
						
						rs.qk = -1;
					}
				
					break;
				case BEQ:
				case SW:
					if (regStatus[inst.getRD()] != -1) {
						rs.qk = regStatus[inst.getRD()];
						rs.vk = 0;
					} else {
						int testRob = reorderBuffer.findDest(inst.getRD());
						
						if (testRob != -1) {
							rs.vk = testRob;
						} else {
							rs.vk = regFile[inst.getRD()];
						}
						
						rs.qk = -1;
					}
					
					rs.address = inst.getRT();
					
					break;
				default:
					rs.qk = -1;
					rs.vk = -1;
					rs.address = inst.getRT();
			}

			rs.setRob(reorderBuffer.tailIndex());
			
			if (instructionCycles.containsKey(rs.getType())) {
				rs.setCycles(instructionCycles.get(rs.getType()));
			} else {
				rs.setCycles(1);
			}
			
			rs.setBusy(true);
			rs.setStage(Stage.ISSUE);
			rs.setOperation(inst.getType());

			int destination = inst.getRD();

			if (writesToReg(rs.getType())) {
				regStatus[destination] = reorderBuffer.tailIndex();
			} else if (inst.getType() == InstructionType.BEQ) {
				destination = inst.getInstAddress();
				//Coloca o PC original de volta em seu lugar
			} else {
				destination = -1;
			}

			RobEntry robEntry = new RobEntry(destination, rs.getType());
			
			reorderBuffer.add(robEntry);
		}
	}

	static void fetch() {
		if (programDone)
			return;

		InstructionEntry inst = null;
		
		try {
			inst = (InstructionEntry) memory.readInstruction(pc * 2, cycle);
			
			System.out.println("\nLeitura da instru��o: " + inst.getType() + " a partir da mem�ria(Ciclo: " + cycle + ", PC: " + pc * 2 + ").");
		} catch(NullPointerException e) {
			//e.printStackTrace();
			return;
		}
				
		if (inst!= null && !instructionBuffer.isFull()) {
			System.out.println("\nInstru��o Encontrada: " + inst.getType() + ". Ciclo: " + cycle + ".");

			switch (inst.getType()) {
				case JMP: {
					pc += 1 + regFile[inst.getRD()] + inst.getRS();
					
					break;
				}
				case BEQ: {
					inst.setPcAddress(pc + 1);
					
					pc += (inst.getRT() >= 0) ? 1 : inst.getRT() + 1;
					
					instructionBuffer.add(inst);
					
					break;
				}
				case JALR: {
					pc += regFile[inst.getRS()];
					
					regFile[inst.getRD()] = pc + 1;
					
					break;
				}
				case RET: {
					pc = regFile[inst.getRD()];
					
					break;
				}
				case END:
					programDone = true;
				default:
					pc += 1;
					
					instructionBuffer.add(inst);
					
					break;
			}
		}
	}

	public static void createRS(int count, int time, InstructionType type) {
		for (int i = count; i > 0; i--)
			resvStations.add(new ReservationStation(type));
		instructionCycles.put(type, time);
	}

	public static void run() {
		while (!commitDone) {
			commit();
			write();
			execute();
			issue();
			fetch();
			
			cycle++;
			
			//System.out.println("\nCiclo Atual: " + cycle);
		}
		
		System.out.println("\nSimula��o finalizada, resultados:");
		System.out.println("\nCiclos totais: " + cycle);
		System.out.println("IPC: "  + (pc - 1) / (double)cycle);
		System.out.println("Hit/Ratio Cache N�vel 1: " + memory.getL1CacheR());
		System.out.println("Hit/Ratio Cache N�vel 2: " + memory.getL2CacheR());
		System.out.println("Hit/Ratio Cache N�vel 3: " + memory.getL3CacheR());
		System.out.println("Valores dos registradores: ");
		
		System.out.print("Posi��o: \n[ ");
		for (int i = 0; i < regFile.length; i++)
			System.out.print(i + 1 + " ");
		System.out.print("]");
		
		System.out.print("\nValor:  \n[ ");
		for (int i = 0; i < regFile.length; i++)
			System.out.print(regFile[i] + " ");
		System.out.print("]");
		
		/*System.out.print("\nStatus:  [ ");
		for (int i = 0; i < regStatus.length; i++)
			System.out.print(regStatus[i] + " ");
		System.out.print("]");*/
	}

	public static void main(String... args) {
		System.out.println("Simulador iniciado!\n");
		
		initializeDefault();
		
		run();
	}
}
