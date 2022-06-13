package memory;

public class Instruction {
	final static String LOAD_WORD = "load word";
	final static String STORE_WORD = "store word";
	final static String JUMP = "jump";
	final static String BRANCH_IF_EQUAL = "branch if equal";
	final static String JUMP_AND_LINK = "jump and link";
	final static String RETURN = "return";
	final static String ADD = "add";
	final static String ADDI = "add immediate";
	final static String NAND = "nand";
	final static String MULT = "mutliply";
	final static String DIV = "divide";
	final static String UNDEFINED = "undifined";
	final static String STATE_FETCH = "fetch";
	final static String STATE_DECODE = "decode";
	final static String STATE_DISPATCH = "dispatch";
	final static String STATE_ISSUE = "issue";
	final static String STATE_EXEC = "execute";
	final static String STATE_COMMIT = "commit";

	private String type, currentState;
	private Register regA, regB, regC;
	private long decStartTime, decEndTime, dispStartTime, dispEndTime, issueStartTime, issueEndTime, execStartTime, execEndTime, commitStartTime, commitEndTime, cacheStartTime, cacheEndTime;

	public long getIssueStartTime() {
		return issueStartTime;
	}

	public void setIssueStartTime(long issueStartTime) {
		this.issueStartTime = issueStartTime;
	}

	public long getIssueEndTime() {
		return issueEndTime;
	}

	public void setIssueEndTime(long issueEndTime) {
		this.issueEndTime = issueEndTime;
	}

	private int ID, executionTime;

	public Instruction() {
		this.setType(UNDEFINED);
		this.setRegA(new Register());
		this.regB = new Register();
		this.regC = new Register();
		this.immediate = 0;
	}

	public Instruction(int Id,String type, Register regA, Register regB, Register regC, int immediate, int ex) {
		this.setType(type);
		this.setRegA(regA);
		this.regB = regB;
		this.regC = regC;
		this.immediate = immediate;
		this.setExecutionTime(ex);
		this.ID = Id;
	}

	public Register getRegB() {
		return regB;
	}

	public void setRegB(Register regB) {
		this.regB = regB;
	}

	public Register getRegC() {
		return regC;
	}

	public void setRegC(Register regC) {
		this.regC = regC;
	}

	public int getImmediate() {
		return immediate;
	}

	public void setImmediate(int immediate) {
		this.immediate = immediate;
	}

	private int immediate;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Register getRegA() {
		return regA;
	}

	public void setRegA(Register regA) {
		this.regA = regA;
	}

	public String getCurrentState() {
		return currentState;
	}

	public void setCurrentState(String currentState) {
		this.currentState = currentState;
	}

	public long getDecStartTime() {
		return decStartTime;
	}

	public void setDecStartTime(long decStartTime) {
		this.decStartTime = decStartTime;
	}

	public long getDecEndTime() {
		return decEndTime;
	}

	public void setDecEndTime(long decEndTime) {
		this.decEndTime = decEndTime;
	}

	public long getDispStartTime() {
		return dispStartTime;
	}

	public void setDispStartTime(long dispStartTime) {
		this.dispStartTime = dispStartTime;
	}

	public long getDispEndTime() {
		return dispEndTime;
	}

	public void setDispEndTime(long dispEndTime) {
		this.dispEndTime = dispEndTime;
	}

	public long getExecStartTime() {
		return execStartTime;
	}

	public void setExecStartTime(long execStartTime) {
		this.execStartTime = execStartTime;
	}

	public long getExecEndTime() {
		return execEndTime;
	}

	public void setExecEndTime(long execEndTime) {
		this.execEndTime = execEndTime;
	}

	public long getCommitStartTime() {
		return commitStartTime;
	}

	public void setCommitStartTime(long commitStartTime) {
		this.commitStartTime = commitStartTime;
	}

	public long getCommitEndTime() {
		return commitEndTime;
	}

	public void setCommitEndTime(long commitEndTime) {
		this.commitEndTime = commitEndTime;
	}

	public int getID() {
		return ID;
	}

	public void setID(int iD) {
		ID = iD;
	}

	public int getExecutionTime() {
		return executionTime;
	}

	public void setExecutionTime(int executionTime) {
		this.executionTime = executionTime;
	}

	@Override
	public boolean equals(Object o) {
		Instruction r = (Instruction) o;
		if (this.ID == r.ID) {
			return true;
		}
		return false;
	}

	public String toString() {
		String ins;
		ins = "Instrução: "+ ID + "Decodificada: " + decStartTime + " - " + decEndTime + "Despachada: " + dispStartTime + " - " + dispEndTime + "Tarefa: " + issueStartTime + " - " + issueEndTime
				+ "Executada: " + execStartTime + " - " + execEndTime + "Tempo de Commit: " + commitEndTime + "Tempo Final do Cahce: " + cacheEndTime;
		
		return ins;
	}

	public long getCacheStartTime() {
		return cacheStartTime;
	}

	public void setCacheStartTime(long cacheStartTime) {
		this.cacheStartTime = cacheStartTime;
	}

	public long getCacheEndTime() {
		return cacheEndTime;
	}

	public void setCacheEndTime(long cacheEndTime) {
		this.cacheEndTime = cacheEndTime;
	}
}
