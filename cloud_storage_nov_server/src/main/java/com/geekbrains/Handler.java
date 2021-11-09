package com.geekbrains;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public class Handler implements Runnable{

    private final Path serverDir;
    private static int counter = 0;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ObjectInputStream ois;
    private final ObjectOutputStream oos;
    private final String name;
    private boolean isRunning;

    public Handler(Socket socket) throws IOException {
        serverDir = Paths.get("cloud_storage_nov_server","server");
        if(!Files.exists(serverDir)){
            Files.createDirectory(serverDir);
        }
        oos = new ObjectOutputStream(socket.getOutputStream());
        ois = new ObjectInputStream(socket.getInputStream());

        counter++;
        name = "User#" + counter;
        log.debug("Set nick: {} for new client", name);
        isRunning = true;
    }

    private String getDate(){
        return formatter.format(LocalDateTime.now());
    }

    public void setRunning(boolean running){
        isRunning = running;
    }

    @Override
    public void run() {
        try{
            log.debug("run");
            while (isRunning) {
                String response, fileName;
                MyObject myObject = (MyObject) ois.readObject();
                fileName = myObject.getName();
                if (myObject.getMas() != null) {
                    FileOutputStream fileOutputStream = new FileOutputStream(serverDir.toString() + File.separator + fileName);
                    fileOutputStream.write(myObject.mas);
                    fileOutputStream.close();
                    response = "file received: " + fileName;
                    myObject.setMas(null);
                } else {
                    response = String.format("%s %s: %s", getDate(), name, fileName);
                }
                log.debug("Message for response: {}", response);
                myObject.setName(response);
                myObject.setMas(null);
                oos.writeObject(myObject);
                oos.flush();
            }
        } catch (Exception e){
            log.error("", e);
        }
    }
}
