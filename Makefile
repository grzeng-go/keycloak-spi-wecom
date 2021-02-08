image:
	docker build . -t registry.cn-hangzhou.aliyuncs.com/palan/account:12.0.1

push: image
	docker push registry.cn-hangzhou.aliyuncs.com/palan/account:12.0.1
