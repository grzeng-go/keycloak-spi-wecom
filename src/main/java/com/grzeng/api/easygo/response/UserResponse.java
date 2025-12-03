package com.grzeng.api.easygo.response;

import com.google.gson.annotations.SerializedName;

/**
* Generated from JSON:
    {"panform-json":{"coustom":{"msg":"成功","success":true},"type":"info"}}
*/
public class UserResponse {
    @SerializedName("panform-json")
    private PanformJson panformJson;

    public PanformJson getPanformJson() {
    return panformJson;
    }

    public void setPanformJson(PanformJson panformJson) {
    this.panformJson = panformJson;
    }

    public static class PanformJson {
        @SerializedName("coustom")
        private Coustom coustom;

        @SerializedName("type")
        private String type;

        public Coustom getCoustom() {
        return coustom;
        }

        public String getType() {
        return type;
        }

        public void setCoustom(Coustom coustom) {
        this.coustom = coustom;
        }

        public void setType(String type) {
        this.type = type;
        }

    }
    public static class Coustom {
        @SerializedName("msg")
        private String msg;

        @SerializedName("success")
        private Boolean success;

        @SerializedName("status")
        private String status;

        public String getMsg() {
        return msg;
        }

        public Boolean getSuccess() {
        return success;
        }

        public void setMsg(String msg) {
        this.msg = msg;
        }

        public void setSuccess(Boolean success) {
        this.success = success;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}