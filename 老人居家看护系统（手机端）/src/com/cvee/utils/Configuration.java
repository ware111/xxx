package com.cvee.utils;

/**
 * 描述：配置信息 包括基本的静态常量信息和指令集 
 */
public class Configuration {
	
	
	public static boolean isRun = true;//控制读串口的数据
	public static boolean isDefend = true;//是否布防
	/**
	 * 描述：socket通信配置信息
	 */
	public final static String ZD_IP = "192.168.1.101";//终端的IP
	public final static int ZD_PORT = 8080;//终端端口
	/**
	 * 描述：摄像机网络配置信息
	 */
	public static String wip = "192.168.1.250";//网关IP
	public static String jip = "192.168.1.170";//监控网络IP
	public static String port = "1024";//摄像头网络端口号
	public static String name = "admin";//用户名
	public static String password = "123456";//密码
	public final static String IPCAMERA_NAME = "";//该连接的名称
	public final static int IPCAMERA_TIME = 100;//音频缓冲时间	
	public final static int wport = 5000;//网关端口号
}
