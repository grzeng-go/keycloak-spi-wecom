image:
	docker build . -t registry.cn-shanghai.aliyuncs.com/happiness-frog/keycloak:23.0.7

push: image
	docker push registry.cn-shanghai.aliyuncs.com/happiness-frog/keycloak:23.0.7
