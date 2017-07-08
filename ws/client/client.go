package client

import (
	"encoding/json"
	"golang.org/x/net/websocket"
	"log"
)

type Message struct {
	Org int `json:"org"`             // 归属组织
	Topic int `json:"topic"`         // 主题
	Content string `json:"content"`  // 内容
}

type WebSocket struct {
	Stop chan bool
	conn *websocket.Conn
}

const DEFAULT_ORIGIN = "http://127.0.0.1"
func NewWebSocket(uri string) (ws *WebSocket, err error)  {
	config, err :=websocket.NewConfig(uri, DEFAULT_ORIGIN)
	if err != nil {
		log.Fatal(err)
		return
	}
	conn, err := websocket.DialConfig(config)
	if err != nil {
		log.Fatal(err)
		return
	}
	ws = &WebSocket{make(chan bool),conn}
	return
}

func (ws *WebSocket) Close() (err error) {
	if ws.conn != nil {
		err = ws.conn.Close()
		if err != nil {
			log.Fatal(err)
			return
		}
	}
	if ws.Stop != nil {
		close(ws.Stop)
	}
	return
}

func (ws *WebSocket) Send(message string) (err error)  {
	inbound := []byte(message)
	log.Printf("Send: %s\n", inbound)
	_, err = ws.conn.Write(inbound)
	if err != nil {
		log.Fatal(err)
	}
	return
}

func (ws *WebSocket) SendJson(ref interface{}) (err error) {
	inbound, _ := json.Marshal(ref)
	log.Printf("Send: %s\n", inbound)
	err = websocket.JSON.Send(ws.conn, ref)
	if err != nil {
		log.Fatal(err)
	}
	return
}

func (ws *WebSocket) Receive() string {
	defer func() {
		ws.Stop <- true
	}()

	buffer := make([]byte, 2048)
	m, err := ws.conn.Read(buffer)
	if err != nil {
		log.Fatal(err)
		return ""
	}
	outbound := buffer[:m]
	log.Printf("Receive: %s\n", outbound)
	return string(outbound)
}

func (ws *WebSocket) ReceiveJson(ref interface{}) {
	defer func() {
		ws.Stop <- true
	}()

	err := websocket.JSON.Receive(ws.conn, ref)
	if err != nil {
		log.Fatal(err)
		return
	}
	outbound, _ := json.Marshal(ref)
	log.Printf("Receive: %s\n", outbound)
	return
}

func (ws *WebSocket) OnMessage(handler IMessageHandler) {
	for {
		var msg Message
		go ws.ReceiveJson(&msg)
		<- ws.Stop
		if handler != nil {
			handler.Exec(&msg)
		}
	}
}
