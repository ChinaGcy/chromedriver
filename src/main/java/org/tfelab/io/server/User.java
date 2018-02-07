package org.tfelab.io.server;

public interface User {

	boolean isValid();

	boolean isEnabled();

	String getPrivateKey();
}
