package memory;

import java.util.Arrays;

public class L1Cache {
	private String writePolicy;
	private int cycles;
	private int m, s, l;
	private int hits, misses;
	private static CacheEntry[][] instructionsCache;
	private static CacheEntry[][] dataCache;
	private L2Cache l2;
	private L3Cache l3;
	private int memAddSize;
	private int disp;
	private static CacheEntry[][] cache;
	final static String NOT_FOUND = "not found l1";
	public final static String WRITE_BACK = "Write Back";
	public final static String WRITE_THROUGH = "Write Through";

	public L1Cache(String wp, int c, int s, int l, int m) {
		writePolicy = wp;
		cycles = c;
		this.s = s;
		this.l = l;
		this.m = m;
		this.disp = (Log.log(l / 16));
		instructionsCache = new CacheEntry[s / l][l / 16];
		dataCache = new CacheEntry[s / l][l / 16];
		cache = new CacheEntry[s / l][l / 16];
		hits = misses = 0;
	}

	public Object readFromCache(int address, long currentTime, boolean readInstruction) throws Exception {
		int baseAdress = (int) (address - address % Math.pow(2, disp));
		
		if (readInstruction) {
			// Leitura a partir das instruções na Cache
			for (int i = 0; i < instructionsCache.length; i++) {
				if (instructionsCache[i] != null) {
					if (instructionsCache[i][0] != null && instructionsCache[i][0].check(baseAdress)) { // Achou na Cache
						for (int j = 0; j < instructionsCache[i].length; j++) {
							instructionsCache[i][j].setTimeInCache(currentTime);
						}
						hits++;
						return instructionsCache[i][address - baseAdress].getValue();
					}
				}
			}
			misses++;
		} else {
			// read from dataCache
			for (int i = 0; i < dataCache.length; i++) {
				if (dataCache[i] != null) {
					if (dataCache[i][0] != null
							&& dataCache[i][0].check(baseAdress)) { // Found
						for (int j = 0; j < dataCache[i].length; j++) {
							dataCache[i][j].setTimeInCache(currentTime);
						}
						hits++;
						return dataCache[i][address - baseAdress].getValue();
					}
				}
			}
			misses++;
		}
		throw new Exception(NOT_FOUND, new Throwable());
	}

