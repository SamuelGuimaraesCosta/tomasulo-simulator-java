package memory;

public class Memory {
	public static Object[] mem;
	private static int accessTime;

	public Memory(int size, int access) {
		mem = new Object[size / 16];
		accessTime = access;
	}

	public static Object load(int address) {
		return mem[address];
	}

	public static void store(int address, Object value) {
		mem[address] = value;
	}

	public static int getAccessTime() {
		return accessTime;
	}

	public String toString() {
		String s = "";
		for (int i = 0; i < mem.length; i++) {
			s += "Memória[" + i + "] :" + mem[i] + "   ";
			if (i % 10 == 0 && i != 0)
				s += "\n";
		}
		return s;
	}
}
