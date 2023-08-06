package com.example.data.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import com.example.data.Model.MessageResponse;

import com.example.data.Services.ClientService;
//import com.example.data.Model.ServerModel;


@CrossOrigin(origins = "*")
@RestController
public class ClientController {
	
	 @Autowired
	    private ClientService clientService;

    // Endpoint pour obtenir le message en fonction des paramètres "port" et "path"
    @GetMapping(path = "/client/getMessage", produces = MediaType.APPLICATION_JSON_VALUE)
    public MessageResponse getMessageByPortAndPath(
            @RequestParam(name = "port") int port,
            @RequestParam(name = "path") String path ,
            @RequestParam(name = "addressIp") String addressIp) {
        String message = clientService.getMessageByPortAndPath(port, path, addressIp);

        // Créer l'objet MessageResponse
        MessageResponse response = new MessageResponse(message);
        return response;
    }

    // Endpoint pour déclencher le traitement avec les données fournies par le frontend
    // Note: You can choose to keep or remove this endpoint based on your requirements
    // If you only need the getMessageByPortAndPath, you can remove this endpoint.
}
