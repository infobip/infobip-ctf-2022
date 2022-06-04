#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <fcntl.h>

#define __SECRET_COUNT 20
#define __CHUNK_SIZE 100
#define __FILE_BUF_SIZE 1024

__attribute__((constructor))
void setup() {
	setvbuf(stdin, NULL, _IONBF, 0);
	setvbuf(stdout, NULL, _IONBF, 0);
	setvbuf(stderr, NULL, _IONBF, 0);
	alarm(60);
}

char* ptrs[__SECRET_COUNT];
int ptr_idx = 0;

int selftest() {
	void* test_ptr = calloc(0, 0);
	if (test_ptr == NULL) {
		return -1;
	}
	fgets(test_ptr, 0, stdin);
	if (ferror(stdin)) {
		return 1;
	}
	fwrite(test_ptr, 0, 0, stdout);
	if (ferror(stdout)) {
		return 2;
	}
	free(test_ptr);
}

void secret_inp(char* content) {
	printf("Secret content: ");
	fgets(content, 1024, stdin);
}

void new_or_update(uint8_t pos, uint8_t update) {
	if (!update || (update && ptrs[pos] != 0x00)) {
		ptrs[pos] = calloc(1, __CHUNK_SIZE);
		secret_inp(ptrs[pos]);
	} else {
		printf("Err: cannot operate on slot %d\n", pos);
	}
}

void load(uint8_t pos) {
	if (ptrs[pos] != 0x00) {
		printf("%d. - ", pos);
		fwrite(ptrs[pos], 1, __CHUNK_SIZE, stdout);
		printf("\n");
	} else {
		printf("%d. - *slot is free*\n", pos);
	}
}

void drop(uint8_t pos) {
	if (ptrs[pos] != 0x00) {
		free(ptrs[pos]);
		printf("Deleted slot %d\n", pos);
	} else {
		printf("Err: cannot delete slot %d\n", pos);
	}
}

int positioning() {
	int idx;
	printf("Secret index: ");
	scanf("%d", &idx);
	getchar();
	return idx;
}

void banner() {
	char buffer[575];
	sprintf(&buffer[0], "      :::        ::::::::  :::        :::::::::     :::      ::::::::   :::::::: \n"
		"     :+:       :+:    :+: :+:        :+:    :+:  :+: :+:   :+:    :+: :+:    :+: \n"
		"    +:+       +:+    +:+ +:+        +:+    +:+ +:+   +:+  +:+        +:+         \n"
		"   +#+       +#+    +:+ +#+        +#++:++#+ +#++:++#++: +#++:++#++ +#++:++#++   \n"
		"  +#+       +#+    +#+ +#+        +#+       +#+     +#+        +#+        +#+    \n"
		" #+#       #+#    #+# #+#        #+#       #+#     #+# #+#    #+# #+#    #+#     \n"
		"########## ########  ########## ###       ###     ###  ########   ########       \n");
	printf("%s", buffer);
	puts("  - We keep your secrets in memory.\n");
}

int menu() {
	char fp_buf[__FILE_BUF_SIZE];
	int fp, buflen, choice, idx, ret;
	printf("1. Create new secret (%d/%d)\n", ptr_idx, __SECRET_COUNT);
	puts("2. Read secret");
	puts("3. Update secret");
	puts("4. Delete secret");
	puts("5. Perform self-test");
	puts("9. About lolpass");
	puts("0. Exit");
	printf("Your choice: ");
	scanf("%d",  &choice);
	getchar();
	
	switch (choice) {
		case 0:
			break;
		case 1:
			new_or_update(ptr_idx++, 0);
			break;
		case 2:
			idx = positioning();
			load(idx);
			break;
		case 3:
			idx = positioning();
			new_or_update(idx, 1);
			break;
		case 4:
			idx = positioning();
			drop(idx);
			break;
		case 5:
			if ((ret = selftest()) != 0) {
				printf("%d errors have occurred during self-test, please restart\n", ret);
				exit(ret);
			}
			printf("self-test successful\n");
			break;
		case 9:
			puts("lolpass 0.1 © Serious security takers");
			fp = open("/proc/self/maps", O_RDONLY);
			while((buflen = read(fp, fp_buf, __FILE_BUF_SIZE)) > 0) {
					write(1, fp_buf, buflen);
			}
			close(fp);
			break;
		default:
			puts("choice unknown!\n");
	}
	
	return choice;
}

int main() {
	for (int i = 0; i < __SECRET_COUNT; ++i) {
		ptrs[i] = 0x00;
	}
	banner();
	
	int choice;
	do {
		choice = menu();
	} while (choice != 0);
	
	return 0;
}
