# Build

```sh
apt install -y libseccomp-dev
make local
docker build -t golfrev .
docker run -d -p 9000:9000 -it golfrev
nc localhost 9000
```
