#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>
#include <unistd.h>

#define __MAX_SIZE 32

char* u;

uint32_t prompt_alloc_size() {
	uint32_t alloc_b;
	printf("input number of bytes to allocate (max %d): ", __MAX_SIZE);
	scanf("%d", &alloc_b);
	if (alloc_b > __MAX_SIZE) {
		printf("invalid! defaulting to %d bytes ...\n", __MAX_SIZE);
		alloc_b = __MAX_SIZE;
	}
	return alloc_b;
}

void loc(char* p, uint64_t off) {
	printf("your location is at %p\n", p+off);
}

uint64_t choose_offset(uint32_t max_alloc) {
	int64_t offset;
	printf("choose offset: ");
	scanf("%ld", &offset);
	if (offset > max_alloc) {
		printf("out of bounds offset! defaulting to 0 ...\n");
		return 0;
	}
	printf("offsetting for %ld ...\n", offset);
	return offset;
}

int loop() {
	uint64_t offset;
	uint32_t alloc_b = prompt_alloc_size();
	char *s = alloca(__MAX_SIZE);
	uint8_t i;
	char op;

	while (1) {
		loc(s, 0);
		offset = choose_offset(alloc_b);
		if (s+offset > s+alloc_b) {
			printf("out of bounds\n");
			return -1;
		}
		loc(s, offset);
	
		printf("choose op: (r)ead current, (w)rite current or (p)rint result? ");
		scanf(" %c", &op);
		getchar();
		switch (op) {
			case 'r':
				for (i = 0; i < alloc_b; ++i) {
					printf("%02x", (unsigned)(unsigned char)(*(s+offset+i)));
				}
				printf("\n");
				break;
			case 'w':
				fgets(s+offset, alloc_b-offset, stdin);
				break;
			case 'p':
				printf("%s", s);
				return 0;
			default:
				printf("unknown op\n");
		}
	}
	
	return 0;
}

int main() {
	alarm(60);
	setbuf(stdin, NULL);
	setbuf(stdout, NULL);
	setbuf(stderr, NULL);
	u = malloc(__MAX_SIZE);
	printf("give us your name: ");
	fgets(u, __MAX_SIZE, stdin);
	printf("hello %s!\n", u);
	int ret = loop();
	free(u);
}
