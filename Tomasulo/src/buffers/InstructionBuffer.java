package buffers;

import entries.InstructionEntry;

public class InstructionBuffer extends CircularBuffer {
	
	public InstructionBuffer(int size) {
		super();
		buffer = new InstructionEntry[size];
	}
}
