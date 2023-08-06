package com.example.data.download;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class FileUtils {

	
	
	 private static final String HASH_ALGORITHM_MD5 = "MD5";

	    /**
	     * This function create hash file from file path of file uses to MD5 algorithm.
	     *
	     * @param filePath is path of file.

	     * @return Computes and returns the hash value for the file's data.
	     */
	    public static String createHash(String filePath) {
	        try {
	            File fileToSend = new File(filePath);
	            FileInputStream fileInputStream = new FileInputStream(fileToSend);
	            System.out.println("Sending : " + fileToSend.getPath());
	            byte[] fileData = fileInputStream.readAllBytes();
	            fileInputStream.close();
	            MessageDigest messageDigest = MessageDigest.getInstance(HASH_ALGORITHM_MD5);
	            messageDigest.update(fileData);
	            return convertToHex(messageDigest.digest());
	        } catch (IOException e) {
	            System.out.println("IOException :" + e);
	        } catch (NoSuchAlgorithmException ex) {
	            System.out.println("NoSuchAlgorithmException " + ex);
	        }
	        return "";
	    }

	    private static String convertToHex(byte[] bytes) {

	        char[] result = new char[bytes.length * 2];

	        for (int index = 0; index < bytes.length; index++) {
	            int v = bytes[index];

	            int upper = (v >>> 4) & 0xF;
	            result[index * 2] = (char) (upper + (upper < 10 ? 48 : 65 - 10));

	            int lower = v & 0xF;
	            result[index * 2 + 1] = (char) (lower + (lower < 10 ? 48 : 65 - 10));
	        }

	        return new String(result);
	    }

	}


