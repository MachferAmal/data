package com.example.data.Services;

import org.springframework.stereotype.Service;

import com.example.data.download.ServerMain;

@Service
//@Validated
public class ServerServices {
	
	// Méthode pour récupérer un message basé sur les paramètres "port" et "path"
    public String getMessageByPortAndPath(int port, String path) {
    	
    	// Créez une instance de ServerMain
        ServerMain serverMain = new ServerMain();

    	
        // Traitement des paramètres pour générer le message
        // Simulating loading delay for demonstration purposes
        try {
        	
        	   // Appelez la méthode start avec les paramètres
            serverMain.start(port, path);
            Thread.sleep(2000); // Wait for 2 seconds to simulate loading
        	
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "Message with port: " + port + " and path: " + path;
    }
}
