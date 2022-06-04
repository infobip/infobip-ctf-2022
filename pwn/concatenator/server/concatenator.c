#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <unistd.h>
#include <signal.h>
#include <string.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>

#define __MAX_SIZE 16
#define __MAX_CHAR 32

__attribute__((constructor))
void setup() {
		setvbuf(stdin, NULL, _IONBF, 0);
		setvbuf(stdout, NULL, _IONBF, 0);
		setvbuf(stderr, NULL, _IONBF, 0);
}

void handle_request(int cfd){
	char* strings_no = "number of strings: \0\0\0\0";
	char num[2];
	char storage[__MAX_SIZE][__MAX_CHAR];
	uint8_t size;
	uint8_t i;
	
	write(cfd, strings_no, strlen(strings_no));
	read(cfd, num, 2);
	
	size = atoi(num);
	if (size > __MAX_SIZE) {
		size = __MAX_SIZE;
	}

	for (i = 0; i < size; ++i) {
		strings_no = "string: ";
		write(cfd, strings_no, strlen(strings_no));
		read(cfd, storage[i], 1024);
	}

	strings_no = "resulting string is: ";
	write(cfd, strings_no, strlen(strings_no));
	for (i = 0; i < size; ++i) {
		write(cfd, storage[i], strlen(storage[i]) - 1);
	}
}

int main(int argc, char const *argv[])
{
	int sfd;
	int cfd;
	int opt = 1;
	char sip[INET_ADDRSTRLEN];
	struct sockaddr_in s_addr;
	s_addr.sin_family = AF_INET;
	s_addr.sin_port = htons(9000);
	s_addr.sin_addr.s_addr = INADDR_ANY;
	
	if ((sfd = socket(AF_INET, SOCK_STREAM, 0)) < 0) {
		printf("init sock failed\n");
		return 1;
	}
	
	if (setsockopt(sfd, SOL_SOCKET, SO_REUSEADDR | SO_REUSEPORT, &opt, sizeof(opt)) < 0) {
		printf("setsockopt failed\n");
		return 1;
	}
	
	if (bind(sfd, (struct sockaddr*)&s_addr, sizeof(s_addr)) < 0) {
		printf("bind failed\n");
		return 1;
	}
	
	if (listen(sfd,10) < 0) {
		printf("listen failed\n");
		return 1;
	}
	
	inet_ntop(AF_INET, &(s_addr.sin_addr), sip, INET_ADDRSTRLEN);
	printf("listening on %s:9000, sfd is %d\n", sip, sfd);
	
	struct sockaddr_in c_addr;
	socklen_t addr_len = sizeof(c_addr);
	while (1) {
		if ((cfd = accept(sfd, (struct sockaddr*)&c_addr, (socklen_t*)&addr_len)) < 0) {
			printf("accept failed\n");
			continue;
		}
		
		pid_t pid;
		if ((pid = fork()) == 0) { 
			close(sfd);
			char cip[INET_ADDRSTRLEN];
			int cport = ntohs(c_addr.sin_port);
			inet_ntop(AF_INET, &(c_addr.sin_addr), cip, INET_ADDRSTRLEN);
			handle_request(cfd);
			close(cfd);
			return 0;
		}
		
		signal(SIGCHLD, SIG_IGN);
		close(cfd);
	}
	
	close(sfd);
	return 0;
}
