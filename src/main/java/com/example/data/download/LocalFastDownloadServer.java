package com.example.data.download;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Path;


public class LocalFastDownloadServer {
	
	 /**
     * Default maximum time in milliseconds to wait for client connection
     */
    private static final int DEFAULT_SOCKET_TIMEOUT = 90_000;
    /**
     * Default size in bytes per write in order to send to client
     */
    private static final int DEFAULT_CHUNK_SIZE = 8192;

    /**
     * The tcp port number on which server waits for client connection
     */
    private final int mPort;
    /**
     * The maximum time in milliseconds to wait for client connection
     */
    private final int mTimeout;
    /**
     * The path to file to send to client
     */
    private final String mFilePath;

    /**
     * Holds a ServerSocket object that is opened on port 'mPort' and used to wait for client connection
     */
    private ServerSocket serverSocket;
    /**
     * Holds an object that handles all the communication with client in a separate thread
     */
    private StreamHandler streamHandler;

    /**
     * Creates a server instance with given port, default socket timeout and given filePath.
     *
     * @param port     indicates port number on which server waits for client connection.
     * @param filePath indicates path to file to send to client.
     */
    public LocalFastDownloadServer(int port, String filePath) {
        this(port, DEFAULT_SOCKET_TIMEOUT, filePath);
    }

    /**
     * Creates a server instance with given port, socket timeout and filePath.
     *
     * @param port     indicates port number on which server waits for client connection.
     * @param timeout  indicates maximum time in milliseconds to wait for client connection.
     * @param filePath indicates path to file to send to client.
     */
    public LocalFastDownloadServer(int port, int timeout, String filePath) {
        mPort = port;
        mTimeout = timeout;
        mFilePath = filePath;
    }

    /**
     * This method does all the preparation work before server actually sends file to client in a separate thread.
     *
     * @return Returns a Result enum case to indicate the result of preparation work.
     */
    public Result start() {
        if (isServerRunning()) {
            return Result.SERVER_ALREADY_RUNNING;
        }

        Result validateResult = validateFile();
        if (validateResult != Result.SUCCESS) return validateResult;

        Result initResult = initServer();
        if (initResult != Result.SUCCESS) return initResult;

        return startListener();
    }

    /**
     * @return whether server currently sends data to client.
     */
    private boolean isServerRunning() {
        return streamHandler != null && streamHandler.isAlive();
    }

    /**
     * Validates field 'mFilePath' and initializes field 'fileToSend'.
     *
     * @return Returns a Result enum case to indicate the file validation result.
     */
    private Result validateFile() {
        if (mFilePath == null) {
            return Result.FILE_PATH_INVALID;
        }
        File fileToSend = new File(mFilePath);
        // validate file path
        if (!fileToSend.exists() || !fileToSend.isFile()) {
            System.err.println(mFilePath + " is NOT valid!");
            return Result.FILE_PATH_INVALID;
        }
        return Result.SUCCESS;
    }

    /**
     * This method creates and configures a ServerSocket object.
     *
     * @return Returns a Result enum case to indicate the server initialization result.
     */
    private Result initServer() {
        try {
            // Only creates ServerSocket object once per LocalFastDownloadServer instance
            if (serverSocket == null) {
                serverSocket = new ServerSocket(mPort);
                if (mPort == 0) {
                    System.out.println("Automatically Allocated Port: " + serverSocket.getLocalPort());
                }
                serverSocket.setReuseAddress(true);
                serverSocket.setSoTimeout(mTimeout);
            }
        } catch (BindException bindException) {
            // Port already in use: JVM_Bind
            bindException.printStackTrace();
            return Result.PORT_ALREADY_IN_USE;
        } catch (IllegalArgumentException illegalArgumentException) {
            // Port outside the specified range of valid port values, which is between 0 and 65535
            illegalArgumentException.printStackTrace();
            return Result.PORT_OUT_OF_RANGE;
        } catch (SecurityException securityException) {
            // A security manager exists and its checkListen method doesn't allow the operation
            securityException.printStackTrace();
            return Result.SECURITY_ERROR;
        } catch (IOException ioException) {
            // an I/O error occurs when opening the socket
            ioException.printStackTrace();
            return Result.IO_ERROR;
        }
        return Result.SUCCESS;
    }

