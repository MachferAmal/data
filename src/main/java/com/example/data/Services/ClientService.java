package com.example.data.Services;



import org.springframework.stereotype.Service;

import com.example.data.download.ClientMain;




@Service
public class ClientService {
	
	
	// Méthode pour récupérer un message basé sur les paramètres "port" et "path"
    public String getMessageByPortAndPath(int port, String path,String addressIp) {
    	
    	// Créez une instance de ServerMain
        ClientMain  clientMain  = new  ClientMain ();
    	
        // Traitement des paramètres pour générer le message
    	// Simulating loading delay for demonstration purposes
        try {
        	
        	 clientMain.start( addressIp,port,path);
            Thread.sleep(2000); // Wait for 2 seconds to simulate loading
        
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "Message with port: " + port + " and path: " + path + " and addressip: " + addressIp;
    }
    }