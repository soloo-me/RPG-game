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
import javafx.scene.text.FontWeight;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;
import javafx.scene.shape.Rectangle;
import javafx.scene.effect.Glow;
import javafx.geometry.Bounds;


public class BattleManager {
	private Pane animationLayer = new Pane();
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
    private int enemyMaxHp;

    private ProgressBar player1HpBar = new ProgressBar(1.0);
    private ProgressBar player2HpBar = new ProgressBar(1.0);
    private ProgressBar enemyHpBar = new ProgressBar(1.0);
    
    private Label player1HpLabel = new Label();
    private Label player2HpLabel = new Label();
    private Label enemyHpLabel = new Label();
    private Label battleInfoLabel = new Label("Press Start Game, choose a character, and begin.");

    private ImageView player1Sprite;
    private ImageView player2Sprite; 
    private ImageView bossSprite; 
    
    private int[] playerMaxStamina = new int[2];

    private ProgressBar player1StaminaBar = new ProgressBar(1.0);
    private ProgressBar player2StaminaBar = new ProgressBar(1.0);

    private Label player1StaminaLabel = new Label();
    private Label player2StaminaLabel = new Label();
    
    private void updateTurnIndicator() {

        player1Sprite.setEffect(null);
        player2Sprite.setEffect(null);

        if (!gameStarted) return;

        if (playerTurn) {
            if (currentPlayerIndex == 0) {
                player1Sprite.setEffect(new Glow(0.8));
            } 
            else if (currentPlayerIndex == 1) {
                player2Sprite.setEffect(new Glow(0.8));
            }
        } 
        else {
            bossSprite.setEffect(new Glow(0.8));
        }
    }
    
    private int firstAlivePlayerIndex() {
        if (players[0] != null && players[0].isAlive()) {
            return 0;
        }
        if (players[1] != null && players[1].isAlive()) {
            return 1;
        }
        return 0; // fallback (shouldn't happen in normal play)
    }
    
    private void nextTurn() {

        // If player turn phase
        if (playerTurn) {

            // switch between player 1 and player 2
            if (currentPlayerIndex == 0 && players[1] != null && players[1].isAlive()) {
                currentPlayerIndex = 1;
            } 
            else {
                // move to enemy turn
                playerTurn = false;
            }
        }

        // Enemy turn
        if (!playerTurn) {
            enemyTurn();
            return;
        }

        updateTurnIndicator();
    }
    
    
    private void showEndGameScreen(boolean isVictory) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText(isVictory ? "Victory!" : "Defeat!");
        alert.setContentText(isVictory 
            ? "You defeated the boss! Play again?" 
            : "You have fallen. Try again?");

        ButtonType yesButton = new ButtonType("Yes");
        ButtonType noButton = new ButtonType("No");
        alert.getButtonTypes().setAll(yesButton, noButton);

        alert.show(); // ✅ NON-BLOCKING

