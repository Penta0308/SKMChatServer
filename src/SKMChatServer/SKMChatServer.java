package SKMChatServer;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.ArrayList;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import cmt.CMT;

import data.Data;
 
public class SKMChatServer {
	
	public static SSLServerSocket sslserversocket;
	
	private static SSLContext newSSLContext(final KeyStore ks, final String password, final String ksAlgorithm) throws Exception {
		try {
		    // Get a KeyManager and initialize it
		    final KeyManagerFactory kmf = KeyManagerFactory.getInstance(ksAlgorithm);
		    kmf.init(ks, password.toCharArray());
		    
		    // Get a TrustManagerFactory with the DEFAULT KEYSTORE, so we have all the certificates in cacerts trusted
		    final TrustManagerFactory tmf = TrustManagerFactory.getInstance(ksAlgorithm);
		    tmf.init((KeyStore) null);
		    
		    // Get the SSLContext to help create SSLSocketFactory
		    //final SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
		    final SSLContext sslContext = SSLContext.getInstance("SSL");
		    sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
		    return sslContext;
		} catch (final GeneralSecurityException e) {
		    throw new Exception(e);
		}
	}
	
    public static void main(String[] args) throws IOException {
    	try {
        	//SSLContext context = SSLContext.getInstance("SSL");
        	//context.init(null, null, null);
    		KeyStore ks = KeyStore.getInstance("PKCS12");
    		//"E:\\Documents\\SKMChatServer\\SKMChatServer.ks"
    		
    		System.setProperty("javax.net.ssl.keyStore", "E:\\Documents\\SKMChatServer\\SKMChatServer.ks");
    		//System.setProperty("javax.net.debug", "all");
    		
    	    java.io.FileInputStream fis = null;
    	    try {
    	        fis = new java.io.FileInputStream("E:\\Documents\\SKMChatServer\\SKMChatServer.ks");
    	        ks.load(fis, Data.password.toCharArray());
    	    } finally {
    	        if (fis != null) {
    	            fis.close();
    	        }
    	    }
    	    
    		SSLContext context = newSSLContext(ks, Data.password, "SunX509");
        	SSLServerSocketFactory factory = context.getServerSocketFactory();
        	
        	//String [] suites = {"TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384"};
            String [] suites = factory.getSupportedCipherSuites();
            System.out.println(suites[0]);
            sslserversocket = (SSLServerSocket) factory.createServerSocket(Data.port);
            sslserversocket.setEnabledCipherSuites(suites);
            
            ArrayList<CMT> CMTList = new ArrayList<CMT>();
            
            System.out.println("Wating Connection");
            
            while(true) {
            	CMTList.add(new CMT());
            }
             
        } catch(Exception ex){
            System.out.println(ex);
        } finally {
        	if(!sslserversocket.isClosed()) sslserversocket.close();
        }
    }
     
}