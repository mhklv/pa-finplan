package org.mchklv.finplan.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.time.LocalDateTime;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.*;

public class Server implements Runnable {
    int localPort;
    ThreadPoolExecutor threadPoolExecutor;
    
    public Server(int localPort) {
        this.localPort = localPort;
        threadPoolExecutor = new ThreadPoolExecutor(20, 100, 30, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(200));
    }

    public void run() {
        try {
            SSLServerSocket serverSocket = getServerSocket(localPort);
            
            while (true) {
                SSLSocket socket = (SSLSocket) serverSocket.accept();
                DataConnectionThread connectionThread = new DataConnectionThread(socket);
                threadPoolExecutor.execute(connectionThread);
            }
        }
        catch (Throwable e) {
            System.out.println("Error " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private SSLServerSocket getServerSocket(int port)
        throws NoSuchAlgorithmException, KeyStoreException, CertificateException,
        IOException, KeyManagementException, UnrecoverableKeyException {

        char[] password = "GrievousnessDissectedPolystyleThysanuraMetamorphosedNoblewoman".toCharArray();
        
        SSLContext context = SSLContext.getInstance("TLS");
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        KeyStore keyStore = KeyStore.getInstance("JKS");
        InputStream certPrivKeyStream = ResourceManager.getKeyStoreStream();
        keyStore.load(certPrivKeyStream, password);
        certPrivKeyStream.close();
        kmf.init(keyStore, password);
        context.init(kmf.getKeyManagers(), null, null);
        SSLServerSocket serverSocket = (SSLServerSocket) context.getServerSocketFactory().createServerSocket(port);
        
        return serverSocket;
    }
}
