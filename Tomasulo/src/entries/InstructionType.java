package entries;

public enum InstructionType {
	LW, SW, 
	JMP,   // Instrução Incondicional
	BEQ,   // Instrução Condicional
	JALR,  // Jump e Link
	RET,
	ADD, SUB, NAND, MUL, ADDI,
	END    // Pseudo Fim
}