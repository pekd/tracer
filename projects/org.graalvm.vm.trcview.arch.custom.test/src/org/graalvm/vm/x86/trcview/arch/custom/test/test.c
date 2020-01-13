typedef void EVENT;
typedef void NODE;

typedef uint8_t u8;
typedef uint16_t u16;
typedef uint64_t u64;

typedef struct state {
	u64		step;
	u16		pc;
	u8		a;
	u8		x;
	u8		y;
} STATE;

typedef struct step {
	STATE	state;
	u8		insn_len;
	u8		insn[1];
} STEP;

short init()
{
	set_name("c_arch");
	set_description("Custom Test Architecture");
	set_step_type("STEP", "state", "insn", "insn_len");
	set_state_type("STATE", "pc", "step");
	return 0xFF00;
}

void process(EVENT* event, NODE* node)
{
	if(is_step_event(event)) {
		u64 pc = get_field(event, "pc");
		if(pc == 0xBEEF) {
			STEP step;
			step.state.step = 0;
			step.state.pc = pc;
			step.state.a = get_field(event, "temp");
			step.insn[0] = 21;
			step.insn_len = 1;
			create_step(step);
		}
	}
}
