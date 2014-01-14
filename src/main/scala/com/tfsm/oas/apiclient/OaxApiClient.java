package com.tfsm.oas.apiclient;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.namespace.QName;
import javax.xml.rpc.Call;
import javax.xml.rpc.Service;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.ServiceFactory;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

/**
 * This is a Dynamic Invocation Interface (DII) client meaning there is no stub class
 * required by the client to compile.
 * 
 * The JAX-RPC interface provides a higher level API allowing programmers to avoid the
 * details of SOAP messaging.  Since the operations provided by a service are defined in 
 * the WSDL that is published, a client can invoke these operations on the service based 
 * on the service's abstract definition.
 * 
 *
 */
public class OaxApiClient
{
    private static Logger logger  = Logger.getLogger(OaxApiClient.class);
    /**
     * @param args
     */
    public static void main (String[] args) throws Exception
    {
    	// Use a local trust Store
    	String keyStorePath = InstallCert.getKeyStorePath();
    	System.setProperty("javax.net.ssl.trustStore", keyStorePath );
    	
        PropertyConfigurator.configure("log4j.properties");

        if (args.length != 5)
        {
            logger.fatal("You have not supplied 5 arguments:  host[:port] OasAccount OasUser OasPassword fullPathToAdXMLFile");
        }
        else
        {
            OaxApiClient oaxApiClient = new OaxApiClient();
            oaxApiClient.Run(args);
        }        
    }

    private void Run (String[] args) throws Exception
    {
        String oasHost = args[0];
        String oasAccount = args[1];
        String oasUser = args[2];
        String oasPassword = args[3];
        String adXML = getXmlStringFromFile (args[4]);
        if (!oasHost.startsWith("http"))
        {
            oasHost = "http://" + oasHost;
        }


        logger.info("Contacting webservice at " + oasHost + "/oasapi/");
        printInput (oasAccount, oasUser, oasPassword, args[4]);
        printXmlInput(adXML);
        logger.info("output:" );
        System.out.println (callOasApi(oasHost, oasAccount, oasUser, oasPassword, adXML));
    }

    /**
     * This generic method may be reused to call any OAS API function
     * 
     * @param oasHost     The host on which OAS resides  
     * @param oasAccount  The OAS Account to which you have access
     * @param oasUser     The OAS User name given to you
     * @param oasPassword The OAS Password for your user
     * @param adXML       The structured XML request containing all the parameters 
     * @return            The AdXML response from the OAS API
     * @throws MalformedURLException 
     * @throws ServiceException 
     * @throws RemoteException 
     */
    public String callOasApi (String oasHost, String oasAccount, String oasUser, String oasPassword, String adXML) throws MalformedURLException, ServiceException, RemoteException 
    {
        
       HostnameVerifier hv = new HostnameVerifier() {
            public boolean verify(String urlHostName, SSLSession session) 
            {
                System.out.println("Warning: URL Host: " + urlHostName + " does not match SSL Certificate host: " + session.getPeerHost() + ".");
                return true;
            }
        };
 
        HttpsURLConnection.setDefaultHostnameVerifier(hv);
    	String urlString = oasHost + "/oasapi/";
        URL url = new URL (urlString + "OaxApi?wsdl");
    	String nameSpace = "http://api.oas.tfsm.com/";
        QName qname = new QName (nameSpace, "OaxApiService");
        QName port = new QName (nameSpace, "OaxApiPort");
        QName operation = new QName (nameSpace, "OasXmlRequest");

        ServiceFactory factory = ServiceFactory.newInstance ();
        Service service = factory.createService (url, qname);
        Call call = service.createCall (port, operation);
    	return (String)call.invoke (new Object[] {oasAccount, oasUser, oasPassword, adXML });
    }
    
    private void printInput (String oasAccount, String oasUser, String oasPassword, String fileName)
    {
        logger.info ("OaxApi.OasXmlRequest(" 
                + oasAccount 
                + ", "
                + oasUser
                + ","
                + oasPassword
                + ",\n[text from file: " + fileName + "]");
        logger.info(")");
    }
    
    private void printXmlInput (String adXML)
    {
        logger.debug("--------------START adXML------------------ ");
        logger.debug(adXML);
        logger.debug("--------------END   adXML------------------ ");
    }
    
    private String getXmlStringFromFile (String fullPathToFile) 
    {       
        StringBuffer fileText  = new StringBuffer();
        BufferedReader fileReader = null;
        try
        {
            fileReader = new BufferedReader(new FileReader (fullPathToFile));
            String line = null;
            while ((line = fileReader.readLine()) != null)
            {
                line = line.trim();
                fileText.append(line + "\r\n");
            }
            fileReader.close();
        }
        catch (FileNotFoundException fnfe)
        {
            fnfe.printStackTrace();
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
        finally
        {
            try
            {
                fileReader.close();                    
            }
            catch (IOException ioe)
            {                
            }            
        }
        return fileText.toString();
    }
}
