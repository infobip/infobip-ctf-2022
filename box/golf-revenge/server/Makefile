.PHONY: local remote

BIN=golfrev
CFLAGS=-O0 -fno-stack-protector -z execstack
CC=gcc

local:
	$(CC) -o $(BIN) $(CFLAGS) $(BIN).c -lseccomp

