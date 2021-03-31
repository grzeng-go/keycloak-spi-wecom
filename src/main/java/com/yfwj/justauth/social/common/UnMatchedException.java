package com.yfwj.justauth.social.common;

import org.keycloak.broker.provider.IdentityBrokerException;

public class UnMatchedException extends IdentityBrokerException {

	public UnMatchedException(String message) {
		super(message);
	}
}
