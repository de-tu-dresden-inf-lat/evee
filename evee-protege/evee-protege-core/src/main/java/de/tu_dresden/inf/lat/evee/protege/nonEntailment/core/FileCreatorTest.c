#include <conio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>

int main() {
    mkdir("E://Programs//Protege//Protege-5.5.0//NewlyCreatedDirectory");
	FILE *fp;
	fp = fopen ("E://Programs//Protege//Protege-5.5.0//NewlyCreatedDirectory//NewlyCreatedFile.txt", "w");
	fclose (fp);
	printf("Hello World\n");
	return 0;
}