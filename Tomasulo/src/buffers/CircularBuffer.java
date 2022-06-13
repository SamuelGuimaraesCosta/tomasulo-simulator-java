package buffers;

import entries.*;

public class CircularBuffer {
	Entry[] buffer;
	
	int head;
	int tail;
	
	public CircularBuffer() {
		head = 0;
		tail = 0;
	}
	
	public boolean isEmpty() {
		return head == tail;
	}
	
	public void flush() {
		head = tail;
	}
	
	public boolean isFull() {
		return head == ((tail + 1) % buffer.length);
	}
	
	public void moveHead() {
		head++;
		
		head %= buffer.length;
	}
	
	public boolean add(Entry e) {
		if(isFull()) return false;
		
		buffer[tail++] = e;
		
		tail %= buffer.length;
		
		return true;
	}
	
	public Entry get(int address) {
		return buffer[address];
	}
	
	public Entry getFirst() {
		if(isEmpty()) return null;
		
		return buffer[head];
	}
	
	public int tailIndex() {
		return tail;
	}
}
