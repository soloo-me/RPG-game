package Griffith;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class GameMenu extends Application {

    private Stage window;
    private Scene mainMenuScene;
    private Scene characterSelectScene;
    private Scene settingsScene;
    private Scene battleScene;
    private Scene pauseScene;

    private double volume = 0.5;
    private boolean musicOn = true;

    private BaseCharacter selectedCharacter1;
    private BaseCharacter selectedCharacter2;

    private Label selectedNameLabel = new Label("No character selected");
    private Label statsLabel = new Label("Stats will appear here");

    private BattleManager battleManager = new BattleManager();
    private VBox archerCard;
    private VBox brawlerCard;
    private VBox mageCard;
    private VBox tankCard;

    @Override
    public void start(Stage primaryStage) {
        this.window = primaryStage;
        window.setTitle("Ooga Booga");

        mainMenuScene = createMainMenuScene();
        characterSelectScene = createCharacterSelectScene();
        settingsScene = createSettingsScene();
        battleScene = battleManager.createBattleScene(this::pauseGame);
        pauseScene = createPauseScene();
        
        battleManager.setReturnToMenuAction(() -> {
        // 1. Clear the old character selections
        selectedCharacter1 = null;
        selectedCharacter2 = null;
                
        // 2. Reset the labels on the selection screen
        selectedNameLabel.setText("No character selected");
        statsLabel.setText("Stats will appear here");

        // 3. Move the window back to the character selection screen
        window.setScene(characterSelectScene);
        });

        window.setScene(mainMenuScene);
        window.setWidth(1000);
        window.setHeight(650);
        window.centerOnScreen();
        window.show();
    }

    private Scene createMainMenuScene() {
        Label title = new Label("Ooga Booga");
        title.setFont(Font.font(34));

        Label subtitle = new Label("Keyboard for gameplay, mouse for menus");

        Button startBtn = new Button("Start Game");
        Button pauseBtn = new Button("Pause / Resume");
        Button settingsBtn = new Button("Settings");
        Button exitBtn = new Button("Exit");

        startBtn.setPrefWidth(220);
        pauseBtn.setPrefWidth(220);
        settingsBtn.setPrefWidth(220);
        exitBtn.setPrefWidth(220);

        startBtn.setOnAction(e -> window.setScene(characterSelectScene));

        pauseBtn.setOnAction(e -> {
            if (!battleManager.isGameStarted()) {
                showAlert("No game is running yet.");
            } else if (battleManager.isGamePaused()) {
                resumeGame();
            } else {
                pauseGame();
            }
        });

        settingsBtn.setOnAction(e -> window.setScene(settingsScene));
        exitBtn.setOnAction(e -> window.close());

        VBox layout = new VBox(18, title, subtitle, startBtn, pauseBtn, settingsBtn, exitBtn);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 1000, 650);
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                window.close();
            }
        });

        return scene;
    }
    private Scene createCharacterSelectScene() {
        Label title = new Label("Select Your Character");
        title.setFont(Font.font(28));
        title.setStyle(
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-effect: dropshadow(gaussian, black, 8, 0.6, 2, 2);"
        );

        Archer archer = new Archer("Archer");
        Brawler brawler = new Brawler("Brawler");
        Mage mage = new Mage("Mage");
        Tank tank = new Tank("Tank");

        archerCard = createCharacterCard(archer);
        brawlerCard = createCharacterCard(brawler);
        mageCard = createCharacterCard(mage);
        tankCard = createCharacterCard(tank);

        HBox characterRow = new HBox(18, archerCard, brawlerCard, mageCard, tankCard);
        characterRow.setAlignment(Pos.CENTER);

        selectedNameLabel.setFont(Font.font(20));
        selectedNameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        statsLabel.setStyle("-fx-font-family: monospace; -fx-text-fill: #f5e6c8;");

        Label detailsLabel = new Label("Character Details");
        detailsLabel.setStyle("-fx-text-fill: #ffd36b; -fx-font-size: 18px; -fx-font-weight: bold;");

        VBox statsBox = new VBox(12,
                detailsLabel,
                selectedNameLabel,
                statsLabel
        );
        statsBox.setPadding(new Insets(20));
        statsBox.setPrefWidth(320);
        statsBox.setStyle(
            "-fx-border-color: #8b5a2b; " +
            "-fx-border-width: 3; " +
            "-fx-background-color: rgba(0,0,0,0.65); " +
            "-fx-background-radius: 12; " +
            "-fx-border-radius: 12;"
        );

        Label difficultyLabel = new Label("Select Difficulty:");
        difficultyLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        ComboBox<String> difficultyBox = new ComboBox<>();
        difficultyBox.getItems().addAll("EASY", "MEDIUM", "HARD");
        difficultyBox.setValue("MEDIUM");
        difficultyBox.setStyle(
            "-fx-background-color: #3b2a1a;" +
            "-fx-text-fill: white;" +
            "-fx-border-color: #c89b3c;" +
            "-fx-border-width: 2;"
        );
        difficultyBox.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
                setTextFill(javafx.scene.paint.Color.WHITE);
                setStyle("-fx-background-color: #3b2a1a;");
            }
        });

        difficultyBox.setCellFactory(listView -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
                setTextFill(javafx.scene.paint.Color.WHITE);
                setStyle("-fx-background-color: #3b2a1a;");
            }
        });

        Button confirmBtn = new Button("Confirm Selection");
        Button backBtn = new Button("Back");

        confirmBtn.setStyle(
            "-fx-background-color: linear-gradient(#6b4f2a, #3e2d18);" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: #d6b36a;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 10;" +
            "-fx-padding: 10 18 10 18;"
        );

        backBtn.setStyle(
            "-fx-background-color: linear-gradient(#5a5a5a, #2f2f2f);" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: #bbbbbb;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 10;" +
            "-fx-padding: 10 18 10 18;"
        );

        confirmBtn.setOnAction(e -> {
            if (selectedCharacter1 == null || selectedCharacter2 == null) {
                showAlert("Select 2 characters first.");
                return;
            }

            battleManager.setDifficulty(difficultyBox.getValue());
            battleManager.startBattle(selectedCharacter1, selectedCharacter2);
            window.setScene(battleScene);
            battleScene.getRoot().requestFocus();
        });

        backBtn.setOnAction(e -> window.setScene(mainMenuScene));

        VBox bottomSection = new VBox(10,
                difficultyLabel,
                difficultyBox,
                new HBox(15, confirmBtn, backBtn)
        );
        bottomSection.setAlignment(Pos.CENTER);
        bottomSection.setStyle("-fx-background-color: rgba(0,0,0,0.35); -fx-padding: 12;");

        // Background layer
        ImageView bg = new ImageView(new Image("file:pictures/Game_background.png"));
        bg.setPreserveRatio(false);

        Pane overlay = new Pane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.55);");

        // Content layer
        BorderPane content = new BorderPane();
        content.setTop(title);
        BorderPane.setAlignment(title, Pos.CENTER);
        BorderPane.setMargin(title, new Insets(20));

        content.setCenter(characterRow);
        content.setRight(statsBox);
        content.setBottom(bottomSection);
        BorderPane.setMargin(bottomSection, new Insets(20));
        BorderPane.setMargin(statsBox, new Insets(20));

        StackPane root = new StackPane(bg, overlay, content);
        bg.fitWidthProperty().bind(root.widthProperty());
        bg.fitHeightProperty().bind(root.heightProperty());
        overlay.prefWidthProperty().bind(root.widthProperty());
        overlay.prefHeightProperty().bind(root.heightProperty());

        Scene scene = new Scene(root, 1000, 650);
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                window.setScene(mainMenuScene);
            }
            if (event.getCode() == KeyCode.ENTER && selectedCharacter1 != null && selectedCharacter2 != null) {
                battleManager.setDifficulty(difficultyBox.getValue());
                battleManager.startBattle(selectedCharacter1, selectedCharacter2);
                window.setScene(battleScene);
                battleScene.getRoot().requestFocus();
            }
        });

        return scene;
    }
    private VBox createCharacterCard(BaseCharacter character) {
        Label nameLabel = new Label(character.getName());
        nameLabel.setFont(Font.font(18));
        nameLabel.setStyle(
            "-fx-text-fill: #ffd36b;" +
            "-fx-font-weight: bold;" +
            "-fx-effect: dropshadow(gaussian, black, 4, 0.5, 1, 1);"
        );

        Label hp = new Label("HP: " + character.getHp());
        hp.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        Label atk = new Label("ATK: " + character.getAttack());
        atk.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        Button selectBtn = new Button("Select");
        selectBtn.setPrefWidth(100);

        VBox card = new VBox(10, nameLabel, hp, atk, selectBtn);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setPrefWidth(160);
        card.setMinHeight(220);

        Runnable updateCardAppearance = () -> {
            boolean selected = (selectedCharacter1 == character || selectedCharacter2 == character);

            if (selected) {
                card.setStyle(
                    "-fx-background-color: rgba(72, 48, 20, 0.92);" +
                    "-fx-border-color: #ffd36b;" +
                    "-fx-border-width: 4;" +
                    "-fx-background-radius: 14;" +
                    "-fx-border-radius: 14;" +
                    "-fx-effect: dropshadow(gaussian, rgba(255,211,107,0.35), 14, 0.4, 0, 0);"
                );

                selectBtn.setText("Selected");
                selectBtn.setStyle(
                    "-fx-background-color: linear-gradient(#b88a2a, #7a5818);" +
                    "-fx-text-fill: white;" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-radius: 10;" +
                    "-fx-border-color: #fff0b3;" +
                    "-fx-border-width: 2;" +
                    "-fx-border-radius: 10;" +
                    "-fx-padding: 8 14 8 14;"
                );
            } else {
                card.setStyle(
                    "-fx-background-color: rgba(32, 22, 14, 0.82);" +
                    "-fx-border-color: #8b5a2b;" +
                    "-fx-border-width: 3;" +
                    "-fx-background-radius: 14;" +
                    "-fx-border-radius: 14;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.45), 10, 0.3, 0, 4);"
                );

                selectBtn.setText("Select");
                selectBtn.setStyle(
                    "-fx-background-color: linear-gradient(#6b4f2a, #3e2d18);" +
                    "-fx-text-fill: white;" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-radius: 10;" +
                    "-fx-border-color: #d6b36a;" +
                    "-fx-border-width: 2;" +
                    "-fx-border-radius: 10;" +
                    "-fx-padding: 8 14 8 14;"
                );
            }
        };

        selectBtn.setOnAction(e -> {
            if (selectedCharacter1 == character) {
                selectedCharacter1 = null;
            } else if (selectedCharacter2 == character) {
                selectedCharacter2 = null;
            } else if (selectedCharacter1 == null) {
                selectedCharacter1 = character;
            } else if (selectedCharacter2 == null) {
                selectedCharacter2 = character;
            } else {
                showAlert("You already selected 2 characters. Deselect one first.");
                return;
            }

            String player1Name = (selectedCharacter1 == null) ? "None" : selectedCharacter1.getName();
            String player2Name = (selectedCharacter2 == null) ? "None" : selectedCharacter2.getName();

            selectedNameLabel.setText("Player 1: " + player1Name + " | Player 2: " + player2Name);
            statsLabel.setText(buildStatsText(character));

            refreshCharacterCards();
        });

        card.setOnMouseEntered(e -> {
            boolean selected = (selectedCharacter1 == character || selectedCharacter2 == character);
            if (!selected) {
                card.setStyle(
                    "-fx-background-color: rgba(48, 32, 18, 0.9);" +
                    "-fx-border-color: #c89b3c;" +
                    "-fx-border-width: 3;" +
                    "-fx-background-radius: 14;" +
                    "-fx-border-radius: 14;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.55), 14, 0.35, 0, 4);"
                );
            }
        });

        card.setOnMouseExited(e -> updateCardAppearance.run());

        // set initial look
        updateCardAppearance.run();

        // store updater on the node itself so refreshCharacterCards can call it later
        card.setUserData(updateCardAppearance);

        return card;
    }
    private void refreshCharacterCards() {
        if (archerCard != null && archerCard.getUserData() instanceof Runnable updater) {
            updater.run();
        }
        if (brawlerCard != null && brawlerCard.getUserData() instanceof Runnable updater) {
            updater.run();
        }
        if (mageCard != null && mageCard.getUserData() instanceof Runnable updater) {
            updater.run();
        }
        if (tankCard != null && tankCard.getUserData() instanceof Runnable updater) {
            updater.run();
        }
    }

    // Settings
    private Scene createSettingsScene() {
        Label title = new Label("Settings");
        title.setFont(Font.font(28));

        Label volumeLabel = new Label("Volume");
        Slider volumeSlider = new Slider(0, 100, volume * 100);
        volumeSlider.setShowTickLabels(true);
        volumeSlider.setShowTickMarks(true);

        CheckBox musicCheckbox = new CheckBox("Music On");
        musicCheckbox.setSelected(musicOn);

        Button saveBtn = new Button("Save");
        Button backBtn = new Button("Back");

        saveBtn.setOnAction(e -> {
            volume = volumeSlider.getValue() / 100.0;
            musicOn = musicCheckbox.isSelected();
            showAlert("Settings saved.");
        });

        backBtn.setOnAction(e -> {
            if (battleManager.isGamePaused()) {
                window.setScene(pauseScene);
            } else {
                window.setScene(mainMenuScene);
            }
        });

        VBox layout = new VBox(20, title, volumeLabel, volumeSlider, musicCheckbox, saveBtn, backBtn);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));

        Scene scene = new Scene(layout, 1000, 650);
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                if (battleManager.isGamePaused()) {
                    window.setScene(pauseScene);
                } else {
                    window.setScene(mainMenuScene);
                }
            }
        });

        return scene;
    }

    private Scene createPauseScene() {
        Label title = new Label("Paused");
        title.setFont(Font.font(28));

        Button resumeBtn = new Button("Resume");
        Button settingsBtn = new Button("Settings");
        Button mainMenuBtn = new Button("Main Menu");

        resumeBtn.setOnAction(e -> resumeGame());
        settingsBtn.setOnAction(e -> window.setScene(settingsScene));
        mainMenuBtn.setOnAction(e -> {
            window.setScene(mainMenuScene);
        });

        VBox layout = new VBox(18, title, resumeBtn, settingsBtn, mainMenuBtn);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 1000, 650);
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE || event.getCode() == KeyCode.P) {
                resumeGame();
            }
        });

        return scene;
    }

    private void pauseGame() {
        if (!battleManager.isGameStarted()) {
            showAlert("No game is running.");
            return;
        }
        battleManager.pauseGame();
        window.setScene(pauseScene);
    }

    private void resumeGame() {
        if (!battleManager.isGameStarted()) {
            showAlert("No game is running.");
            return;
        }
        battleManager.resumeGame();
        window.setScene(battleScene);
        battleScene.getRoot().requestFocus();
    }

    private String buildStatsText(BaseCharacter character) {
        StringBuilder sb = new StringBuilder();
        sb.append("HP: ").append(character.getHp()).append("\n");
        sb.append("Stamina: ").append(character.getStamina()).append("\n");
        sb.append("Agility: ").append(character.getAgility()).append("\n");
        sb.append("Intellect: ").append(character.getIntellect()).append("\n");
        sb.append("Attack: ").append(character.getAttack());

        if (character instanceof Mage mage) {
            sb.append("\nMana: ").append(mage.getMana());
        }
        return sb.toString();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
