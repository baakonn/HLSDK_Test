package com.ihuilian.volley;

import java.io.File;
import java.io.Serializable;

/** 
 * @author linjinfa 331710168@qq.com 
 * @version 创建时间：2014年10月17日 下午3:23:12 
 */
public class FormFile implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private String fileParamName;
	private File uploadFile;
	public FormFile() {
		super();
		// TODO Auto-generated constructor stub
	}
	public FormFile(String fileParamName, File uploadFile) {
		super();
		this.fileParamName = fileParamName;
		this.uploadFile = uploadFile;
	}
	public String getFileParamName() {
		return fileParamName;
	}
	public void setFileParamName(String fileParamName) {
		this.fileParamName = fileParamName;
	}
	public File getUploadFile() {
		return uploadFile;
	}
	public void setUploadFile(File uploadFile) {
		this.uploadFile = uploadFile;
	}
	
}
