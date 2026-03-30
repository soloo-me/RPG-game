package Griffith;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class BattleManager {

    private BaseCharacter player;
    private BaseCharacter enemy;

    private boolean gameStarted = false;
    private boolean gamePaused = false;
    private boolean playerTurn = true;

    private int playerMaxHp;
    private int enemyMaxHp;

    private ProgressBar playerHpBar = new ProgressBar(1.0);
    private ProgressBar enemyHpBar = new ProgressBar(1.0);
    private Label playerHpLabel = new Label();
    private Label enemyHpLabel = new Label();
    private Label battleInfoLabel = new Label("Press Start Game, choose a character, and begin.");

    public Scene createBattleScene(Runnable pauseAction) {
        Label title = new Label("Battle");
        title.setFont(Font.font(28));

        VBox playerBox = new VBox(10,
                new Label("Player"),
                playerHpBar,
                playerHpLabel
        );
        playerBox.setAlignment(Pos.CENTER);
        playerBox.setPadding(new Insets(15));
        playerBox.setStyle("-fx-border-color: green; -fx-border-width: 2;");

        VBox enemyBox = new VBox(10,
                new Label("Enemy Boss"),
                enemyHpBar,
                enemyHpLabel
        );
        enemyBox.setAlignment(Pos.CENTER);
        enemyBox.setPadding(new Insets(15));
        enemyBox.setStyle("-fx-border-color: darkred; -fx-border-width: 2;");

        HBox topBars = new HBox(40, playerBox, enemyBox);
        topBars.setAlignment(Pos.CENTER);

        Label controls = new Label("Battle controls: A = Attack | H = Heal | P or ESC = Pause");
        controls.setTextFill(Color.DARKBLUE);

        battleInfoLabel.setWrapText(true);
        battleInfoLabel.setPrefWidth(500);
        battleInfoLabel.setFont(Font.font(16));

        VBox centerBox = new VBox(18, title, topBars, controls, battleInfoLabel);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setPadding(new Insets(25));

        BorderPane root = new BorderPane();
        root.setCenter(centerBox);

        Scene scene = new Scene(root, 1000, 650);

        scene.setOnKeyPressed(event -> {
            if (!gameStarted || gamePaused || player == null || enemy == null) {
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

    public void startBattle(BaseCharacter selectedCharacter) {
        this.player = cloneCharacter(selectedCharacter);
        this.enemy = new boss("Gorgon");

        this.playerMaxHp = player.getHp();
        this.enemyMaxHp = enemy.getHp();

        this.gameStarted = true;
        this.gamePaused = false;
        this.playerTurn = true;

        battleInfoLabel.setText(player.getName() + " enters the battle against " + enemy.getName() + ". Press A to attack.");
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

        int damage = player.calculateAttack();
        int before = enemy.getHp();
        enemy.takeDamage(damage);
        int dealt = before - enemy.getHp();

        battleInfoLabel.setText(player.getName() + " attacks for " + dealt + " damage.");
        updateBattleUi();

        if (!enemy.isAlive()) {
            battleInfoLabel.setText(player.getName() + " defeated the boss. You win!");
            gameStarted = false;
            return;
        }

        playerTurn = false;
        enemyTurn();
    }

    private void playerHeal() {
        if (!playerTurn) {
            return;
        }

        int healAmount = 18;
        int newHp = Math.min(playerMaxHp, player.getHp() + healAmount);
        int actualHeal = newHp - player.getHp();
        player.setHp(newHp);

        battleInfoLabel.setText(player.getName() + " heals for " + actualHeal + " HP.");
        updateBattleUi();

        playerTurn = false;
        enemyTurn();
    }

    private void enemyTurn() {
        if (!enemy.isAlive() || !player.isAlive()) {
            return;
        }

        int damage = enemy.calculateAttack();
        int before = player.getHp();
        player.takeDamage(damage);
        int dealt = before - player.getHp();

        if (dealt <= 0) {
            battleInfoLabel.setText(battleInfoLabel.getText() + "\n" + enemy.getName() + " attacks, but it deals no damage.");
        } else {
            battleInfoLabel.setText(battleInfoLabel.getText() + "\n" + enemy.getName() + " hits back for " + dealt + " damage.");
        }

        updateBattleUi();

        if (!player.isAlive()) {
            battleInfoLabel.setText(battleInfoLabel.getText() + "\n" + player.getName() + " has fallen. Game over.");
            gameStarted = false;
            return;
        }

        playerTurn = true;
    }

    private void updateBattleUi() {
        if (player != null) {
            playerHpBar.setProgress((double) player.getHp() / playerMaxHp);
            playerHpLabel.setText(player.getName() + " HP: " + player.getHp() + " / " + playerMaxHp);
        }

        if (enemy != null) {
            enemyHpBar.setProgress((double) enemy.getHp() / enemyMaxHp);
            enemyHpLabel.setText(enemy.getName() + " HP: " + enemy.getHp() + " / " + enemyMaxHp);
        }
    }

    private BaseCharacter cloneCharacter(BaseCharacter character) {
        if (character instanceof Archer) return new Archer(character.getName());
        if (character instanceof Brawler) return new Brawler(character.getName());
        if (character instanceof Mage) return new Mage(character.getName());
        if (character instanceof Tank) return new Tank(character.getName());
        throw new IllegalArgumentException("Unknown character type");
    }
}