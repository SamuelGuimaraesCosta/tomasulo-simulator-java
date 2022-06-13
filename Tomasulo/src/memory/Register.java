package memory;

public class Register {
	String name;
	int value;
	private final String UNDEFINED = "undefined";

	public Register() {
		this.name = UNDEFINED;
		this.value = 0;
	}

	public Register(String name, int value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	@Override
	public boolean equals(Object o) {
		Register r = (Register) o;
		if (this.name.equals(r.name)) {
			return true;
		}
		return false;
	}

}
