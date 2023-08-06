package com.example.data.download;


public class ServerMain {
	
	  /**
     * Main method, to start the server.
     */
    public  void start ( int tcpPort,String pathOfFileToSend ) throws InterruptedException {
       
        // create hash value for demo app
        String hash = FileUtils.createHash(pathOfFileToSend);
        System.out.println("Hash: " + hash);

        LocalFastDownloadServer server = new LocalFastDownloadServer(tcpPort, pathOfFileToSend);

        LocalFastDownloadServer.Result result = server.start();
        System.out.println("LocalFastDownloadServer start result = " + result.name());
        switch (result) {
            case SUCCESS:
                // This means server just connects and begins sending data to client
                boolean wantToAbort = false;
                if (wantToAbort) {
                    // Wait until time to abort
                    Thread.sleep(100);
                    boolean abortResult = server.abort();
                    System.out.println("LocalFastDownloadServer abort result = " + abortResult);
                }
                break;
            case FILE_PATH_INVALID:
                // This means the File path is not valid
                break;
            case SERVER_ALREADY_RUNNING:
                // This means start() method is called again while server already starts and is sending file data to client
                break;
            case PORT_ALREADY_IN_USE:
                // This means the server's port is already in use by another alive server instance
                break;
            case PORT_OUT_OF_RANGE:
                // This means the server's port is outside the specified range of valid port values, which is between 0 and 65535
                break;
            case SOCKET_TIMEOUT:
                // This means timeout for waiting client connection has been reached
                break;
            case IO_ERROR:
                // This means some I/O error occurs
                break;
            case SECURITY_ERROR:
                // This means a security manager exists and doesn't allow the server listening on or accepting client connection on the port
                break;
        }
    }

}




