#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <stdbool.h>
#include <unistd.h>

#include <sys/ptrace.h>

#define PS 40
#define SS 4

__attribute__((constructor))
void setup() {
	if (ptrace(PTRACE_TRACEME, 0) < 0) {
		exit(0);
	}
    setvbuf(stdin, NULL, _IONBF, 0);
    setvbuf(stdout, NULL, _IONBF, 0);
    setvbuf(stderr, NULL, _IONBF, 0);
    alarm(60);
}

const uint8_t pp[PS] = {196, 117, 74, 135, 92, 145, 84, 106, 237, 51, 245, 86, 20, 157, 133, 80, 116, 100, 89, 73, 42, 214, 175, 165, 187, 143, 144, 58, 62, 168, 201, 230, 174, 137, 162, 208, 130, 90, 185, 132};
const uint8_t seq[SS] = {4, 5, 6, 6};
const char* pq = "FqE)7Q(1zr?3exR!jbaS2WH9NM*vKylIV+dXgLkm";

struct node {
	uint8_t pos;
	uint8_t data;
	struct node* l;
	struct node* r;
};

struct node* insert(struct node* root, uint8_t pos, uint8_t data) {
	if (root == NULL) {
		root = malloc(sizeof(struct node));
		root->pos = pos;
		root->data = data;
		root->l = root->r = NULL;
	} else if (pos < root->pos) {
		root->l = insert(root->l, pos, data);
	} else if (pos > root->pos) {
		root->r = insert(root->r, pos, data);
	}
	return root;
}

struct node* delete_node(struct node* root) {
	if (root->l == NULL && root->r == NULL) {
		free(root);
		root = NULL;
		return root;
	}
	
	struct node* parent;
	struct node* iter;
	struct node* temp = root;
	
	if (root->l != NULL && root->r == NULL) {
		root = root->l;
	} else if (root->l == NULL && root->r != NULL) {
		root = root->r;
	} else if (root->l != NULL && root->r != NULL) {
		parent = root;
		iter = root->r;
		while(iter->l != NULL) {
			parent = iter;
			iter = iter->l;
		}
		
		iter->l = root->l;
		if(root != parent) {
			iter->r = root->r;
			parent->l = NULL;
		} else {
			iter->r = NULL;
		}
		root = iter;
	}

	free(temp);
	temp = NULL;
	return root;
}

struct node* delete(struct node* root, uint8_t pos) {
	if (root == NULL) {
		return NULL;
	}
	
	if (pos < root->pos) {
		root->l = delete(root->l, pos);
	} else if (pos > root->pos) {
		root->r = delete(root->r, pos);
	} else {
		root = delete_node(root);
	}
	return root;
}

struct node* min(struct node* root) {
	if (root == NULL) {
		return NULL;
	}
	
	if (root->l == NULL) {
		return root;
	} else {
		return min(root->l);
	}
}

struct node* max(struct node* root) {
	if (root == NULL) {
		return NULL;
	}
	
	if (root->r == NULL) {
		return root;
	} else {
		return max(root->r);
	}
}

struct node* init() {
	uint8_t i;
	struct node* root = NULL;
	
	for (i = 0; i < PS; ++i) {
		root = insert(root, pp[i], pq[i]);
	}
	
	return root;
}

bool ch_sct(struct node* root, char* input, uint8_t* in_st, uint8_t times, struct node* (*f)(struct node*)) {
	uint8_t i;
	struct node* n;
	
	for (i = 0; i < times; ++i, ++*in_st) {
		n = f(root);
		if (n->data != input[*in_st]) {
			return false;
		}
		root = delete(root, n->pos);
	}
	
	return true;
}

bool check(struct node* root, char* input) {
	uint8_t i, st;
	
	for (i = st = 0; i < SS; ++i) {
		if (!ch_sct(root, input, &st, seq[i], i % 2 ? max : min)) {
			return false;
		}
	}
	
	return true;
}

int main() {
	uint8_t in[32];
	struct node* root = init();
	
	printf("---- VERIFICATION CHECK ----\nInput: ");
	read(STDIN_FILENO, in, 32);
	printf(check(root, in) ? "VERIFIED" : "WRONG");

	return 0;
}
