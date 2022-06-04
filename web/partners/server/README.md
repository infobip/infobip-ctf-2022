# Build

```
apt install maven
cd partners
mvn package spring-boot:repackage
cp target/*.jar ../partners.jar
cd ..
docker build -t partners .
docker run -d -p 8080:8080 -it partners
curl http://localhost:8080
```
