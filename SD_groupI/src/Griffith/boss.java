package Griffith;

public class boss extends BaseCharacter {
	private static String gorgon;

	public boss(String name) {
        // Stats: name, hp (400), stamina (100), agility (10), intellect (20)
        super(gorgon, 400, 100, 10, 20 , 30);
    }

    /**
     * Boss Gimmick: Damage Reduction
     * Reduces all incoming damage by 15%
     */
    @Override
    public void takeDamage(int incomingDamage) {
        // Calculate 15% reduction (multiplying by 0.85)
        double reductionMultiplier = 0.85;
        int finalDamage = (int) Math.round(incomingDamage * reductionMultiplier);
        
        System.out.println(name + "'s thick hide reduced " + incomingDamage + " to " + finalDamage + "!");
        
        // Pass the final calculated damage to the base class HP logic
        super.takeDamage(finalDamage);
    }

    /**
     * Boss Attack: 19-30 Damage
     */
    @Override
    public int calculateAttack() {
        // Generates a random number between 19 and 30
        java.util.Random rand = new java.util.Random();
        return rand.nextInt(12) + 19; // (0 to 11) + 19 = 19 to 30
    }
}

