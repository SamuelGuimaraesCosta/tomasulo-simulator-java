package entries;

public class RobEntry implements Entry {
	int destination; // Index do registrador ou endereço de memória
	int value; // Valor a ser commitado
	boolean branchTaken;

	boolean ready = false;

	InstructionType type;
	
	public String toString() {
		return String.format("Destino do Buffer de Reordenamento: %d, Valor: %d, Pronto: %b, Tipo: %s", destination, value, ready, type.toString());
	}

	public RobEntry(int destination, InstructionType type) {
		this.destination = destination;
		this.type = type;
	}

	public void setReady() {
		ready = true;
	}

	public void setValue(int val) {
		value = val;
	}

	public void setDestination(int destination) {
		this.destination = destination;
	}

	public int getDest() {
		return destination;
	}

	public int getVal() {
		return value;
	}

	public InstructionType getType() {
		return type;
	}

	public boolean isBranchTaken() {
		return branchTaken;
	}

	public void setBranchTaken(boolean b) {
		branchTaken = b;
	}
	
	public boolean isReady() {
		return ready;
	}
}
