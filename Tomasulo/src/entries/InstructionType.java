package entries;

public enum InstructionType {
	LW, SW, 
	JMP,   // Instru��o Incondicional
	BEQ,   // Instru��o Condicional
	JALR,  // Jump e Link
	RET,
	ADD, SUB, NAND, MUL, ADDI,
	END    // Pseudo Fim
}