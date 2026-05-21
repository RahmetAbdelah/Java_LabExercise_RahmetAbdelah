package Pocker;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.io.*;
import java.net.*;

public class PockerGame extends Application {

    private final HBox myBox = new HBox(15);
    private final HBox oppBox = new HBox(15);
    private final Label statusLabel = new Label("Connecting...");
    private Button dealBtn = new Button("DEAL");
    private Button revealBtn = new Button("REVEAL RESULT");

    private PrintWriter out;
    private BufferedReader in;
    private String savedOpponentData = "";
    private String savedResultText = "";

    @Override
    public void start(Stage stage) {
        dealBtn.setDisable(true);
        revealBtn.setDisable(true);

        dealBtn.setOnAction(e -> {
            out.println("DEAL");
            revealBtn.setDisable(true);
        });

        revealBtn.setOnAction(e -> {
            showCards(oppBox, savedOpponentData, false);
            revealBtn.setDisable(true);
            statusLabel.setText(savedResultText);
        });

        myBox.setAlignment(Pos.CENTER);
        oppBox.setAlignment(Pos.CENTER);

        VBox root = new VBox(20,
                new Label("YOUR HAND"), myBox,
                new Label("OPPONENT HAND"), oppBox,
                new HBox(20, dealBtn, revealBtn), statusLabel
        );
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-padding: 25; -fx-font-size: 18; -fx-background-color: #0b6623;");

        stage.setScene(new Scene(root, 800, 600));
        stage.setTitle("5 card pocker");
        stage.show();

        connect();
    }

    private void connect() {
        new Thread(() -> {
            try {
                Socket socket = new Socket("localhost", 12345);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String line;
                while ((line = in.readLine()) != null) {
                    String msg = line;
                    Platform.runLater(() -> parseMessage(msg));
                }
            } catch (IOException e) {
                Platform.runLater(() -> statusLabel.setText("Connection failed."));
            }
        }).start();
    }

    private void parseMessage(String msg) {
        if (msg.startsWith("PLAYER:")) {
            statusLabel.setText("Connected as Player " + msg.split(":")[1]);
            dealBtn.setDisable(false);
        } else if (msg.startsWith("CARDS:")) {
            String[] parts = msg.substring(6).split("\\|");
            String myCards = parts[0];
            savedOpponentData = parts[1];
            savedResultText = parts[2];

            showCards(myBox, myCards, false);
            showCards(oppBox, savedOpponentData, true);

            statusLabel.setText("Cards dealt! Check your hand, then click Reveal to see who won.");
            revealBtn.setDisable(false);
        }
    }

    private void showCards(HBox box, String cardData, boolean hide) {
        box.getChildren().clear();
        String[] cards = cardData.split(",");
        for (String c : cards) {
            if (c.trim().isEmpty()) continue;

            Label card = new Label(hide ? "?" : c);
            card.setMinSize(80, 120);
            card.setAlignment(Pos.CENTER);

            String txtColor = "black";
            if (!hide && (c.contains("♥") || c.contains("♦"))) {
                txtColor = "red";
            }

            card.setStyle("-fx-background-color: white; -fx-border-color: black; " +
                    "-fx-border-radius: 5; -fx-background-radius: 5; " +
                    "-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: " + txtColor);
            box.getChildren().add(card);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
