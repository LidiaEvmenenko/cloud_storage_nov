package com.geekbrains;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Slf4j
public class MainController implements Initializable {

    public Button sendFileButton;
    private Path clientDir;
    private Path serverDir;

    public ListView<String> clientView;
    public ListView<String> serverView;
    public TextField input;

    private Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try{
            clientDir = Paths.get("cloud_storage_nov_client","client");
            if(!Files.exists(clientDir)){
                Files.createDirectory(clientDir);
            }
            serverDir = Paths.get("cloud_storage_nov_server","server");
            refreshViews();
            clientView.setOnMouseClicked(event -> {
                if(event.getClickCount() == 2){
                    String item = clientView.getSelectionModel().getSelectedItem();
                    input.setText(item);
                }
            });
            socket = new Socket("localhost",8189);
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());

            Thread readThread = new Thread(this::read);
            readThread.setDaemon(true);
            readThread.start();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void refreshViews() {
        Platform.runLater(() -> {
            clientView.getItems().clear();
            serverView.getItems().clear();
            try {
                clientView.getItems().addAll(getFiles(clientDir));
                serverView.getItems().addAll(getFiles(serverDir));
            } catch (IOException e) {
                e.printStackTrace();
            }

        });
    }

    private List<String> getFiles(Path path) throws IOException {
        return Files.list(path).map(p -> p.getFileName().toString()).collect(Collectors.toList());
    }

    private void read(){
        try {
            while (true) {
                refreshViews();
                MyObject myObject = (MyObject) ois.readObject();
                String msg = myObject.getName();
                //Platform.runLater(()-> clientView.getItems().add(msg));
                log.debug("received the answer: {}",msg);
            }
        } catch (Exception e) {
            log.error("",e);
        }
    }

    public void sendMessage(ActionEvent actionEvent) throws IOException {
        MyObject myObject = new MyObject(input.getText(),null);
        oos.writeObject(myObject);
        oos.flush();
        input.clear();
    }

    public void sendFile(ActionEvent actionEvent) throws IOException, ClassNotFoundException {
        String fileForSending = input.getText();
        MyObject myObject = new MyObject(fileForSending);
        log.debug("Want to send: {}", Paths.get(clientDir.toString(),fileForSending));
        myObject.setMas(Files.readAllBytes(Paths.get(clientDir.toString(),fileForSending)));
        oos.writeObject(myObject);
        oos.flush();
        input.clear();
        refreshViews();
    }
}
