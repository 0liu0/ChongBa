package com.liuche.common.entity;

import lombok.Data;

@Data
public class ResponseMessage {
    private boolean flag;
    private Integer code;
    private String message;
    private Object data;
    private String url;

    public ResponseMessage() {
    }

    public ResponseMessage(boolean flag, Integer code, String message, Object data) {
        this.flag = flag;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public ResponseMessage(boolean flag, Integer code, String message) {
        this.flag = flag;
        this.code = code;
        this.message = message;
    }
    
    public static ResponseMessage ok(Object data) {
		return new ResponseMessage(true, com.liuche.common.entity.StatusCode.OK,"success",data);
    }
    
    public static ResponseMessage error(Object data) {
  		return new ResponseMessage(true,StatusCode.ERROR,"fail",data);
      }
}
