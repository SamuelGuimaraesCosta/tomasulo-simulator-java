package memory;

import java.util.Arrays;

public class L2Cache {
	private String writePolicy;
	private int cycles;
	private int m, s, l;
	private int hits, misses;
	private static CacheEntry[][] cache;
	private L1Cache l1;
	private L3Cache l3;
	private int memAddSize;
	private int disp;
	final static String NOT_FOUND = "not found l2";
	final static String WRITE_BACK = "Write Back";
	final static String WRITE_THROUGH = "Write Through";

	public L2Cache(String wp, int c, int s, int l, int m) {
		writePolicy = wp;
		cycles = c;
		this.s = s;
		this.l = l;
		this.m = m;
		this.disp = (Log.log(l / 16));
		cache = new CacheEntry[s / l][l / 16];
		hits = misses = 0;
	}

	// In case of read hit, get the value and update L1 using the values from L2
	public Object readFromCache(int address, long currentTime) throws Exception {
		int baseAddress = (int) (address - address % Math.pow(2, disp));
		for (int i = 0; i < cache.length; i++) {
			if (cache[i] != null) {
				if (cache[i][0] != null && cache[i][0].check(baseAddress)) { // Found
					for (int j = 0; j < cache[i].length; j++) {
						cache[i][j].setTimeInCache(currentTime);
					}
					l1.updateFromL2Cache(address, baseAddress, cache[i],
							currentTime);
					hits++;
					return cache[i][address - baseAddress].getValue();
				}
			}
		}
		misses++;
		throw new Exception(NOT_FOUND, new Throwable());
	}

