FROM openjdk:11

RUN apt-get update && apt-get install -y file

RUN groupadd -r ctf && useradd --no-log-init -r -g ctf ctf
RUN groupadd -r flag && useradd --no-log-init -r -g flag flag
RUN echo "flag:4ctJustL1k3Y0uKn0w" | chpasswd

RUN mkdir web/ && chown ctf:ctf web/
WORKDIR web

COPY partners.jar ./partners.jar
COPY flag.txt /home/flag/flag.txt
RUN chown flag:flag /home/flag/flag.txt
RUN chmod 600 /home/flag/flag.txt

USER ctf
COPY instructions.txt /home/ctf/instructions.txt
ENV PARTNER_JWT_SECRET=d23ab20fbb75cbb1cb6bc3368a36fb145aacf91b55d106404528b0c34fff7df1
CMD ["java", "-jar", "partners.jar"]
