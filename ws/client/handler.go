package client

import "log"

type IMessageHandler interface {
	Exec(message *Message) error
}

type TransportMessageToCC struct {
}

func (t2CC *TransportMessageToCC) Exec(message *Message) error { // TODO
	if message == nil {
		return nil
	}

	log.Printf("send message“%s”to CC", message.Content)
	return nil
}