	public Object readMiss(int address, long currentTime) {
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
			return value[address - temp];
		} else if (m == s / l) {
			for (int i = 0; i < cache.length; i++) {
				if (cache[i][0] == null) {
					addToCache(i, temp, value, currentTime);
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
			return value[address - temp];
		} else {
			baseAddress = (int) (baseAddress / Math.pow(2, disp));
			int index = baseAddress % (s / l / m);
			for (; index < cache.length; index += (s / l / m)) {
				if (cache[index][0] == null) {
					addToCache(index, temp, value, currentTime);
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
			return value[address - temp];
		}

	}

	private void writeBack(int address, Object value, long currentTime,
			int index, boolean readInstruction) {
		int i = 0;
		int size = l / 16;
		for (; i < size; i++) {
			if (readInstruction) {
				instructionsCache[index][i].setDirty(true);
				instructionsCache[index][i].setTimeInCache(currentTime);
				if (instructionsCache[index][i].getAddress() == address)
					instructionsCache[index][i].setValue(value);
			} else {
				dataCache[index][i].setDirty(true);
				dataCache[index][i].setTimeInCache(currentTime);
				if (dataCache[index][i].getAddress() == address)
					dataCache[index][i].setValue(value);
			}
			cache[index][i].setDirty(true);
			cache[index][i].setTimeInCache(currentTime);
			if (cache[index][i].getAddress() == address)
				cache[index][i].setValue(value);
		}
	}

	private void writeThrough(int address, Object value, long currentTime,
			int index, boolean readInstruction) {
		int i = 0;
		int size = s / l;

		for (; i < size; i++) {
			if (readInstruction) {
				instructionsCache[index][i].setTimeInCache(currentTime);
				if (instructionsCache[index][i].getAddress() == address) {
					instructionsCache[index][i].setValue(value);
				}
			} else {
				dataCache[index][i].setTimeInCache(currentTime);
				if (dataCache[index][i].getAddress() == address) {
					dataCache[index][i].setValue(value);
				}
			}
			cache[index][i].setTimeInCache(currentTime);
			if (cache[index][i].getAddress() == address) {
				cache[index][i].setValue(value);
				updateMemoryAndCaches(index, i, currentTime);
			}
		}
	}

	public void writeToCache(int address, Object value, long currentTime,
			Instruction instruction, boolean readInstruction) throws Exception {
		int baseAdress = (int) (address - address % Math.pow(2, disp));
		int index = 0;
		boolean found = false;
		instruction.setCacheStartTime(currentTime);

		if (readInstruction) {
			for (int i = 0; i < instructionsCache.length; i++) {
				if (instructionsCache[i] != null) {
					if (instructionsCache[i][0] != null
							&& instructionsCache[i][0].check(baseAdress)) { // Found
						for (int j = 0; j < instructionsCache[i].length; j++) {
							instructionsCache[i][j].setTimeInCache(currentTime);
							cache[i][j].setTimeInCache(currentTime);
						}
						index = i;
						found = true;
						break;
					}
				}
			}
		} else {
			for (int i = 0; i < dataCache.length; i++) {
				if (dataCache[i] != null) {
					if (dataCache[i][0] != null
							&& dataCache[i][0].check(baseAdress)) { // Found
						for (int j = 0; j < dataCache[i].length; j++) {
							dataCache[i][j].setTimeInCache(currentTime);
							cache[i][j].setTimeInCache(currentTime);
						}
						index = i;
						found = true;
						break;
					}
				}
			}
		}

		if (found) {
			if (writePolicy.equals(WRITE_BACK))
				writeBack(address, value, currentTime, index, readInstruction);
			else
				writeThrough(address, value, currentTime, index,
						readInstruction);
			instruction.setCacheEndTime(currentTime + cycles);
			hits++;
		} else {
			misses++;
			if (writePolicy.equals(WRITE_BACK)) {
				if (l2 != null) {
					try {
						l2.readFromCache(address, currentTime);
						instruction.setCacheEndTime(currentTime + cycles
								+ l2.getCycles());

					} catch (Exception e) {
						if (l3 == null) {
							l2.readMiss(address, currentTime);
							instruction.setCacheEndTime(currentTime + cycles
									+ l2.getCycles() + Memory.getAccessTime());
						} else {
							try {
								l3.readFromCache(address, currentTime);
								instruction.setCacheEndTime(currentTime
										+ cycles + l2.getCycles()
										+ l3.getCycles());
							} catch (Exception e1) {
								l3.readMiss(address, currentTime);
								instruction.setCacheEndTime(currentTime
										+ cycles + l2.getCycles()
										+ l3.getCycles()
										+ Memory.getAccessTime());
							}
						}
					}
				} else {
					readMiss(address, currentTime);
					instruction.setCacheEndTime(currentTime + cycles
							+ Memory.getAccessTime());
				}
				index = getIndex(address);
				if (index == -1)
					throw new Exception(NOT_FOUND);
				writeBack(address, value, currentTime, index, readInstruction);
			} else {
				// TODO If found not found in all levels.
				updateMemoryAndCaches2(address, value, currentTime);
			}

		}
	}

	private void updateMemoryAndCaches2(int address, Object value,
			long currentTime) {
		Memory.store(address, value);
		if (l2 != null) {
			l2.updateValue(address, value, currentTime);
		}
		if (l3 != null) {
			l3.updateValue(address, value, currentTime);
		}
	}

	private int getIndex(int address) {
		int baseAddress = (int) (address - address % Math.pow(2, disp));
		int i = 0;
		for (; i < cache.length; i++) {
			if (cache[i][0] != null)
				if (cache[i][0].getAddress() == baseAddress)
					return i;
		}
		return -1;
	}

	private void addToCache(int index, int baseAddress, Object[] value,
			long currentTime) {
		int size = l / 16;

		instructionsCache[index] = new CacheEntry[size];
		dataCache[index] = new CacheEntry[size];
		cache[index] = new CacheEntry[size];
		for (int i = 0; i < size; i++) {
			instructionsCache[index][i] = new CacheEntry(baseAddress + i,
					value[i], currentTime);
			dataCache[index][i] = new CacheEntry(baseAddress + i, value[i],
					currentTime);
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

	public void updateFromL2Cache(int address, int baseAddress2,
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
						for (int i = 0; i < l / 16; i++) {
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
			return;
			// return value[address-temp];
		} else if (m == s / l) {
			for (int i = 0; i < cache.length; i++) {
				if (cache[i][0] == null) {
					int deltaAddress = temp - baseAddress2;
					Object[] value = new Object[(l / 16)];
					for (int j = deltaAddress; j < deltaAddress + (l / 16); j++) {
						value[j - deltaAddress] = values[i].getValue();
					}
					addToCache(i, temp, value, currentTime);
					return;
					// return value[address-temp];
				}
			}
			int index = LRU();
			if (writePolicy.equals(WRITE_BACK)) {
				if (cache[index][0].isDirty()) {
					for (int i = 0; i < l / 16; i++) {
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
			return;
			// return value[address-temp];
		} else {
			baseAddress = (int) (baseAddress / Math.pow(2, disp));
			int index = baseAddress % (s / l / m);
			for (; index < cache.length; index += (s / l / m)) {
				if (cache[index][0] == null) {
					int deltaAddress = temp - baseAddress2;
					Object[] value = new Object[(l / 16)];
					for (int j = deltaAddress; j < deltaAddress + (l / 16); j++) {
						value[j - deltaAddress] = values[j].getValue();
					}
					addToCache(index, temp, value, currentTime);
					return;
					// return value[address-temp];
				}
			}
			index = baseAddress % (s / l / m);
			int newIndex = setAssociativeLRU(index);
			if (writePolicy.equals(WRITE_BACK)) {
				if (cache[newIndex][0].isDirty()) {
					for (int i = 0; i < l / 16; i++) {
						updateMemoryAndCaches(newIndex, i, currentTime);
					}
				}
			}
			int deltaAddress = temp - baseAddress2;
			Object[] value = new Object[(l / 16)];
			for (int j = deltaAddress; j < deltaAddress + (l / 16); j++) {
				value[j - deltaAddress] = values[j].getValue();
			}
			addToCache(newIndex, temp, value, currentTime);
			return;
			// return value[address-temp];
		}

	}

	private void updateMemoryAndCaches(int cacheLine, int indexInCacheLine,
			long currentTime) {
		int address = cache[cacheLine][indexInCacheLine].getAddress();
		Object value = cache[cacheLine][indexInCacheLine].getValue();
		Memory.store(address, value);
		if (l2 != null) {
			l2.updateValue(address, value, currentTime);
		}
		if (l3 != null) {
			l3.updateValue(address, value, currentTime);
		}
	}

	private int LRU() {
		Arrays.sort(cache);
		return 0;
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

	public L3Cache getL3() {
		return l3;
	}

	public void setL3(L3Cache l3) {
		this.l3 = l3;
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
