FROM alpine:3.14
EXPOSE 9000

ENV GROOVY_VERSION=3.0.9
ENV GROOVY_HOME="/opt/groovy-${GROOVY_VERSION}"

RUN apk add openjdk11 socat vim
RUN wget "https://archive.apache.org/dist/groovy/${GROOVY_VERSION}/distribution/apache-groovy-sdk-${GROOVY_VERSION}.zip"

RUN mkdir -p "${GROOVY_HOME}"
RUN unzip "apache-groovy-sdk-${GROOVY_VERSION}.zip" -d /opt
ENV PATH="${PATH}:${GROOVY_HOME}/bin"

RUN mkdir /ctf
WORKDIR /ctf

RUN addgroup -S ctf && adduser -s /bin/sh -S ctf -G ctf
COPY Shellgebra.groovy .
COPY flag.txt no-guess-file-flag.txt

ENV EDITOR=vim
CMD ["/usr/bin/socat", "TCP-LISTEN:9000,reuseaddr,fork", "EXEC:\"groovy Shellgebra.groovy\",pty,rawer,stderr,echo=0,su=ctf"]
