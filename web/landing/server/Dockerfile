FROM python:3-slim-bullseye

RUN apt-get update && apt-get upgrade -y
RUN groupadd -r ctf && useradd --no-log-init -r -g ctf ctf

RUN mkdir app && chown ctf:ctf app/
WORKDIR app
COPY templates ./templates
COPY app.py ./app.py
COPY flag.txt /home/ctf/flag.txt
RUN pip3 install Flask gunicorn lxml PyYAML

USER ctf
CMD ["gunicorn", "-w", "4", "-b", "0.0.0.0:8080", "app:app"]
