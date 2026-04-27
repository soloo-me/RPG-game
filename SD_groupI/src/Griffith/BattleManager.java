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
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.io.File;


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
    private String difficulty = "HARD";
    
    private double p1DirX = 1, p1DirY = 0;
    private double p2DirX = 1, p2DirY = 0;
    private MediaPlayer battleMusic;
 
    
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
    
    private void tryPlayerAttack(int requestingPlayerIndex) {
        // Check if it's the player phase and if the correct player pressed their attack key
        if (playerTurn && currentPlayerIndex == requestingPlayerIndex) {
            playerAttack(); // Call your existing attack logic
        } else {
            // Optional: Let them know it isn't their turn
            battleInfoLabel.setText("It is not " + players[requestingPlayerIndex].getName() + "'s turn to attack!");
        }
    }
    
    private boolean isInRange(ImageView a, ImageView b) {
        Bounds ba = a.getBoundsInParent();
        Bounds bb = b.getBoundsInParent();

        double dx = (ba.getMinX() + ba.getWidth() / 2) - (bb.getMinX() + bb.getWidth() / 2);
        double dy = (ba.getMinY() + ba.getHeight() / 2) - (bb.getMinY() + bb.getHeight() / 2);

        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance < 2000;	
    }
    
    private void moveBossTowardTarget(ImageView boss, ImageView target) {
        double dx = target.getTranslateX() - boss.getTranslateX();
        double dy = target.getTranslateY() - boss.getTranslateY();

        TranslateTransition move = new TranslateTransition(Duration.seconds(0.4), boss);
        move.setByX(dx * 0.3);
        move.setByY(dy * 0.3);

        move.setOnFinished(e -> clampToBattleBounds(boss, boss.getScene()));

        move.play();
    }
    
    private void telegraphAttack(ImageView targetSprite, Runnable onHit) {

    	Bounds bounds = targetSprite.localToScene(targetSprite.getBoundsInLocal());

        double x = bounds.getMinX() + bounds.getWidth() / 2;
        double y = bounds.getMinY() + bounds.getHeight() / 2;

        
        double localX = animationLayer.sceneToLocal(x, y).getX();
        double localY = animationLayer.sceneToLocal(x, y).getY();
        
        //  Warning circle
        javafx.scene.shape.Circle warning = new javafx.scene.shape.Circle(80, Color.RED);
        warning.setOpacity(0.3);
        warning.setLayoutX(localX);
        warning.setLayoutY(localY);

        animationLayer.getChildren().add(warning);

        //  Delay before hit
        PauseTransition delay = new PauseTransition(Duration.seconds(2));
        delay.setOnFinished(e -> {
            animationLayer.getChildren().remove(warning);

            if (warning.getBoundsInParent().intersects(targetSprite.getBoundsInParent())) {
                // HIT SUCCESSFUL
                onHit.run(); 
                playExplosionEffect(targetSprite);
                playHitEffect(targetSprite);
            } else {
                // DODGED
                battleInfoLabel.setText("The attack missed! " + targetSprite.getId() + " dodged!");
                // Switch turns here if you don't use the timer method above
                endEnemyTurn(); 
            }
        });
        
        
        
        delay.play();
    }
    
    private void endEnemyTurn() {
        Platform.runLater(() -> {
            playerTurn = true;
            currentPlayerIndex = firstAlivePlayerIndex();
            updateTurnIndicator();
             battleInfoLabel.setText(
					battleInfoLabel.getText() + "\n" +
					players[currentPlayerIndex].getName() + "'s turn."
				);
        });
    }
    
    private void clampToBattleBounds(ImageView sprite, Scene scene) {
        Bounds bounds = sprite.localToScene(sprite.getBoundsInLocal());

        double dx = 0;
        double dy = 0;

        double minX = 0;
        double maxX = scene.getWidth();
        double minY = 0;
        double maxY = scene.getHeight();

        if (bounds.getMinX() < minX) {
            dx = minX - bounds.getMinX();
        } else if (bounds.getMaxX() > maxX) {
            dx = maxX - bounds.getMaxX();
        }

        if (bounds.getMinY() < minY) {
            dy = minY - bounds.getMinY();
        } else if (bounds.getMaxY() > maxY) {
            dy = maxY - bounds.getMaxY();
        }

        sprite.setTranslateX(sprite.getTranslateX() + dx);
        sprite.setTranslateY(sprite.getTranslateY() + dy);
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

        player1Sprite.setFitHeight(200); // Standardize height for the stage
        player1Sprite.setPreserveRatio(true);
        player2Sprite.setFitHeight(200);
        player2Sprite.setPreserveRatio(true);
        
        bossSprite.setFitHeight(450); // Make boss bigger
        bossSprite.setPreserveRatio(true);
        bossSprite.setScaleX(-1); // Flip boss to face left

        
        VBox heroes = new VBox(20, player1Sprite, player2Sprite); 
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
        player1HpBar.setPrefWidth(70);      
        player1HpBar.setPrefHeight(20);      
        player1StaminaBar.setPrefWidth(70); 
        player1StaminaBar.setPrefHeight(20);

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
        player2HpBar.setPrefWidth(70);      
        player2HpBar.setPrefHeight(20);      
        player2StaminaBar.setPrefWidth(70); 
        player2StaminaBar.setPrefHeight(20);


        // Combine both
        HBox playersContainer = new HBox(30, player1Box, player2Box);
        playersContainer.setAlignment(Pos.TOP_LEFT);

        // Set to HUD
        hud.setLeft(playersContainer);        
        player1HpLabel.setTextFill(Color.WHITE);
        player1HpLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 10));
        player2HpLabel.setTextFill(Color.WHITE);
        player2HpLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 10));
        enemyHpLabel.setTextFill(Color.WHITE);
        enemyHpLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 14));
        player1StaminaLabel.setTextFill(Color.WHITE);
        player1StaminaLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 10));
        player2StaminaLabel.setTextFill(Color.WHITE);
        player2StaminaLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 10));
        
        
        
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
            double speed = 15.0; // Speed of movement for the 

            switch (event.getCode()) {
                // --- Player 1 Controls 
            case W -> {
                player1Sprite.setTranslateY(player1Sprite.getTranslateY() - speed);
                p1DirX = 0; p1DirY = -1;
                clampToBattleBounds(player1Sprite, battleScene);
            }
            case S -> {
                player1Sprite.setTranslateY(player1Sprite.getTranslateY() + speed);
                p1DirX = 0; p1DirY = 1;
                clampToBattleBounds(player1Sprite, battleScene);
            }
            case A -> {
                player1Sprite.setTranslateX(player1Sprite.getTranslateX() - speed);
                p1DirX = -1; p1DirY = 0;
                clampToBattleBounds(player1Sprite, battleScene);
            }
            case D -> {
                player1Sprite.setTranslateX(player1Sprite.getTranslateX() + speed);
                p1DirX = 1; p1DirY = 0;
                clampToBattleBounds(player1Sprite, battleScene);
            }
                case E -> tryPlayerAttack(0); 

                // --- Player 2 Controls 
                case UP -> {
                    player2Sprite.setTranslateY(player2Sprite.getTranslateY() - speed);
                    p2DirX = 0; p2DirY = -1;
                    clampToBattleBounds(player2Sprite, battleScene);
                }
                case DOWN -> {
                    player2Sprite.setTranslateY(player2Sprite.getTranslateY() + speed);
                    p2DirX = 0; p2DirY = 1;
                    clampToBattleBounds(player2Sprite, battleScene);
                }
                case LEFT -> {
                    player2Sprite.setTranslateX(player2Sprite.getTranslateX() - speed);
                    p2DirX = -1; p2DirY = 0;
                    clampToBattleBounds(player2Sprite, battleScene);
                }
                case RIGHT -> {
                    player2Sprite.setTranslateX(player2Sprite.getTranslateX() + speed);
                    p2DirX = 1; p2DirY = 0;
                    clampToBattleBounds(player2Sprite, battleScene);
                }
                case SHIFT -> tryPlayerAttack(1); // Pass index 1 for Player 2
                case H -> playerHeal();
                case P, ESCAPE -> pauseAction.run();
                default -> { }
            }
        });

        // 5. Return the finished scene
        return battleScene;
    }
    private void startBattleMusic() {
        try {
            File musicFile = new File("sounds/BattleMusic.mp3");

            if (!musicFile.exists()) {
                System.out.println("Music file not found: " + musicFile.getAbsolutePath());
                return;
            }

            Media music = new Media(musicFile.toURI().toString());

            battleMusic = new MediaPlayer(music);
            battleMusic.setCycleCount(MediaPlayer.INDEFINITE); // loop forever
            battleMusic.setVolume(0.5); // 50% volume
            battleMusic.play();

        } catch (Exception e) {
            System.out.println("Failed to load battle music.");
            e.printStackTrace();
        }
    }
    private void stopBattleMusic() {
        if (battleMusic != null) {
            battleMusic.stop();
        }
    }
    
    private void animateBossAttack(ImageView sourceSprite, ImageView targetSprite, Runnable onImpact) {

        Bounds sourceBounds = sourceSprite.localToScene(sourceSprite.getBoundsInLocal());
        Bounds targetBounds = targetSprite.localToScene(targetSprite.getBoundsInLocal());

        double startX = sourceBounds.getMinX();
        double startY = sourceBounds.getMinY() + (sourceBounds.getHeight() / 2);

        double endX = targetBounds.getMaxX();
        double endY = targetBounds.getMinY() + (targetBounds.getHeight() / 2);

        int particleCount = 3; // 🔥 number of fire particles

        for (int i = 0; i < particleCount; i++) {

        	ImageView fire = new ImageView(new Image("file:pictures/fire.png"));
        	fire.setFitWidth(80);
        	fire.setFitHeight(80);

            fire.setLayoutX(startX);
            fire.setLayoutY(startY);

            animationLayer.getChildren().add(fire);

            TranslateTransition tt = new TranslateTransition(
                    Duration.seconds(0.4 + Math.random() * 0.2), fire);

            tt.setByX((endX - startX) + (Math.random() - 0.5) * 50);
            tt.setByY((endY - startY) + (Math.random() - 0.5) * 50);

            FadeTransition fade = new FadeTransition(Duration.seconds(0.4), fire);
            fade.setToValue(0);

            ParallelTransition anim = new ParallelTransition(tt, fade);

            anim.setOnFinished(e -> animationLayer.getChildren().remove(fire));
            anim.play();
        }

        // 🔥 VERY IMPORTANT: delay impact until animation finishes
        PauseTransition delay = new PauseTransition(Duration.seconds(0.45));
        delay.setOnFinished(e -> {
            onImpact.run();              // damage happens here
            playHitEffect(targetSprite); // shake + flash
            playExplosionEffect(targetSprite); // 💥 explosion effect
        });

        delay.play();
    }
    
    private void animatePlayerAttack(ImageView attacker, ImageView target, Runnable onImpact) {

            
            onImpact.run();
            playHitEffect(target);
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
        startBattleMusic();

        battleInfoLabel.setText(
        	    players[0].getName() + " and " + players[1].getName() +
        	    " enter the battle against " + enemy.getName() + ".\n" +
        	    players[currentPlayerIndex].getName() + "'s turn.\n" +
        	    "Controls: W=Up, S=Down, A=Back, D=Forward, E=Attack"
        	);
        
        player1Sprite.setImage(getSpriteForCharacter(players[0]));
        player2Sprite.setImage(getSpriteForCharacter(players[1]));
        bossSprite.setImage(new Image("file:pictures/Boss.png")); //file name spelling error on purpose because image isn't where it's supposed to be when you run it
        
     // 🔥 HARD MODE VISUAL EFFECT
        if (difficulty.equals("HARD")) {
            Glow redGlow = new Glow(0.9);
            bossSprite.setEffect(redGlow);
        }
        
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
    	if (attacker.getStamina() < 20) {
    	    battleInfoLabel.setText(attacker.getName() + " is too tired!");
    	    return;
    	}

    	// Subtract stamina
    	int staminaCost = difficulty.equals("HARD") ? 30 : 20;
    	attacker.setStamina(attacker.getStamina() - staminaCost);
    	updateBattleUi(); // ✅ RIGHT HERE

    	// Continue with attack
    	ImageView attackerSprite = (currentPlayerIndex == 0) ? player1Sprite : player2Sprite;
    	if (!isInRange(attackerSprite, bossSprite)) {
    	    battleInfoLabel.setText(attacker.getName() + " is too far away!");
    	    return;
    	}
    	
        battleInfoLabel.setText(attacker.getName() + " launches an attack!");

        // 2. Trigger the Animation
        playSlashProjectile(attackerSprite, bossSprite);

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
                stopBattleMusic();
                showEndGameScreen(true);
            } else {
                // Logic to switch to player 2 or enemy turn
            	if (currentPlayerIndex == 0 && players[1] != null && players[1].isAlive()) {
            	    nextTurnAfterPlayerAction();
            	
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
        int healAmount;

        if (difficulty.equals("EASY")) {
            healAmount = 25;
        } else if (difficulty.equals("HARD")) {
            healAmount = 10;
        } else {
            healAmount = 18;
        }

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
            stopBattleMusic();
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
            stopBattleMusic();
            showEndGameScreen(false);
            return; 
        }

        // Figure out which sprite the boss is aiming at
        ImageView targetSprite = (target == players[0]) ? player1Sprite : player2Sprite;
        moveBossTowardTarget(bossSprite, targetSprite);
        
        // Change text to show the boss is attacking
        battleInfoLabel.setText(enemy.getName() + " is firing a beam at " + target.getName() + "!");

        // FIRE THE BEAM! 
        telegraphAttack(targetSprite, () -> {

            // AFTER warning → fire attack happens
            animateBossAttack(bossSprite, targetSprite, () -> {

                int damage = enemy.calculateAttack();

                if (difficulty.equals("EASY")) {
                    damage = Math.max(5, damage - 5);
                } else if (difficulty.equals("HARD")) {
                    damage += 15;
                }

                if (difficulty.equals("HARD") && Math.random() < 0.3) {
                    damage *= 2;
                    battleInfoLabel.setText(enemy.getName() + " lands a CRITICAL HIT!");
                }

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

                // back to player turn
                playerTurn = true;
                currentPlayerIndex = firstAlivePlayerIndex();
                updateTurnIndicator();

                battleInfoLabel.setText(
                    battleInfoLabel.getText() + "\n" +
                    players[currentPlayerIndex].getName() + "'s turn."
                );
            });
        });
    }
    
    
    private BaseCharacter chooseBossTarget() {
    	boolean p1Alive = players[0] != null && players[0].isAlive();
    	boolean p2Alive = players[1] != null && players[1].isAlive();

    	if (!p1Alive && !p2Alive) return null;
    	if (!p1Alive) return players[1];
    	if (!p2Alive) return players[0];

    	if (difficulty.equals("HARD")) {
    	    return (players[0].getHp() < players[1].getHp()) ? players[0] : players[1];
    	}

    	// EASY / MEDIUM = random
    	return Math.random() < 0.5 ? players[0] : players[1];
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
    
    public void setDifficulty(String difficulty) {
    	this.difficulty = difficulty;
    }
    
    private void playExplosionEffect(ImageView target) {

        Bounds bounds = target.localToScene(target.getBoundsInLocal());

        double centerX = bounds.getMinX() + bounds.getWidth() / 2;
        double centerY = bounds.getMinY() + bounds.getHeight() / 2;

        for (int i = 0; i < 15; i++) {

            // 🔥 Circle instead of rectangle (key difference)
            javafx.scene.shape.Circle fire = new javafx.scene.shape.Circle(
                5 + Math.random() * 6, // random size
                Color.color(1, Math.random() * 0.5, 0) // red → orange → yellow
            );

            fire.setLayoutX(centerX);
            fire.setLayoutY(centerY);
            fire.setEffect(new Glow(1.0));
            fire.setOpacity(0.9);

            animationLayer.getChildren().add(fire);

            // 🔥 movement (spread + rise like fire)
            TranslateTransition move = new TranslateTransition(Duration.millis(400), fire);
            move.setByX((Math.random() - 0.5) * 100); // spread sideways
            move.setByY(-50 - Math.random() * 50);    // move UP (important for fire look)

            // 🔥 grow
            javafx.animation.ScaleTransition scale =
                    new javafx.animation.ScaleTransition(Duration.millis(400), fire);
            scale.setToX(2.5);
            scale.setToY(2.5);

            // 🔥 fade out
            javafx.animation.FadeTransition fade =
                    new javafx.animation.FadeTransition(Duration.millis(400), fire);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);

            javafx.animation.ParallelTransition anim =
                    new javafx.animation.ParallelTransition(move, scale, fade);

            anim.setOnFinished(e -> animationLayer.getChildren().remove(fire));
            anim.play();
        }
    }
    
    private void playSlashProjectile(ImageView source, ImageView target) {

        Bounds start = source.getBoundsInParent();
        Bounds end = target.getBoundsInParent();

        double startX = start.getMinX() + start.getWidth() / 2;
        double startY = start.getMinY() + start.getHeight() / 2;

        double endX = end.getMinX() + end.getWidth() / 2;
        double endY = end.getMinY() + end.getHeight() / 2;

        Rectangle slash = new Rectangle(80, 6, Color.WHITE);
        slash.setRotate(25);

        slash.setLayoutX(startX);
        slash.setLayoutY(startY);

        animationLayer.getChildren().add(slash);

        TranslateTransition move = new TranslateTransition(Duration.millis(300), slash);

        move.setByX(endX - startX);
        move.setByY(endY - startY);

        FadeTransition fade = new FadeTransition(Duration.millis(300), slash);
        fade.setToValue(0);

        ParallelTransition anim = new ParallelTransition(move, fade);

        anim.setOnFinished(e -> {
            animationLayer.getChildren().remove(slash);
            playHitEffect(target);
        });

        anim.play();
    }
}