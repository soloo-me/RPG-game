package Griffith;
import java.util.Random;

public class Archer extends BaseCharacter{
	
	private Random rand = new Random();
	
	public Archer(String name) {
		super(name, 180, 200, 70, 40, 15);
	}
	@Override
	public int calculateAttack() {
		
		//damage = 15-30
		int damage = attack + rand.nextInt(16);
		
		//30% chance of a double damage attack (critical hit)
		if (rand.nextDouble() < 0.3) {
			damage *= 2;
			System.out.println(name + " lands a critical hit!");
		}
		return damage;
	}
	@Override
	public void takeDamage(int incomingDamage) {
		//Can evade attack if damage is below 27
		if (incomingDamage <= 27) {
			System.out.println(name + " evaded the attack!");
			return; //takes 0 damage
		}
		//otherwise he takes normal damage
		super.takeDamage(incomingDamage);
	}
	
}
