package Griffith;

public class Mage extends BaseCharacter {
    private int mana;
    private int intelligence;
    private static final int DAMAGE_THRESHOLD = 25;

    public Mage(String name) {
        super(name);
        // Random HP between 125-150
        this.health = 125 + (int)(Math.random() * 26);
        this.attackPower = 40;
        this.intelligence = 75;
        this.mana = 100;
    }

    @Override
    public void takeDamage(int damage) {
        // If damage is higher than 25, ignore it completely
        if (damage > DAMAGE_THRESHOLD) {
            System.out.println(name + " ignores the " + damage + " damage!");
            // No damage taken
        } else {
            // Take normal damage if 25 or less
            super.takeDamage(damage);
            System.out.println(name + " takes " + damage + " damage!");
        }
    }

    public void castSpell(String spell) {
        System.out.println(name + " casts " + spell + " with " + intelligence + " intelligence!");
        int spellDamage = attackPower + (intelligence / 2);
        System.out.println("Spell deals " + spellDamage + " damage!");
    }

    public int getIntelligence() {
        return intelligence;
    }
}