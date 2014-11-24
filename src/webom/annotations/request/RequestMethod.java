package webom.annotations.request;

public enum RequestMethod {
	GET("GET"), POST("POST"), PUT("PUT"), WEBSOCKET("WEBSOCKET");

	public String str;

	RequestMethod(String str) {
		this.str = str;
	}
}
