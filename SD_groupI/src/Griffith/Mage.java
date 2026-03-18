package Griffith;

public class Mage extends BaseCharacter {
    private int mana;
    private static final int DAMAGE_THRESHOLD = 25;

    public Mage(String name) {
        super(name, 125 + (int)(Math.random() * 26), 50, 80, 100, 50);
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
    	int manaCost = 20;
    	
    	if (mana < manaCost) {
    		System.out.println(name + " does not have enough mana!");
    		return;
    	}
    	mana -= manaCost;
    	
    	System.out.println(name + " casts " + spell + " with " + intellect + " intelligence!");
    	int spellDamage = attack + (intellect /2);
    	System.out.println("Spell deals " + spellDamage + " damage!");
    	
    }

    public int getMana() {
    	return mana;
    }
    public int getIntelligence() {
        return intellect;
    }
}