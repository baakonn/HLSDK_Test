package com.ihuilian.entity;

/**
 * 响应数据
 * @author linjinfa 331710168@qq.com 
 * @version 创建时间：2014年10月24日 下午5:27:28 
 */
public class ResponseObj extends MSG{

	private static final long serialVersionUID = 1L;
	
	private String status;
	private String message;
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
}
