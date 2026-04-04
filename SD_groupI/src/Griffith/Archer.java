package Griffith;
import java.util.Random;

public class Archer extends BaseCharacter {
    
    private Random rand = new Random();
    private int stamina; // Added stamina field
    private final int ATTACK_STAMINA_COST = 20; // Cost per attack

    public Archer(String name) {
        // Assuming BaseCharacter handles name, hp, maxHp, attack, defense, etc.
        super(name, 180, 200, 70, 40, 15);
        this.stamina = 100; // Initialize starting stamina
    }

    /**
     * Checks if the character has enough stamina and performs the attack.
     * returns the damage dealt, or 0 if stamina is insufficient.
     */
    public int performAttackAction() {
        if (hasEnoughStamina(ATTACK_STAMINA_COST)) {
            consumeStamina(ATTACK_STAMINA_COST);
            return calculateAttack();
        } else {
            System.out.println(name + " is too tired to attack!");
            return 0;
        }
    }

    // Check if stamina is sufficient
    public boolean hasEnoughStamina(int cost) {
        return this.stamina >= cost;
    }

    // Deduct stamina
    public void consumeStamina(int amount) {
        this.stamina -= amount;
        System.out.println(name + " used " + amount + " stamina. Remaining: " + this.stamina);
    }

    @Override
    public int calculateAttack() {
        // Base damage logic: 15-30
        int damage = attack + rand.nextInt(16);
        
        // 30% chance of a double damage attack (critical hit)
        if (rand.nextDouble() < 0.3) {
            damage *= 2;
            System.out.println(name + " lands a critical hit!");
        }
        return damage;
    }

    @Override
    public void takeDamage(int incomingDamage) {
        // Can evade attack if damage is below 27
        if (incomingDamage <= 27) {
            System.out.println(name + " evaded the attack!");
            return; 
        }
        super.takeDamage(incomingDamage);
    }

    // Getter for stamina (optional)
    public int getStamina() {
        return stamina;
    }
}
