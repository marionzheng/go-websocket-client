package main

import (
	"github.com/marionzheng/go-websocket-client/ws/client"
)

func main() {
	ws, err := client.NewWebSocket("ws://127.0.0.1:8080/sock/notify")
	if err != nil {
		return
	}
	defer ws.Close()

	ws.Send("golang")

	var handler client.IMessageHandler = new(client.TransportMessageToCC)
	ws.OnMessage(handler)
}
