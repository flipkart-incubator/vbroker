package main

import (
	"fmt"
	"io"
	"io/ioutil"
	"log"
	"net"
	"net/http"
	"strings"
	"time"
)

const (
	PORT = 12000
	HOST = "localhost"
)

type Message struct {
	Id   int
	Text string
}

func GetOutboundIP() string {
	conn, err := net.Dial("udp", "8.8.8.8:80")
	if err != nil {
		log.Fatal(err)
	}
	defer conn.Close()

	localAddr := conn.LocalAddr().String()
	idx := strings.LastIndex(localAddr, ":")

	return localAddr[0:idx]
}

func GetMsg(w http.ResponseWriter, r *http.Request) {
	startTime := time.Now()
	defer func() {
		timeTaken := time.Since(startTime)
		log.Printf("Time for %s is %s\n", r.URL.Path, timeTaken)
	}()

	w.Header().Set("Content-Type", "application/json")
	w.Write([]byte("{}"))
}

func PostMsg(w http.ResponseWriter, r *http.Request) {
	startTime := time.Now()
	defer func() {
		timeTaken := time.Since(startTime)
		log.Printf("Time for %s is %s\n", r.URL.Path, timeTaken)
	}()

	_, err := io.Copy(ioutil.Discard, r.Body)
	if err != nil {
		log.Fatal("error in reading input", err)
		panic(err)
	}
	w.WriteHeader(http.StatusOK)
}

func main() {
	http.HandleFunc("/message", GetMsg)
	http.HandleFunc("/messages", PostMsg)

	ipAddr := "localhost"//GetOutboundIP()
	log.Printf("IpAddr is %s\n", ipAddr)

	var listen string = fmt.Sprintf("%s:%d", ipAddr, PORT)
	log.Printf("Starting the server at %s\n", listen)

	log.Fatal(http.ListenAndServe(listen, nil))
}