	// Read miss of L2 cache, same as L1 cache. Called only if there's no L3
	// cache.
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
		Object[] value = new Object[s / l];
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
			l1.updateFromL2Cache(address, baseAddress, cache[index],
					currentTime);
			return value[address - temp];
		} else if (m == s / l) {
			for (int i = 0; i < cache.length; i++) {
				if (cache[i][0] == null) {
					addToCache(i, temp, value, currentTime);
					l1.updateFromL2Cache(address, baseAddress, cache[i],
							currentTime);
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
			l1.updateFromL2Cache(address, baseAddress, cache[index],
					currentTime);
			return value[address - temp];
		} else {
			baseAddress = (int) (baseAddress / Math.pow(2, disp));
			int index = baseAddress % (s / l / m);
			for (; index < cache.length; index += (s / l / m)) {
				if (cache[index][0] == null) {
					addToCache(index, temp, value, currentTime);
					l1.updateFromL2Cache(address, baseAddress, cache[index],
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
			l1.updateFromL2Cache(address, baseAddress, cache[newIndex],
					currentTime);
			return value[address - temp];
		}

	}

	private void addToCache(int index, int baseAddress, Object[] value,
			long currentTime) {
		int size = l / 16;
		cache[index] = new CacheEntry[size];
		for (int i = 0; i < size; i++) {
			cache[index][i] = new CacheEntry(baseAddress + i, value[i],
					currentTime);
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

	private int LRU() {
		Arrays.sort(cache);
		return 0;
	}

	public void updateFromL3Cache(int address, int baseAddress2,
			CacheEntry[] values, long currentTime) {
		// TODO handle dirty bits with all below levels of cache.
		int baseAddress = (int) (address - address % Math.pow(2, disp)); // The
																			// base
																			// address
																			// is
																			// modified
																			// in
																			// the
																			// code
																			// below
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
							updateMemoryAndCaches(index, i, currentTime);
						}
					}
				}
			}

			int deltaAddress = temp - baseAddress2;
			Object[] value = new Object[(l / 16)];
			for (int i = deltaAddress; i < deltaAddress + (l / 16); i++) {
				value[i - deltaAddress] = values[i].getValue();
			}
			addToCache(index, temp, value, currentTime);
			l1.updateFromL2Cache(address, baseAddress2, values, currentTime);
			return;
			// return value[address-temp];
		} else if (m == s / l) {
			for (int i = 0; i < cache.length; i++) {
				if (cache[i][0] == null) {
					int deltaAddress = temp - baseAddress2;
					Object[] value = new Object[(l / 16)];
					for (int j = deltaAddress; j < deltaAddress + (l / 16); j++) {
						value[j - deltaAddress] = values[j].getValue();
					}
					addToCache(i, temp, value, currentTime);
					l1.updateFromL2Cache(address, baseAddress2, values,
							currentTime);
					return;
					// return value[address-temp];
				}
			}
			int index = LRU();
			if (writePolicy.equals(WRITE_BACK)) {
				if (cache[index][0].isDirty()) {
					for (int i = 0; i < s / l; i++) {
						updateMemoryAndCaches(index, i, currentTime);
					}
				}
			}
			int deltaAddress = temp - baseAddress2;
			Object[] value = new Object[(l / 16)];
			for (int j = deltaAddress; j < deltaAddress + (l / 16); j++) {
				value[j - deltaAddress] = values[j].getValue();
			}
			addToCache(index, temp, value, currentTime);
			l1.updateFromL2Cache(address, baseAddress2, values, currentTime);
			return;
			// return value[address-temp];
		} else {
			baseAddress = (int) (baseAddress / Math.pow(2, disp));
			int index = baseAddress % (s / l / m);
			for (; index < cache.length; index += (s / l / m)) {
				if (cache[index][0] == null) {
					int deltaAddress = temp - baseAddress2;
					Object[] value = new Object[(l / 16)];
					for (int j = deltaAddress; j < (deltaAddress + (l / 16)); j++) {
						value[j - deltaAddress] = values[j].getValue();
					}
					addToCache(index, temp, value, currentTime);
					l1.updateFromL2Cache(address, baseAddress2, values,
							currentTime);
					return;
					// return value[address-temp];
				}
			}
			index = baseAddress % (s / l / m);
			int newIndex = setAssociativeLRU(index);
			if (writePolicy.equals(WRITE_BACK)) {
				if (cache[newIndex][0].isDirty()) {
					for (int i = 0; i < s / l; i++) {
						updateMemoryAndCaches(newIndex, i, currentTime);
					}
				}
			}
			int deltaAddress = temp - baseAddress2;
			Object[] value = new Object[(l / 16)];
			for (int j = deltaAddress; j < deltaAddress + (l / 16); j++) {
				value[j - deltaAddress] = values[deltaAddress].getValue();
			}
			addToCache(newIndex, temp, value, currentTime);
			l1.updateFromL2Cache(address, baseAddress2, values, currentTime);
			return;
			// return value[address-temp];
		}

	}

	private void updateMemoryAndCaches(int cacheLine, int indexInCacheLine,
			long currentTime) {
		int address = cache[cacheLine][indexInCacheLine].getAddress();
		Object value = cache[cacheLine][indexInCacheLine].getValue();
		Memory.store(address, value);
		if (l3 != null) {
			l3.updateValue(address, value, currentTime);
		}
	}

	public String getWritePolicy() {
		return writePolicy;
	}

	public void setWritePolicy(String writePolicy) {
		this.writePolicy = writePolicy;
	}

	public int getCycles() {
		return cycles;
	}

	public void setCycles(int cycles) {
		this.cycles = cycles;
	}

	public int getM() {
		return m;
	}

	public void setM(int m) {
		this.m = m;
	}

	public int getS() {
		return s;
	}

	public void setS(int s) {
		this.s = s;
	}

	public int getL() {
		return l;
	}

	public void setL(int l) {
		this.l = l;
	}

	public L3Cache getL3() {
		return l3;
	}

	public void setL3(L3Cache l3) {
		this.l3 = l3;
	}

	public int getMemAddSize() {
		return memAddSize;
	}

	public void setMemAddSize(int memAddSize) {
		this.memAddSize = memAddSize;
	}

	public int getDisp() {
		return disp;
	}

	public void setDisp(int disp) {
		this.disp = disp;
	}

	public L1Cache getL1() {
		return l1;
	}

	public void setL1(L1Cache l1) {
		this.l1 = l1;
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
