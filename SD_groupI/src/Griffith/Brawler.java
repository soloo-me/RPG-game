package Griffith;
import java.util.Random;

public class Brawler extends BaseCharacter {
	private Random rand = new Random();
	public Brawler(String name) {
		super(name, 220, 200, 38, 30, 17);
	}
	@Override
	public int calculateAttack() {
		//damage of 17-25
		int damage = rand.nextInt(9) + 17;
		
		//15% chance of 2x damage
		if (rand.nextDouble() < 0.15) {
			damage *= 2;
			System.out.println(name + " lands a critical hit!");
		}
		return damage;
	}
	
	//high stamina regeneration every round
	public void regenerateStamina() {
		int regenerationAmount = 25;
		stamina += regenerationAmount;
		
		//cap so stamina doesn't grow forever
		if (stamina > 200) {
			stamina = 200;
		}
		System.out.println(name + " regenerates " + regenerationAmount + " stamina.");
	}
	

}
