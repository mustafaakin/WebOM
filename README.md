WebOM
=====

It maps HTTP requests to Java Objects so you do not have to worry about type conversions and null checking yourself. Also supports websockets!

_This project is still Work in progress, any suggestions are welcome and using it production environment is highly discouraged._

## Example:

### Intialization

```java
WebOM server = new WebOM(5000, "webom.examples");
server.setStaticFileLocation("public");
server.start();
```

### Just plain sample request handler

```java
  @Handler(method = RequestMethod.GET, path = "/hello")
  public class HelloWorld extends HTTPRequestHandler {
  
  	@Override
  	public Object handle(Request request, Response response) {
  		return "hi";
  	}
  }
```

### URL Parameters sample

```java
  @Handler(method = RequestMethod.GET, path = "/hello/:person")
  public class ReqHandler2 extends HTTPRequestHandler {
  	@NotNull
  	@Param
  	public String person;
  
  	@Override
  	public Object handle(Request request, Response response) {
  		return "hello " + person;
  	}
  
  }
```

### WebSocket Example!

```java
  @Handler(method = RequestMethod.WEBSOCKET, path = "/terminal/:termId")
  public class TerminalHandler extends WebSocketHandler {
  		
  	private static Logger logger = LoggerFactory.getLogger(TerminalHandler.class);
  
  	@NotNull
  	@Param
  	public String termId;
  
  	@Override
  	public void onWebSocketBinary(byte[] payload, int offset, int len) {
  
  	}
  
  	@Override
  	public void onWebSocketClose(int statusCode, String reason) {
  		logger.info("Web socket closed, code: {} reason: {}", statusCode, reason);
  	}
  
  	@Override
  	public void onWebSocketConnect() {
  		logger.info("Websocket connected");
  		send("Hello, it seems you want the terminal: " + termId);
  	}
  
  	@Override
  	public void onWebSocketError(Throwable cause) {
  		cause.printStackTrace();
  	}

  	@Override
  	public void onWebSocketText(String message) {
  		logger.info("Recieved ws message: {}", message);
  	}
  }
```

## TODO:

  - Allow file uploads to temporary directory and size restrictions
  - Decide session implementation
  - More validation rules, regex es etc.
  - Implement FileSession and RedisSession 
  - Better documentation
  
  
If you have anything to share you can do it via opening an issue or shooting me an e-mail at mustafa91@gmail.com
