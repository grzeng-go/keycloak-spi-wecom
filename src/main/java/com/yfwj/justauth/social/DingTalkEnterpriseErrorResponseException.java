package com.yfwj.justauth.social;

import org.keycloak.broker.provider.IdentityBrokerException;

public class DingTalkEnterpriseErrorResponseException extends IdentityBrokerException {

	public DingTalkEnterpriseErrorResponseException(String message) {
		super(message);
	}
}
