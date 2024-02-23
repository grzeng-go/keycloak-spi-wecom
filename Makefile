image:
	mvn clean package
	docker build . -t registry.cn-shanghai.aliyuncs.com/happiness-frog/keycloak:12.0.1

push: image
	docker push registry.cn-shanghai.aliyuncs.com/happiness-frog/keycloak:12.0.1
