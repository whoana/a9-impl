package pep.per.mint.agent.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// import org.springframework.stereotype.Service;

import pep.per.mint.agent.exception.AgentException;

//@Service
public class FileUploadService {

	Logger logger = LoggerFactory.getLogger(FileUploadService.class);

	public void uploadFil(String dest, String contents) throws AgentException{

		byte[] myByteArray = contents.getBytes();
		try {
			FileUtils.writeByteArrayToFile(new File(dest), myByteArray);
		} catch (IOException e) {
			logger.warn(e.getMessage() , e);
			throw new AgentException("1001", e.getMessage(), e.getMessage());
		}

	}

	public byte[] getFileContents(String filePath) throws AgentException{
		byte[] rData = null;
		File file = new File(filePath);
		try {
			rData = FileUtils.readFileToByteArray(file);
		} catch (FileNotFoundException e) {
			logger.warn(e.getMessage() , e);
			throw new AgentException("1001", e.getMessage(), e.getMessage());
		} catch (IOException e) {
			logger.warn(e.getMessage() , e);
			throw new AgentException("1001", e.getMessage(), e.getMessage());
		}
		return  rData;
	}

	public List<String> getConfigFile(String filePath, String fileType) throws AgentException {

		List<String> list = new ArrayList();
		if("JAVA".equalsIgnoreCase(fileType)){
			list = getXMLConfigParse(filePath);
		}else if("API".equalsIgnoreCase(fileType) || "TMAX".equalsIgnoreCase(fileType)){
			list = getINIConfigParse(filePath);
		}

		return list;
	}

	private List<String> getINIConfigParse(String filePath) throws AgentException {
		INIConfiguration  iniconf = new INIConfiguration();
		List<String> list = new ArrayList<String>();
		try {
			iniconf.read(new BufferedReader(new InputStreamReader(new FileInputStream(new File(filePath)))));

			Set setOfSections = iniconf.getSections();
			Iterator sectionNames = setOfSections.iterator();

			while(sectionNames.hasNext()){
				String sectionName = sectionNames.next().toString();
				list.add(sectionName);
			}
		} catch (FileNotFoundException e) {
			throw new AgentException(e.getMessage(), e);
		} catch (ConfigurationException e) {
			throw new AgentException(e.getMessage(), e);
		} catch (IOException e) {
			throw new AgentException(e.getMessage(), e);
		}

		return list;
	}

	private List<String> getXMLConfigParse(String filePath) throws AgentException {
		List<String> list = new ArrayList<String>();
		try {
			Parameters params = new Parameters();
			FileBasedConfigurationBuilder<XMLConfiguration> fileBuilder =
					new FileBasedConfigurationBuilder(XMLConfiguration.class);
			fileBuilder.configure(params.fileBased().setFileName(filePath));

			XMLConfiguration xmlconf = fileBuilder.getConfiguration();

			List<String> list3 =  xmlconf.getList(String.class , "Interface[@id]");
			for (String field : list3) {
				list.add(field);
			}

		} catch (ConfigurationException e) {
			throw new AgentException(e.getMessage(), e);
		}

		return list;
	}

}
