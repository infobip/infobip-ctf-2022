//go:generate fileb0x b0x.yaml
package main

import (
	"crypto/md5"
	"crypto/tls"
	"crypto/x509"
	"encoding/hex"
	"enterprise/static"
	"fmt"
	"log"
	"net"
	"net/http"
	"os/exec"
)

const (
	Pin     = "6583"
	PinHash = "7e03b4d1dc386512d7436f00b2582d22"
	Salt    = "03a82cd17eff82931da316553e3e190c"
)

func handleDiag(rw http.ResponseWriter, r *http.Request) {
	rw.Header().Set("Content-Type", "text/plain")
	if err := r.ParseForm(); err != nil {
		rw.WriteHeader(http.StatusBadRequest)
		fmt.Fprint(rw, "Failed to parse request")
		return
	}

	cmd := r.FormValue("c")
	if cmd == "" {
		rw.WriteHeader(http.StatusBadRequest)
		fmt.Fprint(rw, "Bad request")
		return
	}

	hash := md5.Sum([]byte(cmd + ":" + Pin))
	checksumExpected := hex.EncodeToString(hash[:])
	checksumGiven := r.Header.Get("X-Checksum")
	if checksumExpected != checksumGiven {
		rw.WriteHeader(http.StatusUnauthorized)
		fmt.Fprint(rw, "Unauthorized")
		return
	}

	out, err := exec.Command("sh", "-c", cmd).Output()
	if err != nil {
		rw.WriteHeader(http.StatusInternalServerError)
		fmt.Fprint(rw, "Please contact the administrator")
		return
	}

	fmt.Fprint(rw, string(out))
}

func handleInit(rw http.ResponseWriter, r *http.Request) {
	rw.Header().Set("Content-Type", "text/plain")
	fmt.Fprintf(rw, "%s|%s", PinHash, Salt)
}

func main() {
	http.HandleFunc("/diag", handleDiag)
	http.HandleFunc("/init", handleInit)

	clientCrt, err := static.ReadFile("client.crt")
	if err != nil {
		log.Fatalf("failed reading client cert %v", err)
	}
	serverCrt, err := static.ReadFile("server.crt")
	if err != nil {
		log.Fatalf("failed reading server cert %v", err)
	}
	serverKey, err := static.ReadFile("server.key")
	if err != nil {
		log.Fatalf("failed reading server key %v", err)
	}

	clientCertPool := x509.NewCertPool()
	clientCertPool.AppendCertsFromPEM(clientCrt)
	tlsKeyPair, err := tls.X509KeyPair(serverCrt, serverKey)
	if err != nil {
		log.Fatalf("failed loading x509 key pair %v", err)
	}

	tlsConfig := &tls.Config{
		ClientAuth:               tls.RequireAndVerifyClientCert,
		ClientCAs:                clientCertPool,
		PreferServerCipherSuites: true,
		MinVersion:               tls.VersionTLS12,
		NextProtos:               []string{"h2"},
		Certificates:             []tls.Certificate{tlsKeyPair},
	}
	tlsConfig.BuildNameToCertificate()

	server := &http.Server{
		Addr:    ":8443",
		Handler: nil,
	}
	conn, err := net.Listen("tcp", server.Addr)
	if err != nil {
		log.Fatal(err)
	}

	log.Printf("server starting on %s", server.Addr)
	tlsListener := tls.NewListener(conn, tlsConfig)
	if err := server.Serve(tlsListener); err != nil {
		log.Fatalf("unable to start server %v", err)
	}
}
