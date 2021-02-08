FROM  jboss/keycloak:12.0.1

## copy themes
COPY themes /opt/jboss/keycloak/themes

## copy jar
COPY  target/keycloak-justauth-12.0.1-jar-with-dependencies.jar /opt/jboss/keycloak/providers/
