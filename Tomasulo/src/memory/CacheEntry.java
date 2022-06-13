package memory;

public class CacheEntry {
	private int address;
	private Object value;
	private boolean dirty;
	private long timeInCache;

	public CacheEntry(int address, Object value, long timeInCache) {
		this.address = address;
		this.value = value;
		this.timeInCache = timeInCache;
	}

	public boolean check(int address) {
		if (this.address == address) {
			return true;
		} else {
			return false;
		}
	}

	public int getAddress() {
		return address;
	}

	public void setAddress(int address) {
		this.address = address;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public boolean isDirty() {
		return dirty;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	public long getTimeInCache() {
		return timeInCache;
	}

	public void setTimeInCache(long timeInCache) {
		this.timeInCache = timeInCache;
	}

	public int compare(Object o) {
		CacheEntry c = (CacheEntry) o;
		
		if (this.timeInCache < c.timeInCache) {
			return -1;
		}
		if (this.timeInCache > c.timeInCache) {
			return 1;
		}
		return 0;
	}

	public String toString() {
		String x = "";
		
		x = "[ Endereço: " + address + ", Valor: " + value + ", Tempo: " + timeInCache + ", Sujo: " + dirty + "]";
		
		return x;
	}

}
