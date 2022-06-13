package memory;

import java.util.Arrays;

public class L3Cache {
	private String writePolicy;
	private int cycles;
	private int m, s, l;
	private int hits, misses;
	private CacheEntry[][] cache;
	private L1Cache l1;
	private L2Cache l2;
	private int memAddSize;
	private int disp;
	final static String NOT_FOUND = "not found l3";
	final static String WRITE_BACK = "Write Back";
	final static String WRITE_THROUGH = "Write Through";

	public L3Cache(String wp, int c, int s, int l, int m) {
		writePolicy = wp;
		setCycles(c);
		this.s = s;
		this.l = l;
		this.m = m;
		this.disp = (Log.log(l / 16));
		cache = new CacheEntry[s / l][l / 16];
		hits = misses = 0;
	}

	public Object readFromCache(int address, long currentTime) throws Exception {
		int baseAddress = (int) (address - address % Math.pow(2, disp));
		for (int i = 0; i < cache.length; i++) {
			if (cache[i] != null) {
				if (cache[i][0] != null && cache[i][0].check(baseAddress)) { // Found
					for (int j = 0; j < cache[i].length; j++) {
						cache[i][j].setTimeInCache(currentTime);
					}
					l2.updateFromL3Cache(address, baseAddress, cache[i],
							currentTime);
					hits++;
					return cache[i][address - baseAddress].getValue();
				}
			}
		}
		misses++;
		throw new Exception(NOT_FOUND, new Throwable());
	}

	public void updateValue(int address, Object value, long currentTime) {
		int baseAddress = (int) (address - address % Math.pow(2, disp)); // The
																			// base
																			// address
																			// is
																			// modified
																			// in
																			// the
																			// code
																			// below
		for (int i = 0; i < cache.length; i++) {
			if (cache[i][0] != null)
				if (cache[i][0].getAddress() == baseAddress) {
					int index = address - baseAddress;
					cache[i][index].setValue(value);
					cache[i][0].setTimeInCache(currentTime);
					cache[i][index].setTimeInCache(currentTime);
				}
		}
	}

	public Object readMiss(int address, long currentTime) {

		int baseAddress = (int) (address - address % Math.pow(2, disp)); // The
																			// base
																			// address
																			// is
																			// modified
																			// in
																			// the
																			// code
																			// below
		Object[] value = new Object[l / 16];
		for (int i = 0; i < value.length; i++)
			value[i] = Memory.load(baseAddress + i);
		int temp = baseAddress; // Stored in temp value to be used as the
								// original base address
		if (m == 1) {
			int disp = (Log.log(l / 16));
			baseAddress = (int) (baseAddress / Math.pow(2, disp));
			int index = baseAddress % (s / l);
			if (writePolicy.equals(WRITE_BACK)) {
				if (cache[index][0] != null) {
					if (cache[index][0].isDirty()) {
						for (int i = 0; i < s / l; i++) {
							Memory.store(cache[index][i].getAddress(),
									cache[index][i].getValue());
						}
					}
				}
			}
			addToCache(index, temp, value, currentTime);
			l2.updateFromL3Cache(address, temp, cache[index], currentTime);
			return value[address - temp];
		} else if (m == s / l) {
			for (int i = 0; i < cache.length; i++) {
				if (cache[i][0] == null) {
					addToCache(i, temp, value, currentTime);
					l2.updateFromL3Cache(address, temp, cache[i], currentTime);
					return value[address - temp];
				}
			}
			int index = LRU();
			if (writePolicy.equals(WRITE_BACK)) {
				if (cache[index][0].isDirty()) {
					for (int i = 0; i < s / l; i++) {
						Memory.store(cache[index][i].getAddress(),
								cache[index][i].getValue());
					}
				}
			}
			addToCache(index, temp, value, currentTime);
			l2.updateFromL3Cache(address, temp, cache[index], currentTime);
			return value[address - temp];
		} else {
			baseAddress = (int) (baseAddress / Math.pow(2, disp));
			int index = baseAddress % (s / l / m);
			for (; index < cache.length; index += (s / l / m)) {
				if (cache[index][0] == null) {
					addToCache(index, temp, value, currentTime);
					l2.updateFromL3Cache(address, temp, cache[index],
							currentTime);
					return value[address - temp];
				}
			}
			index = baseAddress % (s / l / m);
			int newIndex = setAssociativeLRU(index);
			if (writePolicy.equals(WRITE_BACK)) {
				if (cache[newIndex][0].isDirty()) {
					for (int i = 0; i < s / l; i++) {
						Memory.store(cache[newIndex][i].getAddress(),
								cache[newIndex][i].getValue());
					}
				}
			}
			addToCache(newIndex, temp, value, currentTime);
			l2.updateFromL3Cache(address, temp, cache[newIndex], currentTime);
			return value[address - temp];
		}
	}

	private int setAssociativeLRU(int index) {
		long minSoFar = cache[index][0].getTimeInCache();
		int newIndex = index;
		for (; index < cache.length; index += (s / l / m)) {
			if (cache[index][0].getTimeInCache() < minSoFar) {
				minSoFar = cache[index][0].getTimeInCache();
				newIndex = index;
			}
		}
		return newIndex;
	}

	private void addToCache(int index, int baseAddress, Object[] value,
			long currentTime) {
		int size = l / 16;
		// cache[index] = new CacheEntry[size];
		for (int i = 0; i < size; i++) {
			cache[index][i] = new CacheEntry(baseAddress + i, value[i],
					currentTime);
		}
	}

	private int LRU() {
		Arrays.sort(cache);
		return 0;
	}

	public int getCycles() {
		return cycles;
	}

	public void setCycles(int cycles) {
		this.cycles = cycles;
	}

	public L1Cache getL1() {
		return l1;
	}

	public void setL1(L1Cache l1) {
		this.l1 = l1;
	}

	public int getMemAddSize() {
		return memAddSize;
	}

	public void setMemAddSize(int memAddSize) {
		this.memAddSize = memAddSize;
	}

	public L2Cache getL2() {
		return l2;
	}

	public void setL2(L2Cache l2) {
		this.l2 = l2;
	}

	public double getHitRatio() {
		return (double) hits / (double) (hits + misses);
	}
	
	public String toString() {
		String x = "";
		for (int i = 0; i < cache.length; i++) {
			for (int j = 0; j < cache[i].length && cache[i][j] != null; j++) {
				x += "Index: " + i + " " + cache[i][j].toString();
			}
			if (cache[i][0] != null)
				x += "\n";
		}
		return x;
	}
}