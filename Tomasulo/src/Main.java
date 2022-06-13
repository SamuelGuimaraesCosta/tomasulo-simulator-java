import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.StringTokenizer;

import memory.L1Cache;
import memory.L2Cache;
import memory.L3Cache;
import memory.MemoryWrapper;
import buffers.InstructionBuffer;
import buffers.ReorderBuffer;
import entries.InstructionEntry;
import entries.InstructionType;

public class Main {
	public static void main(String... args) throws Exception {
		Scanner sc = new Scanner(System.in);
		System.out.println("Welcome to Simulator!");
		System.out.println();

		Simulator.resvStations = new ArrayList<ReservationStation>();
		Simulator.instructionCycles = new HashMap<InstructionType, Integer>();
		
		System.out.print("Please Enter the number of Add RS: ");
		int count = sc.nextInt();
		System.out.print("Please Enter the time of Add RS: ");
		int time = sc.nextInt();
		Simulator.createRS(count, time, InstructionType.ADD);

		System.out.print("Please Enter the number of LOAD RS: ");
		count = sc.nextInt();
		System.out.print("Please Enter the time of LOAD RS: ");
		time = sc.nextInt();
		Simulator.createRS(count, time, InstructionType.LW);

		System.out.print("Please Enter the number of STORE RS: ");
		count = sc.nextInt();
		System.out.print("Please Enter the time of STORE RS: ");
		time = sc.nextInt();
		Simulator.createRS(count, time, InstructionType.SW);

		System.out.print("Please Enter the number of BRANCH RS: ");
		count = sc.nextInt();
		System.out.print("Please Enter the time of BRANCH RS: ");
		time = sc.nextInt();
		Simulator.createRS(count, time, InstructionType.BEQ);

		System.out.print("Please Enter the number of MUL RS: ");
		count = sc.nextInt();
		System.out.print("Please Enter the time of MUL RS: ");
		time = sc.nextInt();
		Simulator.createRS(count, time, InstructionType.MUL);

		System.out.print("please Enter the size of Reorder Buffer: ");
		Simulator.reorderBuffer = new ReorderBuffer(sc.nextInt());
		System.out.print("Please Enter the size of the instruction Buffer: ");
		Simulator.instructionBuffer = new InstructionBuffer(sc.nextInt());

		System.out.print("Please Enter number of cache levels: ");
		int levels = sc.nextInt();
		System.out.println("For cache level 1: ");
		System.out.print("Please Enter access time: ");
		int c = sc.nextInt();
		System.out.print("please Enter S: ");
		int S = sc.nextInt();
		System.out.print("please Enter l: ");
		int L = sc.nextInt();
		System.out.print("Please Enter m: ");
		int m = sc.nextInt();
		System.out.println("Please Choose write policy: ");
		System.out.print("1) Write Back  2) Write Through");
		int choice = sc.nextInt();
		L1Cache nc = new L1Cache((choice == 1) ? L1Cache.WRITE_BACK
				: L1Cache.WRITE_THROUGH, c, S, L, m);

		L2Cache nc2 = null;
		L3Cache nc3 = null;
		if (levels > 1) {
			System.out.println("For cache level 2: ");
			System.out.print("Please Enter access time: ");
			c = sc.nextInt();
			System.out.print("please Enter S: ");
			S = sc.nextInt();
			System.out.print("please Enter l: ");
			L = sc.nextInt();
			System.out.print("Please Enter m: ");
			m = sc.nextInt();
			System.out.println("Please Choose write policy: ");
			System.out.print("1) Write Back  2) Write Through");
			choice = sc.nextInt();
			nc2 = new L2Cache((choice == 1) ? L1Cache.WRITE_BACK : L1Cache.WRITE_THROUGH, c, S, L, m);
		}

		if (levels > 2) {
			System.out.println("For cache level 3: ");
			System.out.print("Please Enter access time: ");
			c = sc.nextInt();
			System.out.print("please Enter S: ");
			S = sc.nextInt();
			System.out.print("please Enter l: ");
			L = sc.nextInt();
			System.out.print("Please Enter m: ");
			m = sc.nextInt();
			System.out.println("Please Choose write policy: ");
			System.out.print("1) Write Back  2) Write Through");
			choice = sc.nextInt();
			nc3 = new L3Cache((choice == 1) ? L1Cache.WRITE_BACK : L1Cache.WRITE_THROUGH, c, S, L, m);
		}

		System.out.print("Please Enter main memory access time: ");
		int memorytime = sc.nextInt();

		Simulator.memory = new MemoryWrapper(memorytime, nc, nc2, nc3);
		
		sc.nextLine();
		//System.out.print("Please Enter the name of the program: ");
		//String file = sc.nextLine();
		Simulator.pc = 0;
		String file = "P.txt";
		Assembler assembler = new Assembler(file);
		ArrayList<InstructionEntry> instructionList = assembler.read();
		
		file = "Memory.txt";
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line= "";
		
		while((line = br.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(line);
			int add = Integer.parseInt(st.nextToken());
			int val = Integer.parseInt(st.nextToken());
			
			Simulator.memory.store(add, val);
		}
		
		Simulator.memory.loadInstructions(instructionList, Simulator.pc * 2);

		Simulator.cycle = 0;

		Simulator.regFile = new int[31]; // repeat for all arrays
		Simulator.regStatus = new int[31];
		for (int i = 0; i < 31; i++) {
			Simulator.regFile[i] = 0;
			Simulator.regStatus[i] = -1;
		}

		Simulator.programDone = false;
		Simulator.commitDone = false;
		Simulator.run();
		
		br.close();
		
		sc.close();
	}
}
