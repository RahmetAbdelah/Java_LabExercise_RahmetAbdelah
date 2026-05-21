import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Circle;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import java.io.*;

public class notepad extends Application {

    private final TextArea textArea = new TextArea();
    private final Pane shapePane = new Pane();
    private double mouseAnchorX;
    private double mouseAnchorY;

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();

        textArea.setStyle("-fx-control-inner-background: #ffffff; -fx-background-color: transparent; -fx-padding: 10;");
        shapePane.setStyle("-fx-background-color: transparent;");

        shapePane.setPickOnBounds(false);

        shapePane.setOnMousePressed(e -> {
            if (e.getTarget() == shapePane) {
                textArea.requestFocus();
            }
        });

        StackPane centerPane = new StackPane(textArea, shapePane);

        MenuBar menuBar = new MenuBar();
        menuBar.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");

        Menu fileMenu = new Menu("File");
        MenuItem newFile = new MenuItem("New"), openFile = new MenuItem("Open"), saveFile = new MenuItem("Save"), clearFile = new MenuItem("Clear");
        fileMenu.getItems().addAll(newFile, openFile, saveFile, clearFile);

        Menu editMenu = new Menu("Edit");
        MenuItem wordCount = new MenuItem("Word Count");
        editMenu.getItems().add(wordCount);

        Menu insertMenu = new Menu("Insert");
        MenuItem insertText = new MenuItem("Insert Text"), insertRectangle = new MenuItem("Rectangle"), insertCircle = new MenuItem("Circle"), insertImage = new MenuItem("Image");
        insertMenu.getItems().addAll(insertText, insertRectangle, insertCircle, insertImage);

        Menu toolsMenu = new Menu("Tools");
        MenuItem fontChoice = new MenuItem("Font Size");
        toolsMenu.getItems().add(fontChoice);

        menuBar.getMenus().addAll(fileMenu, editMenu, insertMenu, toolsMenu);
        root.setTop(menuBar);
        root.setCenter(centerPane);

        Runnable clearAll = () -> { textArea.clear(); shapePane.getChildren().clear(); };
        newFile.setOnAction(e -> clearAll.run());
        clearFile.setOnAction(e -> clearAll.run());

        openFile.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Open Text File");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files (*.txt)", "*.txt"));
            File file = fc.showOpenDialog(stage);

            if (file != null) {
                clearAll.run();
                StringBuilder sb = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    textArea.setText(sb.toString());
                } catch (IOException ex) {
                    showError("Error reading the file.");
                }
            }
        });

        saveFile.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Save Text File");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files (*.txt)", "*.txt"));
            File file = fc.showSaveDialog(stage);
            if (file != null) {
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                    bw.write(textArea.getText());
                } catch (IOException ex) {
                    showError("Error saving the file.");
                }
            }
        });

        insertText.setOnAction(e -> textArea.insertText(textArea.getCaretPosition(), "[Inserted Text] "));

        insertRectangle.setOnAction(e -> {
            Rectangle rect = new Rectangle(130, 80, Color.web("#90caf9"));
            rect.setStroke(Color.web("#1e88e5"));
            rect.setStrokeWidth(2);
            rect.setArcWidth(12); rect.setArcHeight(12);
            makeDraggable(rect);
            spawnNode(rect);
        });

        insertCircle.setOnAction(e -> {
            Circle circle = new Circle(45, Color.web("#a5d6a7"));
            circle.setStroke(Color.web("#43a047"));
            circle.setStrokeWidth(2);
            makeDraggable(circle);
            spawnNode(circle);
        });

        insertImage.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Insert Image");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));
            File file = fc.showOpenDialog(stage);
            if (file != null) {
                ImageView imageView = new ImageView(new Image(file.toURI().toString()));
                imageView.setFitWidth(160);
                imageView.setPreserveRatio(true);
                makeDraggable(imageView);
                spawnNode(imageView);
            }
        });

        wordCount.setOnAction(e -> {
            String text = textArea.getText().trim();
            int words = text.isEmpty() ? 0 : text.split("\\s+").length;
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Words: " + words, ButtonType.OK);
            alert.setTitle("Word Count");
            alert.setHeaderText(null);
            alert.showAndWait();
        });

        fontChoice.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog(String.valueOf((int)textArea.getFont().getSize()));
            dialog.setTitle("Font Size");
            dialog.setHeaderText("Enter font size:");
            dialog.showAndWait().ifPresent(size -> {
                try {
                    textArea.setFont(Font.font(Integer.parseInt(size)));
                } catch (NumberFormatException ex) {
                    showError("Invalid font size.");
                }
            });
        });

        Scene scene = new Scene(root, 900, 650);
        stage.setTitle("Notepad");
        stage.setScene(scene);
        stage.show();
    }

    private void spawnNode(Node node) {
        int offset = shapePane.getChildren().size() * 15;
        node.setLayoutX(60 + offset);
        node.setLayoutY(60 + offset);
        shapePane.getChildren().add(node);
    }

    private void makeDraggable(Node node) {
        node.setOnMousePressed(e -> {
            mouseAnchorX = e.getX();
            mouseAnchorY = e.getY();
            node.requestFocus();
            e.consume();
        });
        node.setOnMouseDragged(e -> {
            node.setLayoutX(node.getLayoutX() + e.getX() - mouseAnchorX);
            node.setLayoutY(node.getLayoutY() + e.getY() - mouseAnchorY);
            e.consume();
        });
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}