package main

import (
	"encoding/hex"
	"fmt"
	"forsolve/protocol"
	"os"
	"strings"

	"github.com/golang/protobuf/proto"
	"github.com/google/gopacket"
	"github.com/google/gopacket/layers"
	"github.com/google/gopacket/pcapgo"

	"golang.org/x/crypto/nacl/secretbox"
)

const (
	TargetDomainName = "a.ibctf.local"
	EncryptionKey    = "cc42312b6a034b78885c50c37e2e06bcb789454ca8992b984912959e4ba24bd6"
)

func decrypt(payload []byte, in_nonce []byte, secretKey string) ([]byte, bool) {
	var secret [32]byte
	var nonce [24]byte
	var out []byte

	secretKeyBytes, err := hex.DecodeString(secretKey)
	if err != nil {
		panic(err)
	}

	copy(secret[:], secretKeyBytes)
	copy(nonce[:], in_nonce)

	return secretbox.Open(out, payload, &nonce, &secret)
}

func main() {
	f, err := os.Open("dump.pcapng")
	if err != nil {
		panic(err)
	}

	r, err := pcapgo.NewNgReader(f, pcapgo.DefaultNgReaderOptions)
	if err != nil {
		panic(err)
	}

	for {
		// Reads one packet
		data, _, err := r.ReadPacketData()
		if err != nil {
			break
		}

		// Decode as Ethernet
		packet := gopacket.NewPacket(data, layers.LayerTypeEthernet, gopacket.Default)
		udpLayer := packet.Layer(layers.LayerTypeUDP)
		udp := udpLayer.(*layers.UDP)

		// Extract UDP payload and decode as DNS
		dnsPayload := gopacket.NewPacket(udp.Payload, layers.LayerTypeDNS, gopacket.Default)
		dnsLayer := dnsPayload.Layer(layers.LayerTypeDNS)
		dns := dnsLayer.(*layers.DNS)

		// Extract content from DNS name
		name := string(dns.Questions[0].Name)
		dataPacket := strings.Replace(strings.Replace(name, TargetDomainName, "", -1), ".", "", -1)
		dataPacketRaw, err := hex.DecodeString(dataPacket)
		if err != nil {
			fmt.Println("failed decoding raw packet")
			panic(err)
		}

		// Decrypt content
		output, valid := decrypt(dataPacketRaw[24:], dataPacketRaw[:24], EncryptionKey)
		if !valid {
			fmt.Println("invalid packet")
			break
		}

		// Serialize to message
		message := &protocol.Message{}
		if err := proto.Unmarshal(output, message); err != nil {
			fmt.Printf("failed to parse message packet: %v\n", err)
			break
		}

		// Dump message data
		packetData := message.GetChunkdata().GetPacket()
		fmt.Printf("%s", packetData)
	}
}
