# Don't forget to execute
# echo 0 | sudo tee /proc/sys/kernel/randomize_va_space
# on target host to disable ASLR
FROM ubuntu:20.04
EXPOSE 9000

RUN rm -rf /var/lib/apt/lists/*

RUN useradd -m ctf
WORKDIR /ctf

COPY flag.txt .
COPY concatenator .

RUN chmod -R 755 /ctf
USER ctf

ENTRYPOINT []
CMD ["/ctf/concatenator"]
