
具体使用看Dockerfile

justauth  http://www.justauth.cn/

docker 启动
docker run -p 8080:8080 -e KEYCLOAK_USER=admin -e KEYCLOAK_PASSWORD=admin registry.cn-hangzhou.aliyuncs.com/yfwj/yfwj-keycloak:12.0.1

docker network create keycloak
docker run -p 80:8080 --name keycloak\
  -e KEYCLOAK_USER=admin \
  -e KEYCLOAK_PASSWORD=admin \
  -e DB_VENDOR=mysql \
  -e DB_ADDR=keycloak-mysql \
  -e DB_PORT=3306 \
  -e DB_DATABASE=keycloak \
  -e DB_USER=root \
  -e DB_PASSWORD=keycloak-root \
  --network keycloak \
  registry.cn-hangzhou.aliyuncs.com/yfwj/yfwj-keycloak:12.0.1

docker run -d -p 3306:3306 --name keycloak-mysql \
  -e MYSQL_ROOT_PASSWORD=keycloak-root  \
  -e MYSQL_DATABASE=keycloak \
  --network keycloak \
  mysql:5.7


## 钉钉

1. 扫码登录

```
https://oapi.dingtalk.com/connect/qrconnect?response_type=code&appid=dingoaxdkfspbtihv2ohrj&scope=snsapi_login&redirect_uri=http://192.168.3.24:4080/auth/realms/palan/broker/ding_talk_enterprise/endpoint&state=rNyTWfTLKO3KitQqfBU9I6UeLaCa8P36vyLqdCh9NaA.xM_c9XvVmWQ.jira
```

2. 钉钉内免登陆

```
https://oapi.dingtalk.com/connect/qrconnect?response_type=code&appid=dingoaxdkfspbtihv2ohrj&scope=snsapi_login&redirect_uri=http://192.168.3.24:4080/auth/realms/palan/broker/ding_talk_enterprise/endpoint&state=Yr8XllPvI5lqdD82BDf4fNPpSwvpn4YVcJwrxtnCBQk.fDQcGofG1VY.jira
https://oapi.dingtalk.com/connect/qrconnect?response_type=code&appid=dingoaxdkfspbtihv2ohrj&scope=snsapi_login&redirect_uri=http://192.168.3.24:4080/auth/realms/palan/broker/ding_talk_enterprise/endpoint&state=rjW8Hf3RU2IfOKg4FPOADv8WZu7sHDqkvugqLcWO9CA.lTACsTVil5Q.jira
https://oapi.dingtalk.com/connect/oauth2/sns_authorize?appid=dingoaxdkfspbtihv2ohrj&response_type=code&scope=snsapi_auth&state=rjW8Hf3RU2IfOKg4FPOADv8WZu7sHDqkvugqLcWO9CA.lTACsTVil5Q.jira&redirect_uri=http://192.168.3.24:4080/auth/realms/palan/broker/ding_talk_enterprise/endpoint
```
