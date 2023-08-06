package com.example.data.download;

public class ClientMain {
	 public  void start(String serverIpAddress,int serverPort, String filepath ) throws InterruptedException {
	     

	        // create hash value for demo app
	        String hash = FileUtils.createHash(filepath);
	        System.out.println("Hash: " + hash);

	        int timeout = 2_000;
	        LocalFastDownloadClient client = new LocalFastDownloadClient(serverIpAddress, serverPort, timeout, filepath);
	        LocalFastDownloadClient.Result result = client.start();
	        System.out.println("LocalFastDownloadClient start result = " + result);
	        switch (result) {
	            // This means client just connects and begins sending data to server
	            case SUCCESS:
	                boolean wantToAbort = false;
	                if (wantToAbort) {
	                    // Wait until time to abort
	                    Thread.sleep(100);
	                    boolean abortResult = client.abort();
	                    System.out.println("LocalFastDownloadClient abort result = " + abortResult);
	                }
	                break;
	            case FILE_PATH_INVALID:
	                // This means Path to file to be sent is not valid
	                break;
	            case UNKNOWN_HOST:
	                // This means The IP address of the host could not be determined
	                break;
	            case SOCKET_TIMEOUT:
	                // This means Timeout expires before connecting
	                break;
	            case PORT_OUT_OF_RANGE:
	                // This means the server's port is outside the specified range of valid port values, which is between 0 and 65535
	                break;
	            case IO_ERROR:
	                // This means some I/O error occurs
	                break;
	            case SECURITY_ERROR:
	                // This means a security manager exists and doesn't allow the client connecting to the server
	                break;
	        }
	    }


}
