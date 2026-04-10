package Griffith;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;



public class BattleManager {

    private BaseCharacter[] players = new BaseCharacter[2];
    private BaseCharacter enemy;
    private int currentPlayerIndex = 0;

    private Runnable returnToMenuAction;

    
    public void setReturnToMenuAction(Runnable action) {
        this.returnToMenuAction = action;
    }
    
    private boolean gameStarted = false;
    private boolean gamePaused = false;
    private boolean playerTurn = true;

    private int[] playerMaxHp = new int[2];
    private int[] playerMaxStamina = new int[2];
    private int enemyMaxHp;

    private ProgressBar player1HpBar = new ProgressBar(1.0);
    private ProgressBar player2HpBar = new ProgressBar(1.0);
    private ProgressBar player1StaminaBar = new ProgressBar(1.0);
    private ProgressBar player2StaminaBar = new ProgressBar(1.0);
    private ProgressBar enemyHpBar = new ProgressBar(1.0);
    
    private Label player1HpLabel = new Label();
    private Label player2HpLabel = new Label();
    private Label player1StaminaLabel = new Label();
    private Label player2StaminaLabel = new Label();
    private Label enemyHpLabel = new Label();
    private Label battleInfoLabel = new Label("Press Start Game, choose a character, and begin.");

    private ImageView player1Sprite;
    private ImageView player2Sprite; 
    private ImageView bossSprite; 
    
