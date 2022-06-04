.PHONY: local remote

BIN=concatenator
CFLAGS=-O0 -no-pie
CC=gcc
OBJCOPY=objcopy
DD=dd

local:
	$(CC) -o /dev/shm/$(BIN).o -c $(BIN).c
	$(DD) if=/dev/zero of=/dev/shm/nulls count=1024
	$(OBJCOPY) --add-section .cont=/dev/shm/nulls --set-section-flags .cont="alloc,code,load" /dev/shm/$(BIN).o /dev/shm/$(BIN).i.o
	$(CC) -o $(BIN) $(CFLAGS) /dev/shm/$(BIN).i.o -lgcc -lc
