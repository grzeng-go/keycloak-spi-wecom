# 认证中心

1. [keycloak](https://www.keycloak.org/)

2. [justauth](https://www.justauth.cn/)

## 构建

```
make image
make push
```

## 启动

```
docker-compose up -d
```

访问地址 `localhost:8080`， 默认账号是 `admin/admin`

由于真实环境中会有第三方登录，比如 `钉钉登录，企业微信登录`，会涉及到回调域名，所以一般会使用 `nginx` 做反向代理，通过一个二级域名进行访问应用，比如 `http://account.example.org`，这样会比较方便。

## 钉钉登录

### 创建钉钉企业内部应用

1. 登录 [钉钉开放平台](https://open-dev.dingtalk.com/)

2. 创建钉钉企业内部应用

    ![ding-app.jpg](images/ding-app.jpg)

3. 查看基础信息

    ![base.jpg](images/base.jpg)

4. 修改开发管理，填写服务器出口IP和首页地址 `http://account.example.org/auth/realms/palan/account`

    ![dev.jpg](images/dev.jpg)

5. 填写接入登录回调域名 `http://account.example.org/auth/realms/palan/broker/ding_talk/endpoint`

    ![callback.jpg](images/callback.jpg)

### 创建钉钉身份提供者

1. 登录 [认证中心管理页面](http://account.example.org/auth/admin)

2. 进入身份提供者列表页面，添加钉钉

    ![provider.jpg](images/provider.jpg)

3. 填写相应配置，`Client ID=AppKey, Client Secret=AppSecret`

    ![dingtalk.jpg](images/dingtalk.jpg)

4. 如果还需要查询钉钉企业成员信息，这个我们就可以通过转换起来实现，比如我们可以根据钉钉通讯录中自定义用户名来作为统一的账户名，头像信息等等

    ![mapper.jpg](images/mapper.jpg)

    这里特别要注意的点是 `User Json Path Attribute Name` 这里的配置内容 `username=$['extension']['用户名'],avatar=$['avatar'],email=$['org_email'],org_email=$['org_email'],name=$['name'],mobile=$['mobile']`

    其实就是根据 `,` 做分割，然后再根据 `=` 做分割等到的第一个的值作为用户属性的 `key`, 第二个值为 `Json Path` 表达式，然后再根据钉钉企业成员信息解析后提取用户属性

    假设请求钉钉企业成员详细接口后返回的内容如果是以下的内容的话

    ```json
    {
      "errcode": 0,
      "errmsg": "ok",
      "result": {
        "active": true,
        "admin": true,
        "avatar": "https:\/\/static-legacy.dingtalk.com\/media\/lADOCnFNdM0Cns0C7g_750_670.jpg",
        "boss": false,
        "dept_id_list": [
          1
        ],
        "dept_order_list": [
          {
            "dept_id": 1,
            "order": 176322897889314512
          }
        ],
        "email": "515265763@qq.com",
        "exclusive_account": false,
        "extension": "{\"用户名\":\"yuanzhencai\"}",
        "hide_mobile": false,
        "job_number": "",
        "leader_in_dept": [
          {
            "dept_id": 1,
            "leader": false
          }
        ],
        "mobile": "18321718279",
        "name": "袁振才",
        "org_email": "zhencai.yuan@datarx.cn",
        "real_authed": true,
        "remark": "",
        "role_list": [
          {
            "group_name": "默认",
            "id": 1507668010,
            "name": "主管理员"
          }
        ],
        "senior": false,
        "state_code": "86",
        "telephone": "",
        "title": "",
        "union_emp_ext": {},
        "unionid": "4L5xxXJOwj3iSGJpwa50qYgiEiE",
        "userid": "033156401534394431",
        "work_place": ""
      },
      "request_id": "71shv9eao41d"
    }
    ```

    最终这个用户创建的属性为以下内容

    ```text
    username=yuanzhencai
    avatar=https:\/\/static-legacy.dingtalk.com\/media\/lADOCnFNdM0Cns0C7g_750_670.jpg
    email=515265763@qq.com
    org_email=zhencai.yuan@datarx.cn
    name=袁振才
    mobile=18321718279
    ```

    `extension` 里的值是钉钉企业通讯录里通过 [创建自定义字段](https://oa.dingtalk.com/index_new.htm#/setting/contactInfo)，然后为每个 [成员信息](https://oa.dingtalk.com/hrmregister/web/index#/empManage/onJob) 中设置这个字段的内容后得到的，比如现在我创建的这个 `用户名` 字段，就是代表钉钉用户的扫码登录认证中心后的统一用户名

    ![extension.jpg](images/extension.jpg)

    ![member.jpg](images/member.jpg)
