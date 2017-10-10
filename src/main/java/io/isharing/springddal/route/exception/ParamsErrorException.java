package io.isharing.springddal.route.exception;

public class ParamsErrorException extends RuntimeException {

	private static final long serialVersionUID = 2296896317900237622L;

	public ParamsErrorException() {
        super();
    }

    public ParamsErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParamsErrorException(String message) {
        super(message);
    }

    public ParamsErrorException(Throwable cause) {
        super(cause);
    }

}