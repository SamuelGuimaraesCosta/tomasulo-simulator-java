package buffers;

import entries.InstructionType;
import entries.RobEntry;

public class ReorderBuffer extends CircularBuffer {

	public ReorderBuffer(int size) {
		super();
		
		buffer = new RobEntry[size];
	}

	public int findDest(int reg) {
		boolean stop = false;
		int i = (tail + buffer.length - 1) % buffer.length;

		while (!stop) {
			if (i == head)
				stop = true;
			RobEntry robEntry = (RobEntry) buffer[i];
			
			if (robEntry!= null && robEntry.getDest() == reg && robEntry.isReady() && robEntry.getType() != InstructionType.BEQ && robEntry.getType() != InstructionType.SW) {
				return robEntry.getVal();
			}
			
			i = (i + buffer.length - 1) % buffer.length;
		}
		return -1;
	}
}
