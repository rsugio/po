package com.differencer.pi;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import org.eclipse.core.runtime.Status;
import com.differencer.pi.editors.Server;
import com.differencer.pi.nodes.ConfigurationNode;
import com.sap.aii.ibtransportclient.StartRepAppWebClient;
import com.sap.aii.ibtransportclient.Transport;
import com.sap.aii.ibtransportclient.XiTransportClientProvider;
import com.sap.aii.ibtransportclient.XiTransportException;
import com.sap.aii.ibtransportclient.XiTransportTestClient;
import com.sap.aii.util.misc.api.IOUtil;
import com.sap.aii.util.misc.api.ResourceException;
public class Exporter extends Transport {
	public Exporter() {
	}
	public static void exportTransportToClient(HashMap<ConfigurationNode, ConfigurationNode> elements, String transportDirectory) {
		for (ConfigurationNode element : elements.values()) {
			XiTransportTestClient transportClient = null;
			try {
				transportClient = XiTransportClientProvider.getXiTransportTestClient(new URL(element.getServer().getURL() + element.getTransportSuffix()), element.getServer().getUSER(), element.getServer().getPASSWORD());
			} catch (ResourceException e) {
				Activator.log(Status.ERROR, "exportTransportToClient failed", e);
			} catch (MalformedURLException e) {
				Activator.log(Status.ERROR, "exportTransportToClient failed", e);
			}
			String swcvId = element.getSWCVID();
			String objectId = element.getOBJECTID();
			String objectName = element.getOBJECTNAME();
			String objectType = element.getOBJECTTYPE();
			String nameSpace = element.getOBJECTNAMESPACE();
			int spLabel = -1;
			boolean anonymize = false;
			InputStream inStream = null;
			try {
				inStream = transportClient.readSingleObject(swcvId, objectId, objectName, objectType, nameSpace, spLabel, anonymize);
			} catch (ResourceException e) {
				Activator.log(Status.ERROR, "exportTransportToClient failed", e);
			} catch (XiTransportException e) {
				Activator.log(Status.ERROR, "exportTransportToClient failed", e);
			}
			File file = new File(transportDirectory + System.getProperty("file.separator") + element.getOBJECTNAME() + ".tpz");
			OutputStream fileOut = null;
			try {
				fileOut = new FileOutputStream(file);
			} catch (FileNotFoundException e) {
				Activator.log(Status.ERROR, "exportTransportToClient failed", e);
			}
			try {
				IOUtil.copyStream(inStream, fileOut);
			} catch (IOException e) {
				Activator.log(Status.ERROR, "exportTransportToClient failed", e);
			}
		}
	}
	public static void exportTransportToServer(HashMap<ConfigurationNode, ConfigurationNode> elements, String transportDirectory, String transportArchiveDirectory, Server left) {
		exportTransportToClient(elements, transportDirectory);
		importTransportToServer(elements, transportDirectory, transportArchiveDirectory);
	}
	private static String importTransportToServer(HashMap<ConfigurationNode, ConfigurationNode> elements, String transportDirectory, String transportArchiveDirectory) {
		ConfigurationNode eee = (ConfigurationNode) elements.values().toArray()[0];
		InputStream inStream;
		FileOutputStream fileOut;
		FileInputStream fileIn;
		inStream = null;
		fileOut = null;
		fileIn = null;
		String result = "";
		URL url = null;
		try {
			url = new URL(eee.getServer().getURL());
		} catch (MalformedURLException e) {
			Activator.log(Status.ERROR, "importTransportToServer failed", e);
		}
		String user = eee.getServer().getUSER();
		String pwd = eee.getServer().getPASSWORD();
		String filename = transportDirectory + System.getProperty("file.separator") + eee.getOBJECTNAME() + ".tpz";
		long maxWaitTime = true ? 0x1b7740L : Long.parseLong("1800000");
		System.out.println("Checking if Folder to Link conversion is required..");
		int migrationResponseCode = triggerExecuteFolderLinkConversion(maxWaitTime, eee.getServer().getURL(), user, pwd);
		if (migrationResponseCode != 200) try {
			throw new Exception("Check for Folder to Link conversion failed with response code: " + migrationResponseCode);
		} catch (Exception e) {
			Activator.log(Status.ERROR, "importTransportToServer failed", e);
		}
		System.out.println("Check for Folder to Link conversion executed successfully.");
		System.out.println((new StringBuilder()).append("Trying to initialize TransportClient with url = ").append(url).toString());
		XiTransportTestClient transportClient = null;
		try {
			transportClient = XiTransportClientProvider.getXiTransportTestClient(url, user, pwd);
		} catch (ResourceException e) {
			Activator.log(Status.ERROR, "importTransportToServer failed", e);
		}
		System.out.println("TransportClient initialized.");
		System.out.println((new StringBuilder()).append("Running import of file ").append(filename).append(" into system ").append(url).toString());
		File file = new File(filename);
		try {
			fileIn = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			Activator.log(Status.ERROR, "importTransportToServer failed", e);
		}
		String importedCLGuid = null;
		try {
			importedCLGuid = transportClient.writeChangelistData(fileIn);
		} catch (ResourceException e) {
			Activator.log(Status.ERROR, "importTransportToServer failed", e);
		} catch (XiTransportException e) {
			Activator.log(Status.ERROR, "importTransportToServer failed", e);
		}
		System.out.println((new StringBuilder()).append("Finished import of versionset ").append(importedCLGuid).toString());
		int conflicts = 0;
		try {
			conflicts = getConflictsImport(transportClient, importedCLGuid);
		} catch (ResourceException e) {
			Activator.log(Status.ERROR, "importTransportToServer failed", e);
		} catch (XiTransportException e) {
			Activator.log(Status.ERROR, "importTransportToServer failed", e);
		}
		if (conflicts > 0) {
			result = (new StringBuilder()).append("Import caused  ").append(conflicts).append(" conflicts").toString();
			if (inStream != null) try {
				inStream.close();
			} catch (Exception e) {
			}
			if (fileOut != null) try {
				fileOut.close();
			} catch (Exception e) {
			}
			if (fileIn != null) try {
				fileIn.close();
			} catch (Exception e) {
			}
			return result;
		} else {
			if (inStream != null) try {
				inStream.close();
			} catch (Exception e) {
			}
			if (fileOut != null) try {
				fileOut.close();
			} catch (Exception e) {
			}
			if (fileIn != null) try {
				fileIn.close();
			} catch (Exception e) {
			}
			return result;
		}
	}
	private static int getConflictsImport(XiTransportTestClient transportClient, String importedCLGuid) throws ResourceException, XiTransportException {
		return transportClient.getImportConflicts(importedCLGuid);
	}
	private static int triggerExecuteFolderLinkConversion(long maxWaitTime, String repURL, String loginUserId, String loginPassword) {
		int responseCode = -1;
		try {
			responseCode = (new StartRepAppWebClient(repURL, loginUserId, loginPassword, maxWaitTime)).startRepositoryAppExplicitlyIfNotStarted();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (responseCode != 200) System.err.println((new StringBuilder()).append("\nError: Folder link conversion execution failed with response code: ").append(responseCode).toString());
		return responseCode;
	}
}