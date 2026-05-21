import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.*;

public class Pocker extends Application {

    static class Card {
        String rank;
        String suit;

        Card(String rank, String suit) {
            this.rank = rank;
            this.suit = suit;
        }

        public String toString() {
            return rank + suit;
        }
    }

    private HBox playerBox = new HBox(20);
    private HBox cpuBox = new HBox(20);
    private Label result = new Label("Click DEAL to start");

    private Button dealBtn;
    private Button resetBtn;

    @Override
    public void start(Stage stage) {

        dealBtn = new Button("DEAL");
        resetBtn = new Button("NEW GAME");

        dealBtn.setStyle("-fx-font-size: 18; -fx-padding: 10 20;");
        resetBtn.setStyle("-fx-font-size: 18; -fx-padding: 10 20;");

        dealBtn.setOnAction(e -> dealHands());
        resetBtn.setOnAction(e -> resetGame());

        playerBox.setAlignment(Pos.CENTER);
        cpuBox.setAlignment(Pos.CENTER);

        HBox buttonRow = new HBox(30, dealBtn, resetBtn);
        buttonRow.setAlignment(Pos.CENTER);

        VBox root = new VBox(30,
                new Label("PLAYER HAND"),
                playerBox,
                new Label("CPU HAND"),
                cpuBox,
                buttonRow,
                result
        );

        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-padding: 30; -fx-font-size: 20; -fx-background-color: #0b6623;");

        stage.setScene(new Scene(root, 900, 700));
        stage.setTitle("Poker Game (Player vs CPU)");
        stage.show();
    }

    private void dealHands() {

        String[] ranks = {"A","2","3","4","5","6","7","8","9","10","J","Q","K"};
        String[] suits = {"♠️","♥️","♦️","♣️"};

        ArrayList<Card> deck = new ArrayList<>();
        for (String r : ranks)
            for (String s : suits)
                deck.add(new Card(r, s));

        Collections.shuffle(deck);

        ArrayList<Card> player = new ArrayList<>();
        ArrayList<Card> cpu = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            player.add(deck.remove(0));
            cpu.add(deck.remove(0));
        }

        showCards(playerBox, player);
        showCards(cpuBox, cpu);

        String p = evaluate(player);
        String c = evaluate(cpu);

        if (rank(p) > rank(c)) {
            result.setText("Player: " + p + "   CPU: " + c + "   → PLAYER WINS!");
        } else if (rank(c) > rank(p)) {
            result.setText("Player: " + p + "   CPU: " + c + "   → CPU WINS!");
        } else {
            result.setText("Player: " + p + "   CPU: " + c + "   → TIE! Press DEAL to reshuffle.");
        }
    }

    private void resetGame() {
        playerBox.getChildren().clear();
        cpuBox.getChildren().clear();
        result.setText("Click DEAL to start");
    }

    private void showCards(HBox box, List<Card> hand) {
        box.getChildren().clear();

        for (Card c : hand) {
            Label card = new Label(formatCard(c));
            card.setMinSize(90, 130);
            card.setAlignment(Pos.CENTER);
            card.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-border-color: black;" +
                            "-fx-border-radius: 10;" +
                            "-fx-background-radius: 10;" +
                            "-fx-border-width: 2;" +
                            "-fx-font-size: 28;" +
                            "-fx-padding: 10;" +
                            "-fx-effect: dropshadow(gaussian, black, 8, 0.3, 2, 2);"
            );
            box.getChildren().add(card);
        }
    }

    private String formatCard(Card c) {
        return c.rank + "\n" + c.suit;
    }

    private String evaluate(List<Card> hand) {
        HashMap<String, Integer> map = new HashMap<>();
        HashSet<String> suits = new HashSet<>();

        for (Card c : hand) {
            map.put(c.rank, map.getOrDefault(c.rank, 0) + 1);
            suits.add(c.suit);
        }

        Collection<Integer> count = map.values();

        if (suits.size() == 1)
            return "Flush";

        if (count.contains(4))
            return "Four of a Kind";

        if (count.contains(3) && count.contains(2))
            return "Full House";

        if (count.contains(3))
            return "Three of a Kind";

        int pairs = 0;
        for (int x : count)
            if (x == 2)
                pairs++;

        if (pairs == 2)
            return "Two Pair";

        if (pairs == 1)
            return "One Pair";

        return "High Card";
    }

    private int rank(String h) {
        return switch (h) {
            case "Flush" -> 5;
            case "Four of a Kind" -> 4;
            case "Full House" -> 3;
            case "Three of a Kind" -> 2;
            case "Two Pair" -> 1;
            case "One Pair" -> 0;
            default -> -1;
        };
    }

    public static void main(String[] args) {
        launch();
    }
}
