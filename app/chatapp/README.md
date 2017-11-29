Ok, since the frontend is not ready, we will use a websocket client to connect to the server. You can slum it out doing it on cmd line or use a nifty websocket extension that chrome provides -> https://chrome.google.com/webstore/detail/dark-websocket-terminal/dmogdjmcpfaibncngoolgljgocdabhke?hl=en

Now, clone the project and go to `app/chatapp`

```
$ sbt run
```

We have the server running. Create two instances of the websocket client mimicking the use case of two users.

Have them connect to the same channel by doing something like this --

`/connect ws://localhost:8888/ws-chat/<channelId>?name=<username>`

I did something like this ... 

```
/connect ws://localhost:8888/ws-chat/12345?name=sushruta
```

use another name for the other user in the other websocket window --

```
/connect ws://localhost:8888/ws-chat/12345?name=aryabhatta
```

chat away!
