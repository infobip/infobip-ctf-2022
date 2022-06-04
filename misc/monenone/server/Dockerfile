FROM ubuntu
EXPOSE 9000

RUN groupadd -r ctf && useradd --no-log-init -r -g ctf ctf
RUN mkdir chall/
WORKDIR chall/

COPY target/release/monenone .
RUN chmod +x ./monenone
USER ctf
CMD ["/chall/monenone"]