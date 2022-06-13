import entries.*;

import java.util.*;
import java.io.*;

public class Assembler {
	String fileName = "P.txt";

	public Assembler(String file) {
		fileName = file;
	}

	public Assembler() {}

	public ArrayList<InstructionEntry> read() {
		ArrayList<InstructionEntry> instructionEntries = new ArrayList<InstructionEntry>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String s;
			s = br.readLine();
			Simulator.pc = Integer.parseInt(s);
			while ((s = br.readLine()) != null) {
				InstructionEntry inst = new InstructionEntry();
				StringTokenizer st = new StringTokenizer(s, " ,");

				inst.setType(InstructionType.valueOf(st.nextToken().toUpperCase()));

				if (st.hasMoreTokens())
					inst.setRD(Integer.parseInt(st.nextToken()));
				if (st.hasMoreTokens())
					inst.setRS(Integer.parseInt(st.nextToken()));
				if (st.hasMoreTokens())
					inst.setRT(Integer.parseInt(st.nextToken()));

				instructionEntries.add(inst);
			}
			
			br.close();
		} catch (IOException e) {
			System.out.println(e);
		}
		
		return instructionEntries;
	}

	public static void main(String[] args) throws IOException {
		Assembler a = new Assembler();
		a.read();
	}
}