    private void showEndGameScreen(boolean isVictory) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText(isVictory ? "Victory!" : "Defeat!");
        alert.setContentText(isVictory ? "You defeated the boss! Play again?" : "You have fallen. Try again?");

       
        ButtonType yesButton = new ButtonType("Yes");
        ButtonType noButton = new ButtonType("No");
        alert.getButtonTypes().setAll(yesButton, noButton);

        
        alert.showAndWait().ifPresent(type -> {
            if (type == yesButton) {
                
            if (returnToMenuAction != null) {
                    returnToMenuAction.run();
                }
            } else {
                
                Platform.exit();
            }
        });
    }
    
    public Scene createBattleScene(Runnable pauseAction) {
        Label title = new Label("Battle");
        title.setFont(Font.font(28));
        
        Pane battlePane = new Pane(); 

        ImageView background = new ImageView(new Image("file:pictures/Game_Background.png")); 
        background.setPreserveRatio(false);
        background.fitWidthProperty().bind(battlePane.widthProperty());
        background.fitHeightProperty().bind(battlePane.heightProperty());
        
        player1Sprite = new ImageView(); 
        player2Sprite = new ImageView(); 
        bossSprite = new ImageView(); 
        
     //Scale images
        player1Sprite.setPreserveRatio(true);
        player1Sprite.setFitWidth(200);

        player2Sprite.setPreserveRatio(true);
        player2Sprite.setFitWidth(200);

        bossSprite.setPreserveRatio(true);
        bossSprite.setFitWidth(450);

        // positions
        player1Sprite.setLayoutX(450);
        player1Sprite.setLayoutY(300);

        player2Sprite.setLayoutX(600);
        player2Sprite.setLayoutY(230);

        bossSprite.setLayoutX(915);
        bossSprite.setLayoutY(-70);

        battlePane.getChildren().addAll(background, bossSprite, player1Sprite, player2Sprite);

        VBox player1Box = new VBox(10,
                new Label("Player"),
                player1HpBar,
                player1HpLabel,
                new Label("Stamina:"), // Label to identify the bar
                player1StaminaBar,
                player1StaminaLabel
        );
        player1Box.setAlignment(Pos.CENTER);
        player1Box.setPadding(new Insets(15));
        player1Box.setStyle("-fx-border-color: green; -fx-border-width: 2;");

        VBox player2Box = new VBox(10, 
                new Label("Player 2"), 
                player2HpBar, 
                player2HpLabel,
                new Label("Stamina:"),
                player2StaminaBar,
                player2StaminaLabel
        );
        player2Box.setAlignment(Pos.CENTER); 
        player2Box.setPadding(new Insets(15)); 
        player2Box.setStyle("-fx-border-color: dodgerblue; -fx-border-width: 2;"); 

        VBox enemyBox = new VBox(10,
                new Label("Enemy Boss"),
                enemyHpBar,
                enemyHpLabel
        );
        enemyBox.setAlignment(Pos.CENTER);
        enemyBox.setPadding(new Insets(15));
        enemyBox.setStyle("-fx-border-color: darkred; -fx-border-width: 2;");

        HBox topBars = new HBox(25, player1Box, player2Box, enemyBox);
        topBars.setAlignment(Pos.CENTER);

        Label controls = new Label("Battle controls: A = Attack | H = Heal | P or ESC = Pause");
        controls.setTextFill(Color.DARKBLUE);

        battleInfoLabel.setWrapText(true);
        battleInfoLabel.setPrefWidth(650);
        battleInfoLabel.setFont(Font.font(16));

        VBox centerBox = new VBox(18, title, topBars, controls, battleInfoLabel);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setPadding(new Insets(25));

        BorderPane root = new BorderPane();
        root.setCenter(new StackPane(battlePane,centerBox));

        Scene scene = new Scene(root, 1000, 650);

        scene.setOnKeyPressed(event -> {
            if (!gameStarted || gamePaused || players[0] == null || enemy == null || players[1] == null) {
                if (event.getCode() == KeyCode.P || event.getCode() == KeyCode.ESCAPE) {
                    pauseAction.run();
                }
                return;
            }

            switch (event.getCode()) {
                case A -> playerAttack();
                case H -> playerHeal();
                case P, ESCAPE -> pauseAction.run();
                default -> { }
            }
        });

        return scene;
    }
    

    public void startBattle(BaseCharacter selectedCharacter1, BaseCharacter selectedCharacter2) {
        this.players[0] = cloneCharacter(selectedCharacter1);
        this.players[1] = cloneCharacter(selectedCharacter2);
        this.enemy = new boss("Gorgon");

        this.playerMaxHp[0] = players[0].getHp();
        this.playerMaxHp[1] = players[1].getHp();
        this.playerMaxStamina[0] = players[0].getStamina();
        this.playerMaxStamina[1] = players[1].getStamina();
        this.enemyMaxHp = enemy.getHp();

        this.gameStarted = true;
        this.gamePaused = false;
        this.playerTurn = true;
        this.currentPlayerIndex = 0;

        battleInfoLabel.setText(players[0].getName() +" and " + players[1].getName() + " enters the battle against " + enemy.getName() + ".\n" + players[currentPlayerIndex].getName() + "'s turn. Press A to attack.");
        
        player1Sprite.setImage(getSpriteForCharacter(players[0]));
        player2Sprite.setImage(getSpriteForCharacter(players[1]));
        bossSprite.setImage(new Image("file:pictures/Boss.png")); //file name spelling error on purpose because image isn't where it's supposed to be when you run it
        bossSprite.setScaleX(-1);
        bossSprite.setRotate(-10);
        
        updateBattleUi();
    }

    public void pauseGame() {
        if (gameStarted) {
            gamePaused = true;
        }
    }

    public void resumeGame() {
        if (gameStarted) {
            gamePaused = false;
        }
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public boolean isGamePaused() {
        return gamePaused;
    }

    public String getBattleStatus() {
        return battleInfoLabel.getText();
    }

    private void playerAttack() {
        if (!playerTurn) {
            return;
        }
        BaseCharacter currentPlayer = players[currentPlayerIndex]; 

        if (currentPlayer == null || !currentPlayer.isAlive()) { 
            nextTurnAfterPlayerAction(); 
            return;
        }// Check if they have enough stamina (e.g., 20)
        if (currentPlayer.getStamina() < 20) {
            battleInfoLabel.setText(currentPlayer.getName() + " is too tired! Use Heal to rest.");
            return; 
        }

        // Subtract stamina
        currentPlayer.setStamina(currentPlayer.getStamina() - 20);
    

        int damage = currentPlayer.calculateAttack();
        int before = enemy.getHp();
        enemy.takeDamage(damage);
        int dealt = before - enemy.getHp();

        battleInfoLabel.setText(currentPlayer.getName() + " attacks for " + dealt + " damage.");
        updateBattleUi();

        if (!enemy.isAlive()) {
            battleInfoLabel.setText(currentPlayer.getName() + " defeated the boss. You win!");
            gameStarted = false;
            
            showEndGameScreen(true); 
            return;
        }

        nextTurnAfterPlayerAction();
    }

    private void playerHeal() {
        if (!playerTurn) {
            return;
        }

        BaseCharacter currentPlayer = players[currentPlayerIndex]; 
        int healAmount = 18;
     // Restore stamina (e.g., +30) but don't go over the max
        int restoredStamina = Math.min(playerMaxStamina[currentPlayerIndex], currentPlayer.getStamina() + 30);
        currentPlayer.setStamina(restoredStamina);

        int newHp = Math.min(playerMaxHp[currentPlayerIndex], currentPlayer.getHp() + healAmount); 
        int actualHeal = newHp - currentPlayer.getHp();
        currentPlayer.setHp(newHp); 

        battleInfoLabel.setText(currentPlayer.getName() + " heals for " + actualHeal + " HP."); 
        updateBattleUi();

        nextTurnAfterPlayerAction(); 
    }
    private void nextTurnAfterPlayerAction() { 
        if (!anyPlayerAlive()) { 
            battleInfoLabel.setText("Both heroes have fallen. Game over."); 
            gameStarted = false; 
            showEndGameScreen(false); 
            return; 
        }

        if (currentPlayerIndex == 0) { 
            if (players[1] != null && players[1].isAlive()) { 
                currentPlayerIndex = 1; 
                battleInfoLabel.setText(battleInfoLabel.getText() + "\n" + players[1].getName() + "'s turn."); 
            } else { 
                playerTurn = false; 
                enemyTurn();
            }
        } else { 
            playerTurn = false; 
            enemyTurn(); 
        }
    }
    
    private void enemyTurn() {
        if (!enemy.isAlive() || !anyPlayerAlive()) {
            return;
        }
        BaseCharacter target = chooseBossTarget(); 
        if (target == null) { 
            battleInfoLabel.setText("Both heroes have fallen. Game over."); 
            gameStarted = false;
            showEndGameScreen(false);
            return; 
        }

        int damage = enemy.calculateAttack();
        int before = target.getHp();
        target.takeDamage(damage);
        int dealt = before - target.getHp();

        if (dealt <= 0) {
            battleInfoLabel.setText(battleInfoLabel.getText() + "\n" + enemy.getName() + " attacks " + target.getName() + ", but it deals no damage.");
        } else {
            battleInfoLabel.setText(battleInfoLabel.getText() + "\n" + enemy.getName() + " hits " + target.getName() + " back for " + dealt + " damage.");
        }

        updateBattleUi();

        if (!anyPlayerAlive()) {
            battleInfoLabel.setText(battleInfoLabel.getText() + "\nBoth heroes have fallen. Game over.");
            gameStarted = false;
            
            showEndGameScreen(false);
            return;
        }

        playerTurn = true;
        currentPlayerIndex = players[0].isAlive() ? 0 : 1;
        battleInfoLabel.setText(
                battleInfoLabel.getText() + "\n" +
                players[currentPlayerIndex].getName() + "'s turn."
        ); 
    }
    private BaseCharacter chooseBossTarget() {
        boolean p1Alive = players[0] != null && players[0].isAlive();
        boolean p2Alive = players[1] != null && players[1].isAlive();

        if (p1Alive && p2Alive) {
            return Math.random() < 0.5 ? players[0] : players[1];
        } else if (p1Alive) { 
            return players[0];
        } else if (p2Alive) { 
            return players[1];
        }

        return null;
    }

    private boolean anyPlayerAlive() {
        return (players[0] != null && players[0].isAlive()) ||
               (players[1] != null && players[1].isAlive());
    }

    private void updateBattleUi() {
        if (players[0] != null) {
            // HP Logic
            player1HpBar.setProgress((double) players[0].getHp() / playerMaxHp[0]);
            player1HpLabel.setText(players[0].getName() + " HP: " + players[0].getHp());
            
            // Stamina Logic - Using the array you just declared
            player1StaminaBar.setProgress((double) players[0].getStamina() / playerMaxStamina[0]);
            player1StaminaLabel.setText("Stamina: " + players[0].getStamina());
            player1StaminaBar.setStyle("-fx-accent: #e67e22;"); // Orange/Gold color
        }

        if (players[1] != null) {
            player2HpBar.setProgress((double) players[1].getHp() / playerMaxHp[1]);
            player2HpLabel.setText(players[1].getName() + " HP: " + players[1].getHp());
            
            player2StaminaBar.setProgress((double) players[1].getStamina() / playerMaxStamina[1]);
            player2StaminaLabel.setText("Stamina: " + players[1].getStamina());
            player2StaminaBar.setStyle("-fx-accent: #e67e22;");
        }

        if (enemy != null) {
            enemyHpBar.setProgress((double) enemy.getHp() / enemyMaxHp);
            enemyHpLabel.setText(enemy.getName() + " HP: " + enemy.getHp());
        }
    }
    private Image getSpriteForCharacter(BaseCharacter character) {
        if (character instanceof Archer) {
            return new Image("file:pictures/Archer.png"); //file name spelling errors on purpose because we still need to work on the character images
        }
        if (character instanceof Brawler) {
            return new Image("file:pictures/Brawler.png");  //when i run the characters are not where they should be
        }
        if (character instanceof Mage) {
            return new Image("file:pictures/Mage.png");
        }
        if (character instanceof Tank) {
            return new Image("file:pictures/Tank.png");
        }

        return null;
    }

    private BaseCharacter cloneCharacter(BaseCharacter character) {
        if (character instanceof Archer) return new Archer(character.getName());
        if (character instanceof Brawler) return new Brawler(character.getName());
        if (character instanceof Mage) return new Mage(character.getName());
        if (character instanceof Tank) return new Tank(character.getName());
        throw new IllegalArgumentException("Unknown character type");
    }
}