package webom.response;

public class Message {
	private int code;
	private String message;

	public Message(int code, String message) {
		this.code = code;
		this.message = message;
	}

	public int getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	public static Message AuthError() {
		return new Message(401, "You are not allowed to see here");
	}

	public static Message InternalError() {
		return new Message(500, "An internal error has occured");
	}
	
	public static Message NotFound() {
		return new Message(404, "The requested content could not be found");		
	}
}
