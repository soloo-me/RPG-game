package Griffith;

import java.util.Random;

public class Tank extends BaseCharacter {

    private Random rand = new Random();

    public Tank(String name) {
        // name, hp, stamina, agility, intellect, attack
        super(name, 250, 120, 40, 20, 15);
    }

    @Override
    public int calculateAttack() {
        // damage = 15–20
        return attack + rand.nextInt(6); 
    }

    @Override
    public void takeDamage(int incomingDamage) {
        // Tank ability: ignore high damage (>25)
        if (incomingDamage > 25) {
            System.out.println(name + " absorbed the heavy hit! No damage taken.");
            return; // takes 0 damage
        }

        // otherwise take normal damage
        super.takeDamage(incomingDamage);
    }
}