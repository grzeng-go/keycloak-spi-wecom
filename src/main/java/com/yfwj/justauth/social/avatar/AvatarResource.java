package com.yfwj.justauth.social.avatar;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.List;

public class AvatarResource {

	protected static final Logger logger = Logger.getLogger(AvatarResource.class);

	private static final String AVATAR_ATTRIBUTE_NAME = "AVATAR_ATTRIBUTE_NAME";

	private static final String DEFAULT_AVATAR = "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";

	protected KeycloakSession session;

	public AvatarResource(KeycloakSession session) {
		this.session = session;
	}

	@GET
	@NoCache
	@Path("/")
	@Produces({"image/png", "image/jpeg", "image/gif"})
	public Response getUserAvatar(@QueryParam("search") String search) {
		String avatarAttributeName = System.getProperty(AVATAR_ATTRIBUTE_NAME, "avatar");
		try {
			RealmModel realm = session.getContext().getRealm();
			List<UserModel> users = session.users().searchForUser(search, realm);
			if (!users.isEmpty()) {
				String avatar = users.get(0).getFirstAttribute(avatarAttributeName);
				if (StringUtils.isNotEmpty(avatar)) {
					URI uri = new URI(avatar);
					return Response.temporaryRedirect(uri).build();
				}
			}
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
		}
		return Response.ok(new ByteArrayInputStream(defaultAvatar())).build();
	}

	private byte[] defaultAvatar() {
		return Base64.decodeBase64(DEFAULT_AVATAR);
	}
}