    /**
     * This method listens for a client connection. It blocks until a client connection comes.
     * Then, it creates and starts a StreamHandler object,
     * which will handle all the communication with the client in a separate thread.
     *
     * @return Returns a Result enum case to indicate the result of listening for client connection.
     */
    private Result startListener() {
        try {
            System.out.println("Waiting for CLIENT connection on port " + serverSocket.getLocalPort());
            final Socket socket = serverSocket.accept();
            System.out.println("Just connected to CLIENT " + socket.getRemoteSocketAddress());
            streamHandler = new StreamHandler(socket);
            // start stream handler
            streamHandler.start();

            return Result.SUCCESS;
        } catch (SocketTimeoutException socketTimeoutException) {
            // A timeout was previously set with setSoTimeout and the timeout has been reached
            socketTimeoutException.printStackTrace();
            return Result.SOCKET_TIMEOUT;
        } catch (SecurityException securityException) {
            // A security manager exists and its checkAccept method doesn't allow the operation
            securityException.printStackTrace();
            return Result.SECURITY_ERROR;
        } catch (IOException ioException) {
            // An I/O error occurs when waiting for a connection
            ioException.printStackTrace();
            return Result.IO_ERROR;
        }
    }

    /**
     * This method is used to abort the file data sending process that is in progress.
     *
     * @return true if the sending process is in progress; otherwise false.
     */
    public boolean abort() {
        if (streamHandler != null && streamHandler.isAlive()) {
            streamHandler.isAborted = true;
            System.out.println("Aborting ...");
            return true;
        }

        if (streamHandler != null) {
            System.out.println("Too late to abort as the sending process Already Completed");
        }

        return false;
    }

    /**
     * This method is used to close the server socket object referenced by field 'serverSocket'.
     * It should be called when the server finishes all the communication with the client.
     */
    private void releaseServer() {
        try {
            serverSocket.close();
        } catch (IOException ioException) {
            // an I/O error occurs when opening the socket
            ioException.printStackTrace();
        }
    }

    /**
     * A Thread subclass to handle all the communication with the connected client.
     */
    class StreamHandler extends Thread {
        /**
         * Holds the socket object connected to the client
         */
        private final Socket mSocket;
        /**
         * Holds the socket's input stream object used to receive request/data from the client
         */
        private final DataInputStream mInputStream;
        /**
         * Holds the socket's output stream object used to send response/data to the client
         */
        private final DataOutputStream mOutputStream;

        /**
         * This flag is used to abort sending data to client
         */
        private volatile boolean isAborted = false;

        /**
         * Creates a StreamHandler instance with given socket.
         *
         * @param socket Indicates the socket object connected to the client.
         * @throws IOException if an I/O error occurs.
         */
        StreamHandler(Socket socket) throws IOException {
            mSocket = socket;
            mInputStream = new DataInputStream(socket.getInputStream());
            mOutputStream = new DataOutputStream(socket.getOutputStream());
        }

        /**
         * This method will be called when the StreamHandler is started.
         * It does all the communication work with the connected client.
         */
        public void run() {
            try {
                startStream();
                stopStream();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }

        /**
         * Sends the hash value of file data followed by the file data to CLIENT.
         *
         * @throws IOException if an I/O error occurs.
         */
        private void startStream() throws IOException {
            System.out.println("startStream");
            byte[] fileData = Files.readAllBytes(Path.of(mFilePath));

            mOutputStream.flush();
            // write data to output stream
            writeToClient(fileData);
        }

        private void writeToClient(byte[] fileData) throws IOException {
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
         * Closes the socket and its input & output streams.
         *
         * @throws IOException if an I/O error occurs.
         */
        private void stopStream() throws IOException {
            System.out.println("stopStream");
            // Here for PCL to finish writing the msg
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            mInputStream.close();
            mOutputStream.flush();
            mOutputStream.close();
            mSocket.close();

            if (isAborted) {
                System.out.println("Aborted");
            } else {
                System.out.println("Finished");
            }
        }
    }

    /**
     * Represents every 'Result' type that can happen and return when start() method is called.
     */
    public enum Result {
        /**
         * This means that server starts successfully and begins sending file data to client.
         */
        SUCCESS,

        // Error results
        /**
         * This means that start() method is called again while server already starts and is sending file data to client.
         */
        SERVER_ALREADY_RUNNING,
        /**
         * File path is not valid.
         */
        FILE_PATH_INVALID,
        /**
         * Port already in use.
         */
        PORT_ALREADY_IN_USE,
        /**
         * Port outside the specified range of valid port values, which is between 0 and 65535.
         */
        PORT_OUT_OF_RANGE,
        /**
         * A security manager exists and its checkListen method doesn't allow the operation.
         */
        SECURITY_ERROR,
        /**
         * A timeout was previously set with setSoTimeout and the timeout has been reached.
         */
        SOCKET_TIMEOUT,
        /**
         * An I/O error occurs when opening the socket.
         */
        IO_ERROR
    }

}
