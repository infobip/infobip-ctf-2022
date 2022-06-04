//go:generate fileb0x b0x.yaml
package main

import (
	"bufio"
	"crypto/md5"
	"crypto/tls"
	"crypto/x509"
	"encoding/hex"
	"enterprise/static"
	"errors"
	"fmt"
	"io/ioutil"
	"log"
	"net/http"
	"net/url"
	"os"
	"strings"
)

const Host = "https://127.0.0.1:8443"

func loadTransportData() (clientCrt []byte, clientKey []byte, caCrt []byte, err error) {
	clientCrt, err = static.ReadFile("client.crt")
	if err != nil {
		return
	}
	clientKey, err = static.ReadFile("client.key")
	if err != nil {
		return
	}
	caCrt, err = static.ReadFile("CA.crt")
	if err != nil {
		return
	}
	return
}

func newClient() (*http.Client, error) {
	clientCrt, clientKey, caCrt, err := loadTransportData()
	if err != nil {
		return nil, err
	}
	caCertPool := x509.NewCertPool()
	caCertPool.AppendCertsFromPEM(caCrt)
	tlsKeyPair, err := tls.X509KeyPair(clientCrt, clientKey)
	if err != nil {
		return nil, err
	}

	return &http.Client{
		Transport: &http.Transport{
			TLSClientConfig: &tls.Config{
				RootCAs:            caCertPool,
				MinVersion:         tls.VersionTLS12,
				Certificates:       []tls.Certificate{tlsKeyPair},
				InsecureSkipVerify: true,
			},
		},
	}, nil
}

func auth(client *http.Client, host, pin string) (bool, error) {
	resp, err := client.Get(host)
	if err != nil {
		return false, err
	}
	
	if resp.StatusCode != http.StatusOK {
		return false, fmt.Errorf("server init returned %d", resp.StatusCode)
	}

	bodyBytes, err := ioutil.ReadAll(resp.Body)
	if err != nil {
	    return false, err
	}
	
	bodyString := string(bodyBytes)
	data := strings.Split(bodyString, "|")
	
	if len(data) != 2 {
		return false, errors.New("unexpected init body")
	}

	hash := md5.Sum([]byte(pin + ":" + data[1]))
	pinHashHex := hex.EncodeToString(hash[:])
	return pinHashHex == data[0], nil
}

func diagnostics(client *http.Client, c, host, pin string) error {
	data := url.Values{"c": {c}}
	hash := md5.Sum([]byte(c + ":" + pin))
	checksum := hex.EncodeToString(hash[:])

	req, err := http.NewRequest(http.MethodPost, host, strings.NewReader(data.Encode()))
	if err != nil {
		return err
	}
	req.Header.Add("X-Checksum", checksum)
	req.Header.Add("Content-Type", "application/x-www-form-urlencoded")

	resp, err := client.Do(req)
	if err != nil {
		return err
	}

	if resp.StatusCode != 200 {
		return fmt.Errorf("diagnostics failed with status %d", resp.StatusCode)
	}

	return nil
}

func main() {
	fmt.Printf("[!] Authorized users only [!]\n")
	scanner := bufio.NewScanner(os.Stdin)
	fmt.Print("Enter 4-digit PIN: ")
	scanner.Scan()
	text := scanner.Text()
	if scanner.Err() != nil {
		log.Fatal(scanner.Err())
	}
	
	client, err := newClient()
	if err != nil {
		log.Fatal(err)
	}

	success, err := auth(client, Host + "/init", text)
	if err != nil {
		log.Fatal(err)
	}
	if !success {
		fmt.Println("UNAUTHORIZED")
		return
	}

	fmt.Println("[+] Running diagnostics ...")
	if err := diagnostics(client, "/bin/true", Host + "/diag", text); err != nil {
		log.Fatal(err)
	}
	fmt.Println("Success")
}
