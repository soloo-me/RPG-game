package Griffith;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
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

    private BaseCharacter selectedCharacter;

    private Label selectedNameLabel = new Label("No character selected");
    private Label statsLabel = new Label("Stats will appear here");

    private BattleManager battleManager = new BattleManager();

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
            window.setScene(mainMenuScene);
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

        Archer archer = new Archer("Archer");
        Brawler brawler = new Brawler("Brawler");
        Mage mage = new Mage("Mage");
        Tank tank = new Tank("Tank");

        VBox archerCard = createCharacterCard(archer);
        VBox brawlerCard = createCharacterCard(brawler);
        VBox mageCard = createCharacterCard(mage);
        VBox tankCard = createCharacterCard(tank);

        HBox characterRow = new HBox(18, archerCard, brawlerCard, mageCard, tankCard);
        characterRow.setAlignment(Pos.CENTER);

        selectedNameLabel.setFont(Font.font(20));
        statsLabel.setStyle("-fx-font-family: monospace;");

        VBox statsBox = new VBox(12,
                new Label("Character Details"),
                selectedNameLabel,
                statsLabel
        );
        statsBox.setPadding(new Insets(20));
        statsBox.setPrefWidth(320);
        statsBox.setStyle("-fx-border-color: black; -fx-border-width: 2; -fx-background-color: #f7f7f7;");

        Button confirmBtn = new Button("Confirm Selection");
        Button backBtn = new Button("Back");

        confirmBtn.setOnAction(e -> {
            if (selectedCharacter == null) {
                showAlert("Select a character first.");
                return;
            }
            battleManager.startBattle(selectedCharacter);
            window.setScene(battleScene);
            battleScene.getRoot().requestFocus();
        });

        backBtn.setOnAction(e -> window.setScene(mainMenuScene));

        HBox bottomButtons = new HBox(15, confirmBtn, backBtn);
        bottomButtons.setAlignment(Pos.CENTER);

        BorderPane root = new BorderPane();
        root.setTop(title);
        BorderPane.setAlignment(title, Pos.CENTER);
        BorderPane.setMargin(title, new Insets(20));
        root.setCenter(characterRow);
        root.setRight(statsBox);
        root.setBottom(bottomButtons);
        BorderPane.setMargin(bottomButtons, new Insets(20));

        Scene scene = new Scene(root, 1000, 650);
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                window.setScene(mainMenuScene);
            }
            if (event.getCode() == KeyCode.ENTER && selectedCharacter != null) {
                battleManager.startBattle(selectedCharacter);
                window.setScene(battleScene);
                battleScene.getRoot().requestFocus();
            }
        });

        return scene;
    }

    private VBox createCharacterCard(BaseCharacter character) {
        Label nameLabel = new Label(character.getName());
        nameLabel.setFont(Font.font(18));

        Label hp = new Label("HP: " + character.getHp());
        Label atk = new Label("ATK: " + character.getAttack());

        Button selectBtn = new Button("Select");
        selectBtn.setOnAction(e -> {
            selectedCharacter = character;
            selectedNameLabel.setText("Selected: " + selectedCharacter.getName());
            statsLabel.setText(buildStatsText(selectedCharacter));
        });

        VBox card = new VBox(10, nameLabel, hp, atk, selectBtn);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setPrefWidth(150);
        card.setStyle("-fx-border-color: gray; -fx-border-width: 2; -fx-background-color: #efefef;");

        return card;
    }

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
