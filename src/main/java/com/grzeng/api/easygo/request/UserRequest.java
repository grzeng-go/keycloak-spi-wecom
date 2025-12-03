package com.grzeng.api.easygo.request;

import com.google.gson.annotations.SerializedName;
import jakarta.ws.rs.QueryParam;

/**
* Generated from JSON:
    {"action":"syncPassword",
        "token":"token",
        "userId":"userId",
        "password":"password",
        "sign":"sign"}
*/
public class UserRequest {
    @SerializedName("action")
    @QueryParam("action")
    private String action;

    @SerializedName("token")
    @QueryParam("token")
    private String token;

    @SerializedName("username")
    @QueryParam("username")
    private String username;

    @SerializedName("password")
    @QueryParam("password")
    private String password;

    @SerializedName("sign")
    @QueryParam("sign")
    private String sign;

    public String getAction() {
    return action;
    }

    public String getToken() {
    return token;
    }

    public String getUsername() {
    return username;
    }

    public String getPassword() {
    return password;
    }

    public String getSign() {
    return sign;
    }

    public void setAction(String action) {
    this.action = action;
    }

    public void setToken(String token) {
    this.token = token;
    }

    public void setUsername(String username) {
    this.username = username;
    }

    public void setPassword(String password) {
    this.password = password;
    }

    public void setSign(String sign) {
    this.sign = sign;
    }

}