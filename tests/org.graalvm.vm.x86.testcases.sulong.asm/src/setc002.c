int main(void)
{
	char out = 0x55;
	__asm__("movl $0x42, %%eax; cmpl $0x84, %%eax; setc %%al" : "=a"(out));
	return out;
}
