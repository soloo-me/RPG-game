package Griffith;

import java.util.Random;

public class Tank extends BaseCharacter {

    private Random rand = new Random();
    private int stamina;
    private final int MAX_STAMINA = 120;
    private final int ATTACK_STAMINA_COST = 25; // Attacks are heavy and expensive

    public Tank(String name) {
        // name, hp, stamina, agility, intellect, attack
        super(name, 250, 120, 40, 20, 15);
        this.stamina = MAX_STAMINA;
    }

    /**
     * Checks stamina before allowing the heavy tank attack.
     */
    public int performAttackAction() {
        if (stamina >= ATTACK_STAMINA_COST) {
            stamina -= ATTACK_STAMINA_COST;
            System.out.println(name + " swings heavily! (" + ATTACK_STAMINA_COST + " stamina used)");
            return calculateAttack();
        } else {
            System.out.println(name + " is too tired to lift their weapon!");
            return 0;
        }
    }

    @Override
    public int calculateAttack() {
        // damage = 15–20
        return attack + rand.nextInt(6); 
    }

    @Override
    public void takeDamage(int incomingDamage) {
        // Tank ability: ignore high damage (>25)
        // This is a passive ability, so it shouldn't cost stamina 
        // unless you want to make it even more challenging!
        if (incomingDamage > 25) {
            System.out.println(name + " absorbed the heavy hit! No damage taken.");
            return;
        }

        super.takeDamage(incomingDamage);
    }
    
    // Tank might need a slower regeneration than the Brawler
    public void regenerateStamina() {
        int regenerationAmount = 15;
        this.stamina = Math.min(this.stamina + regenerationAmount, MAX_STAMINA);
        System.out.println(name + " recovers some breath. Current Stamina: " + this.stamina);
    }

    public int getStamina() {
        return stamina;
    }
}