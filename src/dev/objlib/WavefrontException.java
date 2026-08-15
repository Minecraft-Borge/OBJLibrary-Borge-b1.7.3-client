package dev.objlib;

public class WavefrontException extends RuntimeException {
	public WavefrontException() {
		super();
	}

	public WavefrontException(String message) {
		super(message);
	}

	public WavefrontException(String message, Throwable cause) {
		super(message, cause);
	}

	public WavefrontException(Throwable cause) {
		super(cause);
	}

	protected WavefrontException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
