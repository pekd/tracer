int main(void)
{
	char out = 0x55;
	__asm__("movb $0x80, %%al; cmpb $0x7F, %%al; seto %%al" : "=a"(out));
	return out;
}
