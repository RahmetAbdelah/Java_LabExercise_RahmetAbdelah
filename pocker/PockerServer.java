package Pocker;
import java.io.*;
import java.net.*;
import java.util.*;

public class PockerServer {
    private static final List<PrintWriter> players = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        ServerSocket server = new ServerSocket(12345);
        System.out.println("Server started. Waiting for 2 players...");

        while (players.size() < 2) {
            Socket socket = server.accept();
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            players.add(out);
            out.println("PLAYER:" + players.size());

            int id = players.size();
            new Thread(() -> handlePlayer(socket, id)).start();
        }
    }

    private static void handlePlayer(Socket socket, int id) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                if (line.equals("DEAL")) {
                    dealCards();
                }
            }
        } catch (IOException e) {
            System.out.println("Player left.");
        }
    }

    private static synchronized void dealCards() {
        String[] ranks = {"A","2","3","4","5","6","7","8","9","10","J","Q","K"};
        String[] suits = {"♠","♥","♦","♣"};

        List<String> deck = new ArrayList<>();
        for (String r : ranks) {
            for (String s : suits) {
                deck.add(r + s);
            }
        }
        Collections.shuffle(deck);

        List<String> p1Hand = new ArrayList<>();
        List<String> p2Hand = new ArrayList<>();
        String p1Data = "";
        String p2Data = "";

        for (int i = 0; i < 5; i++) {
            String c1 = deck.remove(0);
            p1Hand.add(c1);
            p1Data += c1 + ",";

            String c2 = deck.remove(0);
            p2Hand.add(c2);
            p2Data += c2 + ",";
        }

        String p1Eval = evaluate(p1Hand);
        String p2Eval = evaluate(p2Hand);
        String resultText;

        if (getRankScore(p1Eval) > getRankScore(p2Eval)) {
            resultText = "PLAYER 1 WINS with " + p1Eval + "! (Player 2 had " + p2Eval + ")";
        } else if (getRankScore(p2Eval) > getRankScore(p1Eval)) {
            resultText = "PLAYER 2 WINS with " + p2Eval + "! (Player 1 had " + p1Eval + ")";
        } else {
            resultText = "IT'S A TIE! Both players had " + p1Eval;
        }

        players.get(0).println("CARDS:" + p1Data + "|" + p2Data + "|" + resultText);
        players.get(1).println("CARDS:" + p2Data + "|" + p1Data + "|" + resultText);
    }

    private static String evaluate(List<String> hand) {
        Map<String, Integer> map = new HashMap<>();
        Set<Character> suits = new HashSet<>();

        for (String card : hand) {
            String rank = card.substring(0, card.length() - 1);
            char suit = card.charAt(card.length() - 1);
            map.put(rank, map.getOrDefault(rank, 0) + 1);
            suits.add(suit);
        }

        Collection<Integer> counts = map.values();

        if (suits.size() == 1) return "Flush";
        if (counts.contains(4)) return "Four of a Kind";
        if (counts.contains(3) && counts.contains(2)) return "Full House";
        if (counts.contains(3)) return "Three of a Kind";

        int pairs = 0;
        for (int x : counts) {
            if (x == 2) pairs++;
        }

        if (pairs == 2) return "Two Pair";
        if (pairs == 1) return "One Pair";

        return "High Card";
    }

    private static int getRankScore(String handName) {
        return switch (handName) {
            case "Flush" -> 5;
            case "Four of a Kind" -> 4;
            case "Full House" -> 3;
            case "Three of a Kind" -> 2;
            case "Two Pair" -> 1;
            case "One Pair" -> 0;
            default -> -1;
        };
    }
}
