package com.example.data.download;



import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;


public class LocalFastDownloadClient {
	
	 /** Default size in bytes per write in order to send to server */
    private static final int DEFAULT_CHUNK_SIZE = 8192;

    /** The IP address of the server host */
    private final String mHost;
    /** The TCP port of the server host */
    private final int mPort;
    /** The timeout in milliseconds for making connection to the server */
    private final int mTimeout;
    /** Holds a Socket connection to server */
    private Socket socket;

    /** The path to the file to send to the server */
    private final String mFilepath;

    /** This flag is used to abort sending data to server */
    private volatile boolean isAborted = false;

    /** Holds a StreamHandler object */
    private StreamHandler streamHandler;


    /**
     * Creates a client instance using given parameters.
     *
     * @param host The IP address of the server host.
     * @param port The TCP port of the server host.
     * @param filepath The path to the file to send to the server.
     */
    public LocalFastDownloadClient(String host, int port, String filepath) {
        this(host, port, 0, filepath);
    }

    /**
     * Creates a client instance using given parameters.
     *
     * @param host The IP address of the server host.
     * @param port The TCP port of the server host.
     * @param timeout The timeout in milliseconds for making connection to the server.
     * @param filepath The path to the file to send to the server.
     */
    public LocalFastDownloadClient(String host, int port, int timeout, String filepath) {
        mHost = host;
        mPort = port;
        mTimeout = timeout;
        mFilepath = filepath;
    }

    /**
     * Initializes 'fileToSend' field, connects to server, and starts the process of sending file data to server.
     *
     * @return Result enum case to indicate the method invocation result.
     */
    public Result start() {
        Result result = validateFile();
        if (result != Result.SUCCESS) {
            return result;
        }

        result = connect();
        if (result != Result.SUCCESS) {
            return result;
        }

        streamHandler = new StreamHandler();
        streamHandler.start();
        return Result.SUCCESS;
    }

    /**
     * Validates field 'mFilePath' and initializes field 'fileToSend'.
     *
     * @return Returns a Result enum case to indicate the file validation result.
     */
    private Result validateFile() {
        if (mFilepath == null) {
            return Result.FILE_PATH_INVALID;
        }
        File fileToSend = new File(mFilepath);
        // Validate file path
        if (!fileToSend.exists() || !fileToSend.isFile()) {
            System.err.println(mFilepath + " is NOT valid!");
            return Result.FILE_PATH_INVALID;
        }
        return Result.SUCCESS;
    }

    /**
     * Makes socket connection to the server.
     *
     * @return Returns a Result enum case to indicate the connection result.
     */
    private Result connect() {
        System.out.println("Connecting to SERVER " + mHost + " on port " + mPort);

        try {
            socket = new Socket();
            SocketAddress address = new InetSocketAddress(mHost, mPort);
            socket.connect(address, mTimeout);
        } catch (ConnectException connectException) {
            connectException.printStackTrace();
            return Result.SERVER_NOT_STARTED;
        } catch (UnknownHostException unknownHostException) {
            unknownHostException.printStackTrace();
            return Result.UNKNOWN_HOST;
        } catch (SocketTimeoutException socketTimeoutException) {
            // Timeout expires before connecting
            socketTimeoutException.printStackTrace();
            return Result.SOCKET_TIMEOUT;
        } catch (IOException ioException) {
            ioException.printStackTrace();
            return Result.IO_ERROR;
        } catch (SecurityException securityException) {
            securityException.printStackTrace();
            return Result.SECURITY_ERROR;
        } catch (IllegalArgumentException illegalArgumentException) {
            illegalArgumentException.printStackTrace();
            return Result.PORT_OUT_OF_RANGE;
        }

        System.out.println("Just connected to SERVER " + socket.getRemoteSocketAddress());

        return Result.SUCCESS;
    }

    /**
     * Sets 'isAborted' flag to signify aborting the process of sending data to server.
     *
     * @return true if the sending process is in progress; otherwise false.
     */
    public boolean abort() {
        if (streamHandler != null && streamHandler.isAlive()) {
            isAborted = true;
            System.out.println("Aborting ...");
            return true;
        }

        if (streamHandler != null) {
            System.out.println("Too late to abort as the sending process Already Completed");
        }

        return false;
    }

    /**
     * A Thread subclass to send all the file's data chunk by chunk to the connected server.
     */
    class StreamHandler extends Thread {

        /** Holds BufferedOutputStream object used to write file data to send to the server */
        private DataOutputStream mOutputStream;

        @Override
        public void run() {
            try {
                mOutputStream = new DataOutputStream(socket.getOutputStream());
                startStream();
                stopStream();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

        /**
         * Reads data from the given file and sends it over socket to server.
         *
         * @throws IOException if an I/O error occurs.
         */
        private void startStream() throws IOException {
            System.out.println("Sending ...");
            byte[] fileData = Files.readAllBytes(Path.of(mFilepath));

            mOutputStream.flush();
            // Here for PCL to finish flush data in output stream.
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // write data to output stream
            writeToServer(fileData);
        }

        private void writeToServer(byte[] fileData) throws IOException {
            int chunkSize = DEFAULT_CHUNK_SIZE;
            int offSet = 0;
            while (true) {
                if (isAborted) {
                    break;
                }
                int availableSize = fileData.length - offSet;
                if (availableSize > chunkSize) {
                    mOutputStream.write(fileData, offSet, chunkSize);
                } else {
                    mOutputStream.write(fileData, offSet, availableSize);
                    break;
                }
                offSet += chunkSize;
            }
        }

        /**
         * Closes socket and its out stream.
         */
        private void stopStream() throws IOException {
            System.out.println("stop stream");
            mOutputStream.flush();
            mOutputStream.close();
            socket.close();

            if (isAborted) {
                System.out.println("Sending Aborted");
            } else {
                System.out.println("Sending Completed");
            }
        }

    }

    /**
     * Represents every 'Result' type that can happen and return when start() method is called.
     */
    public enum Result {
        /** This means that client starts successfully and begins sending file data to server. */
        SUCCESS,

        /** Path to file to be sent is not valid */
        FILE_PATH_INVALID,

        /** When client starts server is not started yet. */
        SERVER_NOT_STARTED,
        /** The IP address of the host could not be determined. */
        UNKNOWN_HOST,
        /** Timeout expires before connecting. */
        SOCKET_TIMEOUT,
        /** An I/O error occurs when creating the socket. */
        IO_ERROR,
        /** A security manager exists and its checkConnect method doesn't allow the operation. */
        SECURITY_ERROR,
        /** Port outside the specified range of valid port values, which is between 0 and 65535. */
        PORT_OUT_OF_RANGE
    }

}