        alert.setOnHidden(event -> {
            ButtonType result = alert.getResult();
            if (result == yesButton) {
                if (returnToMenuAction != null) {
                    returnToMenuAction.run();
                }
            } else {
                Platform.exit();
            }
        });
    }
    
    public Scene createBattleScene(Runnable pauseAction) {
        // Style the Stamina Bars (Orange)
        styleHealthBar(player1StaminaBar, "orange");
        styleHealthBar(player2StaminaBar, "orange");
        
        // Optional: Style the Player HP Bars (Green or Red) so they match the boss's bar style
        styleHealthBar(player1HpBar, "#2ecc71"); // Green
        styleHealthBar(player2HpBar, "#2ecc71"); // Green
        
    	
        // 1. The Root Layer (StackPane allows layering UI over the background)
        StackPane root = new StackPane();

        // --- LAYER 1: THE BACKGROUND ---
        Pane backgroundPane = new Pane();
        ImageView background = new ImageView(new Image("file:pictures/Game_Background.png"));
        background.setPreserveRatio(false);
        background.fitWidthProperty().bind(backgroundPane.widthProperty());
        background.fitHeightProperty().bind(backgroundPane.heightProperty());
        backgroundPane.getChildren().add(background);

        // --- LAYER 2: THE STAGE (Characters) ---
        HBox stage = new HBox(50); // Space between heroes and boss
        stage.setAlignment(Pos.BOTTOM_CENTER);
        stage.setPadding(new Insets(0, 0, 80, 0)); // Moves them up/down to sit on the "floor"

        // Set up Sprites (Keeping your existing variables)
        player1Sprite = new ImageView();
        player2Sprite = new ImageView();
        bossSprite = new ImageView();

        player1Sprite.setFitHeight(300); // Standardize height for the stage
        player1Sprite.setPreserveRatio(true);
        player2Sprite.setFitHeight(300);
        player2Sprite.setPreserveRatio(true);
        
        bossSprite.setFitHeight(450); // Make boss bigger
        bossSprite.setPreserveRatio(true);
        bossSprite.setScaleX(-1); // Flip boss to face left

        // Group players together on the left
        HBox heroes = new HBox(-80, player1Sprite, player2Sprite); 
        heroes.setAlignment(Pos.BOTTOM_CENTER);

        stage.getChildren().addAll(heroes, bossSprite);

        
        BorderPane hud = new BorderPane();
        hud.setPadding(new Insets(30, 50, 0, 50));

        // Player Side (Left)
     // Player 1 Box
        VBox player1Box = new VBox(10,
                new Label("Player 1"),
                player1HpBar,
                player1HpLabel,
                new Label("Stamina"),
                player1StaminaBar,
                player1StaminaLabel
        );
        player1Box.setAlignment(Pos.TOP_LEFT);
        player1Box.setPadding(new Insets(10));

        // Player 2 Box
        VBox player2Box = new VBox(10,
                new Label("Player 2"),
                player2HpBar,
                player2HpLabel,
                new Label("Stamina"),
                player2StaminaBar,
                player2StaminaLabel
        );
        player2Box.setAlignment(Pos.TOP_LEFT);
        player2Box.setPadding(new Insets(10));

        // Combine both
        HBox playersContainer = new HBox(30, player1Box, player2Box);
        playersContainer.setAlignment(Pos.TOP_LEFT);

        // Set to HUD
        hud.setLeft(playersContainer);        
        player1HpLabel.setTextFill(Color.WHITE);
        player1HpLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 14));
        player2HpLabel.setTextFill(Color.WHITE);
        player2HpLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 14));
        enemyHpLabel.setTextFill(Color.WHITE);
        enemyHpLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 14));
        player1StaminaLabel.setTextFill(Color.WHITE);
        player1StaminaLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 14));
        player2StaminaLabel.setTextFill(Color.WHITE);
        player2StaminaLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 14));
        
        
        
        // Boss Side (Right)
        VBox bossHUD = new VBox(10);
        bossHUD.setAlignment(Pos.TOP_RIGHT);
        styleHealthBar(enemyHpBar, "#f1c40f");
        enemyHpBar.setPrefWidth(400);
        enemyHpBar.setPrefHeight(30);
        
        Label bossTitle = new Label("GORGON");
        bossTitle.setTextFill(Color.WHITE);
        bossTitle.setFont(Font.font("Verdana", FontWeight.BOLD, 22));
        
        bossHUD.getChildren().addAll(bossTitle, enemyHpBar, enemyHpLabel);

        
        hud.setRight(bossHUD);

        // --- LAYER 4: INFO & MESSAGES (Center) ---
        VBox infoBox = new VBox(20, battleInfoLabel);
        infoBox.setAlignment(Pos.CENTER);
        battleInfoLabel.setTextFill(Color.WHITE);
        battleInfoLabel.setStyle("-fx-background-color: rgba(0,0,0,0.5); -fx-padding: 10;");

     // 1. Make sure clicks pass through the invisible animation layer
        animationLayer.setMouseTransparent(true); 
        
        // 2. Clear anything old and add the layers in order (Back to Front)
        root.getChildren().clear();
        root.getChildren().addAll(backgroundPane, stage, hud, infoBox, animationLayer); 

        // 3. Create the scene object
        Scene battleScene = new Scene(root, 1000, 650);

        // 4. Add the Key Listeners to this new scene
        battleScene.setOnKeyPressed(event -> {
            if (!gameStarted || gamePaused || players[0] == null || enemy == null) {
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

        // 5. Return the finished scene
        return battleScene;
    }
    
    private void animateBossAttack(ImageView sourceSprite, ImageView targetSprite, Runnable onImpact) {
        // 1. Create the Beam (A glowing cyan rectangle)
        Rectangle beam = new Rectangle(60, 10, Color.CYAN);
        beam.setEffect(new Glow(0.8));

        // 2. Find the exact X/Y coordinates of the sprites on the screen
        Bounds sourceBounds = sourceSprite.localToScene(sourceSprite.getBoundsInLocal());
        Bounds targetBounds = targetSprite.localToScene(targetSprite.getBoundsInLocal());

        
        double startX = sourceBounds.getMinX();
        double startY = sourceBounds.getMinY() + (sourceBounds.getHeight() / 2);

        
        double endX = targetBounds.getMaxX();
        double endY = targetBounds.getMinY() + (targetBounds.getHeight() / 2);

        beam.setLayoutX(startX);
        beam.setLayoutY(startY);

        
        animationLayer.getChildren().add(beam);

        
        TranslateTransition transition = new TranslateTransition(Duration.seconds(0.4), beam);
        transition.setByX(endX - startX);
        transition.setByY(endY - startY);

       
        transition.setOnFinished(e -> {
            animationLayer.getChildren().remove(beam); 
            onImpact.run(); 
            playHitEffect(targetSprite);
        });

        transition.play();
        
     // Inside your animateBossAttack callback:
        if (!anyPlayerAlive()) {
            battleInfoLabel.setText("Both heroes have fallen. Game over.");
            gameStarted = false;

            // Wrap the popup in runLater to avoid the crash
            Platform.runLater(() -> {
                showEndGameScreen(false);
            });
            return;
        }
        
    }
    
    private void animatePlayerAttack(ImageView sourceSprite, ImageView targetSprite, Runnable onImpact) {
        // 1. Create a Golden Beam for the Hero
        Rectangle beam = new Rectangle(50, 8, Color.GOLD);
        beam.setEffect(new Glow(0.8));

        // 2. Get Positions
        Bounds sourceBounds = sourceSprite.localToScene(sourceSprite.getBoundsInLocal());
        Bounds targetBounds = targetSprite.localToScene(targetSprite.getBoundsInLocal());

        // Start at Player (Right side of sprite)
        double startX = sourceBounds.getMaxX();
        double startY = sourceBounds.getMinY() + (sourceBounds.getHeight() / 2);

        // End at Boss (Left side of sprite)
        double endX = targetBounds.getMinX();
        double endY = targetBounds.getMinY() + (targetBounds.getHeight() / 2);

        beam.setLayoutX(startX);
        beam.setLayoutY(startY);
        animationLayer.getChildren().add(beam);

        // 3. Move the beam
        TranslateTransition tt = new TranslateTransition(Duration.seconds(0.3), beam);
        tt.setByX(endX - startX);
        tt.setByY(endY - startY);

        tt.setOnFinished(e -> {
            animationLayer.getChildren().remove(beam);
            onImpact.run(); // Damage happens now!
            playHitEffect(targetSprite);
        });
        tt.play();
     // Inside your animatePlayerAttack callback:
        if (!enemy.isAlive()) {
            battleInfoLabel.setText("Victory! The " + enemy.getName() + " has been defeated!");
            gameStarted = false;
            
            // Wrap the popup in runLater to avoid the crash
            Platform.runLater(() -> {
                showEndGameScreen(true);
            });
        } else {
            // ... rest of your turn switching logic ...
        }
        
    }
    
    private void playHitEffect(ImageView target) {
        // Flash effect
        target.setOpacity(0.4);

        javafx.animation.PauseTransition flash = new javafx.animation.PauseTransition(Duration.millis(100));
        flash.setOnFinished(e -> target.setOpacity(1.0));
        flash.play();

        // Shake effect
        TranslateTransition shake = new TranslateTransition(Duration.millis(100), target);
        shake.setByX(10);
        shake.setCycleCount(4);
        shake.setAutoReverse(true);
        shake.play();
    }
    
    private void styleHealthBar(ProgressBar bar, String color) {
        bar.setStyle(
            "-fx-accent: " + color + "; " +               // Progress color
            "-fx-control-inner-background: #222222; " +    // Background color (Dark Gray)
            "-fx-background-color: white, black; " +       // Double border effect
            "-fx-background-insets: 0, 1; " + 
            "-fx-padding: 2px;"
        );
    }
    

    public void startBattle(BaseCharacter selectedCharacter1, BaseCharacter selectedCharacter2) {
        this.players[0] = cloneCharacter(selectedCharacter1);
        this.players[1] = cloneCharacter(selectedCharacter2);
        this.playerMaxStamina[0] = players[0].getStamina();
        this.playerMaxStamina[1] = players[1].getStamina();
        this.enemy = new boss("Gorgon");

        this.playerMaxHp[0] = players[0].getHp();
        this.playerMaxHp[1] = players[1].getHp();
        this.enemyMaxHp = enemy.getHp();

        this.gameStarted = true;
        this.gamePaused = false;
        this.playerTurn = true;
        this.currentPlayerIndex = this.players[0].isAlive() ? 0 : 1; // Start with player 1 if alive, otherwise player 2
        updateTurnIndicator();

        battleInfoLabel.setText(players[0].getName() +" and " + players[1].getName() + " enters the battle against " + enemy.getName() + ".\n" + players[currentPlayerIndex].getName() + "'s turn. Press A to attack.");
        
        player1Sprite.setImage(getSpriteForCharacter(players[0]));
        player2Sprite.setImage(getSpriteForCharacter(players[1]));
        bossSprite.setImage(new Image("file:pictures/Boss.png")); //file name spelling error on purpose because image isn't where it's supposed to be when you run it
        
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
    	BaseCharacter attacker = players[currentPlayerIndex];
    	if (attacker == null || !attacker.isAlive()) {
    	    nextTurnAfterPlayerAction();
    	    return;
    	}

    	// Check stamina
    	if (attacker.getStamina() < 20) { }

    	// Subtract stamina
    	attacker.setStamina(attacker.getStamina() - 20)

;
    	updateBattleUi(); // ✅ RIGHT HERE

    	// Continue with attack
    	ImageView attackerSprite = (currentPlayerIndex == 0) ? player1Sprite : player2Sprite;
        battleInfoLabel.setText(attacker.getName() + " launches an attack!");

        // 2. Trigger the Animation
        animatePlayerAttack(attackerSprite, bossSprite, () -> {
            // --- ALL DAMAGE LOGIC GOES INSIDE THIS BLOCK ---
            int damage = attacker.calculateAttack();
            enemy.takeDamage(damage);
            
            battleInfoLabel.setText(attacker.getName() + " hit " + enemy.getName() + " for " + damage + " damage!");
            updateBattleUi();

            // 3. Check if boss died or move to next turn
            if (!enemy.isAlive()) {
                battleInfoLabel.setText("Victory! The " + enemy.getName() + " has been defeated!");
                gameStarted = false;
                showEndGameScreen(true);
            } else {
                // Logic to switch to player 2 or enemy turn
                if (currentPlayerIndex == 0 && players[1] != null && players[1].isAlive()) {
                	nextTurn();
                    battleInfoLabel.setText(battleInfoLabel.getText() + "\n" + players[1].getName() + "'s turn!");
                } else {
                    playerTurn = false;
                    // Add a small delay before boss counter-attacks so it feels natural
                    new Thread(() -> {
                        try { Thread.sleep(1000); } catch (InterruptedException e) {}
                        Platform.runLater(() -> enemyTurn());
                    }).start();
                }
            }
        });
    }

    private void playerHeal() {
        if (!playerTurn) {
            return;
        }

        BaseCharacter currentPlayer = players[currentPlayerIndex]; 
        int healAmount = 18;

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

        // Figure out which sprite the boss is aiming at
        ImageView targetSprite = (target == players[0]) ? player1Sprite : player2Sprite;

        // Change text to show the boss is attacking
        battleInfoLabel.setText(enemy.getName() + " is firing a beam at " + target.getName() + "!");

        // FIRE THE BEAM! 
        animateBossAttack(bossSprite, targetSprite, () -> {
            
            // --- EVERYTHING IN HERE HAPPENS AFTER THE BEAM HITS ---
            
            int damage = enemy.calculateAttack();
            int before = target.getHp();
            target.takeDamage(damage);
            int dealt = before - target.getHp();

            if (dealt <= 0) {
                battleInfoLabel.setText(battleInfoLabel.getText() + "\nIt deals no damage!");
            } else {
                battleInfoLabel.setText(battleInfoLabel.getText() + "\nDirect hit for " + dealt + " damage!");
            }

            updateBattleUi();

            if (!anyPlayerAlive()) {
                battleInfoLabel.setText(battleInfoLabel.getText() + "\nBoth heroes have fallen. Game over.");
                gameStarted = false;
                showEndGameScreen(false);
                return;
            }

            // Pass the turn back to the player
            playerTurn = true;
            currentPlayerIndex = firstAlivePlayerIndex();
            updateTurnIndicator();
            battleInfoLabel.setText(
                    battleInfoLabel.getText() + "\n" +
                    players[currentPlayerIndex].getName() + "'s turn."
            ); 
        });
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
            player1HpBar.setProgress((double) players[0].getHp() / playerMaxHp[0]);
            player1HpLabel.setText(players[0].getName() + " HP: " + players[0].getHp());

            player1StaminaBar.setProgress((double) players[0].getStamina() / playerMaxStamina[0]);
            player1StaminaLabel.setText("Stamina: " + players[0].getStamina());
        }

        if (players[1] != null) {
            player2HpBar.setProgress((double) players[1].getHp() / playerMaxHp[1]);
            player2HpLabel.setText(players[1].getName() + " HP: " + players[1].getHp());

            player2StaminaBar.setProgress((double) players[1].getStamina() / playerMaxStamina[1]);
            player2StaminaLabel.setText("Stamina: " + players[1].getStamina());
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