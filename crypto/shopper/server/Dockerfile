FROM python:3-slim-bullseye

RUN apt-get update && apt-get upgrade -y
RUN apt-get install -y sqlite3
RUN groupadd -r ctf && useradd --no-log-init -r -g ctf ctf

RUN mkdir app && chown ctf:ctf app/
WORKDIR app
COPY static ./static
COPY templates ./templates
COPY app.py ./app.py
COPY shopdb.sql ./shopdb.sql
RUN sqlite3 shop.db < shopdb.sql
RUN pip3 install Flask gunicorn pycryptodome

USER ctf
ENV FLASK_SECRET_KEY="87cd3b478e03583dad37dfe4d82963a3"
ENV SHOP_SIGNING_KEY="4d14947af4c281c5f7563d4b46b7dd6c"
CMD ["gunicorn", "-w", "4", "-b", "0.0.0.0:8080", "app:app"]
