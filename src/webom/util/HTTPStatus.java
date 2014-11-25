package webom.util;

public interface HTTPStatus {
	public final static int OK = 200;
	public final static int MOVED_PERMANENTLY = 301;

	public final static int BAD_REQUEST = 400;
	public final static int UNAUTHORIZED = 401;
	public final static int FORBIDDEN = 403;
	public final static int NOT_FOUND = 404;

	public final static int INTERNAL_SERVER_ERROR = 500;
}
