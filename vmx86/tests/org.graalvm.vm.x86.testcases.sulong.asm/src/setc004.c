int main(void)
{
	char out = 0x55;
	__asm__("movb $0xFE, %%al; cmpb $0xFF, %%al; setc %%al" : "=a"(out));
	return out;
}
