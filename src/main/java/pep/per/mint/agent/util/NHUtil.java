package pep.per.mint.agent.util;


import java.io.BufferedReader;
import java.io.File;
//import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import pep.per.mint.common.util.StringUtil;


public class NHUtil {


	public static String readFromInputStream(InputStream inputStream)	throws IOException {
		StringBuilder resultStringBuilder = new StringBuilder();
		try{
			BufferedReader br
				= new BufferedReader(new InputStreamReader(inputStream));
			String line;
			while ((line = br.readLine()) != null) {
				resultStringBuilder.append(line).append("\n");
			}
		}catch(Exception e){
			throw new IOException(e);
		}
		return resultStringBuilder.toString();
	}

	public static String getMQVersion(){
		String osname =  System.getProperty("os.name").toLowerCase();
		InputStream is;
		StringWriter w;
		int x,y;
		String outpath,line;
		BufferedReader br;

		String version ="N/A";
		Process p = null;
		try {
			p = Runtime.getRuntime().exec("dspmqver");
			is = p.getInputStream();
			w = new StringWriter();
			while((x=is.read())!= -1) w.write(x);
			outpath = w.toString();
			br = new BufferedReader(new StringReader(outpath));

			while((line = br.readLine()) != null){
				line = line.trim();
				if(line.startsWith("Version")){
					y = line.indexOf(":");
					version = line.substring(y+1).trim();
				}
			}
		} catch (IOException e) {
			//			e.printStackTrace();
		} catch(Exception e){
			//			e.printStackTrace();
			//			System.out.println(e.getMessage());
		}finally{
			if(p != null) p.destroy();
		}

		return version;
	}

	public static String getMQVersion(String binaryPath){
		String osname =  System.getProperty("os.name").toLowerCase();
		InputStream is;
		StringWriter w;
		int x,y;
		String outpath,line;
		BufferedReader br;

		String version ="N/A";
		Process p = null;

		String cmd =binaryPath +File.separatorChar+"dspmqver";

		try {
			p = Runtime.getRuntime().exec(cmd);
			is = p.getInputStream();
			w = new StringWriter();
			while((x=is.read())!= -1) w.write(x);
			outpath = w.toString();
			br = new BufferedReader(new StringReader(outpath));

			while((line = br.readLine()) != null){
				line = line.trim();
				if(line.startsWith("Version")){
					y = line.indexOf(":");
					version = line.substring(y+1).trim();
				}
			}
		} catch (IOException e) {
			//			e.printStackTrace();
		} catch(Exception e){
			//			e.printStackTrace();
			//			System.out.println(e.getMessage());
		}finally{
			if(p != null) p.destroy();
		}

		return version;
	}

	public static String getMTEVersion(Map params){
		String adtPath = (String)params.get("adtPath");
		String osname =  System.getProperty("os.name").toLowerCase();
		InputStream is;
		StringWriter w;
		int x,y;
		String outpath,line;
		BufferedReader br;

		String version ="N/A";
		Process p = null;
		try {
			p = Runtime.getRuntime().exec(adtPath +" --version");
			is = p.getInputStream();
			w = new StringWriter();
			while((x=is.read())!= -1) w.write(x);
			outpath = w.toString();
			br = new BufferedReader(new StringReader(outpath));

			while((line = br.readLine()) != null){
				line = line.trim();
				if(line.startsWith("Build Version")){
					y = line.indexOf(":");
					version = line.substring(y+1).trim();
					String[] arr = StringUtil.strToStrArray(version, " ");
					version = arr[arr.length-1];
				}
			}
		} catch (IOException e) {
			//			e.printStackTrace();
		} catch(Exception e){
			//			e.printStackTrace();
			//			System.out.println(e.getMessage());
		}finally{
			if(p != null) p.destroy();
		}

		return version;
	}


	/**
	 * Tmax Adapter 버전 정보 조회
	 * 	version 정보 파일 내부 포
	 *  ${applicationNm}_XXXXX=2018090000000
	 * @param params
	 * @return
	 */
	public static String getTmaxVersion(Map params) {
		String version = "N/A";
		BufferedReader br = null;
		try {
			String adaterPath = (String)params.get("adtPath");

			if(adaterPath == null || adaterPath.trim().length() == 0) return version;


			String applicationNm = (String)params.get("applicationNm");


			File versionFile = new File(adaterPath);
            br = new BufferedReader(new FileReader(versionFile));
            String line = "";
            while((line = br.readLine()) != null){
            	if(line.trim().contains(applicationNm)) {
            		int startIndex = line.indexOf("=");
            		version = line.substring(startIndex + 1).trim();
            	}
            }

		}catch(Throwable t) {
			t.printStackTrace();
			version ="notFoundVersion(" + t.getMessage() + ")";
		} finally {
			if(br != null)
				try {
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return version;
	}

}
