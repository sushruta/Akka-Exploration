Running
--------
```
$ sbt run
```

Sending Messages
-----------------

I use this chrome extension for sending messages to a websocket - [dwst](https://chrome.google.com/webstore/detail/dark-websocket-terminal/dmogdjmcpfaibncngoolgljgocdabhke/related)

```
/connect ws://localhost:8888/greeter
```

Here's a log from my console --

```
16:29:46	command:	/connect ws://localhost:8888/greeter
16:29:46	system:	Connecting to ws://localhost:8888/greeter
No protocol negotiation.
16:29:47	system:	Connection established.
16:30:07	command:	/send sashidhar
16:30:07	sent:	sashidhar
16:30:07	received:	Hello sashidhar
16:31:09	system:	Connection closed.
```
