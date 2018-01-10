package com.choice.http;
import com.choice.utils.StatusCodeUtils;

import java.io.*;
import java.net.Socket;
//http协议的响应
public class HttpResponseImpl  implements  HttpResponse {
	 //声明初始变量
	Socket s;//客户端socket
	OutputStream os;//输出端字节流
	BufferedWriter bw;//输出端字符流
	PrintWriter pw;
	StringBuffer sbuffer;//放状态行，\r\n ,
	FileInputStream fis;
	File file;
	//构造器
	public HttpResponseImpl(Socket s) {
                    this.s=s;
                    os=null;
                    bw=null;
                    pw=null;
                    sbuffer=new StringBuffer();//初始化，记得，否则以下的操作会遇见空指针异常
                    fis=null;
                    file=null;   			
                    getInfos();
	}
	  
	private void getInfos() {
			try {
				os=s.getOutputStream();
				bw=new BufferedWriter(new OutputStreamWriter(os,"GBK"));
				pw=new PrintWriter(bw);
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
	//获得一个指向客户端的字节流
	public OutputStream getOutputStream()throws Exception
	{
		return os;
	}
	//获得一个指向客户端的字符流
	public PrintWriter getPrintWriter()throws Exception
	{
		return pw;
	}
	//设置响应的状态行 参数为String类型
	public void setStatusLine(String statusCode)
	{
		String str=StatusCodeUtils.getStatusCodeValue(statusCode);
		sbuffer.append("HTTP/1.1 "+statusCode+" "+str);
		setCRLF();
	}
	//设置响应的状态行 参数为int类型
	public void setStatusLine(int statusCode)
	{
		setStatusLine(statusCode+"");//将int类型转化为String类型
	}
	//设置响应消息报头
	public void setResponseHeader(String hName,String hValue)
	{
		sbuffer.append(hName+": "+hValue);
		setCRLF();
	}
	//设置响应消息报头中Content-Type属性
	public void setContentType(String contentType)
	{
		setResponseHeader("Content-Type",contentType);
	}
	//设置响应消息报头中Content-Type属性 并且同时设置编码
	public void setContentType(String contentType,String charsetName)
	{//text/html;charset=utf-8
		setContentType(";charset= "+charsetName);
	}
	//设置CRLF 回车换行  \r\n
	public void setCRLF()
	{
		sbuffer.append("\r\n");
	}
	//把设置好的响应状态行、响应消息报头、固定空行这三部分写给浏览器
	public void printResponseHeader()
	{
		//设置setResponseLine,setResponseHeader,setResponseType
		String res=sbuffer.toString();
		pw.print(res);
		pw.flush();
	}
	//把响应正文写给浏览器
	public void printResponseContent(String requestPath)
	{
		printResponseHeader();
		//响应正文
		String getPath= requestPath;//客户请求的地址
		String webHome=System.getProperty("user.dir")+File.separator+"navigationHtml"+File.separator;
		String parentPath = webHome+requestPath.substring(1);

		file=new File(parentPath);
		if(file.exists())//如果文件存在就执行
		{
		try {
			fis=new FileInputStream(file);
			byte[] buf=new byte[1024];
			int len=-1;
			while((len=fis.read(buf))!=-1)
			{
				os.write(buf, 0, len);
			}
			bw.flush();
			os.flush();//os要不要关？？？
		} catch (IOException e) {
			e.printStackTrace();
		}finally
		{
			try {
				if(bw!=null)
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		}
	}

	//把响应的数据给前台
	public void printResponseData(String data)
	{
		printResponseHeader();
		//响应数据
		try {
			os.write(data.getBytes("UTF-8"));
			os.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//转发
	@Override
	public void sendRedirect(String pathName) {
		sbuffer = new StringBuffer();
		this.setStatusLine(302);
		this.setContentType("text/HTML");
		sbuffer.append("Location: " + pathName);
		this.setCRLF();
		this.printResponseHeader();
	}

	//向浏览器发地址



}