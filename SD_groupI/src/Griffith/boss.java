package Griffith;

public class boss extends BaseCharacter {

    public boss(String name) {
        super(name, 400, 100, 10, 20, 30);
    }

    @Override
    public void takeDamage(int incomingDamage) {
        double reductionMultiplier = 0.85;
        int finalDamage = (int) Math.round(incomingDamage * reductionMultiplier);

        System.out.println(name + "'s thick hide reduced " + incomingDamage + " to " + finalDamage + "!");
        super.takeDamage(finalDamage);
    }

    @Override
    public int calculateAttack() {
        java.util.Random rand = new java.util.Random();
        return rand.nextInt(12) + 19;
    }
}