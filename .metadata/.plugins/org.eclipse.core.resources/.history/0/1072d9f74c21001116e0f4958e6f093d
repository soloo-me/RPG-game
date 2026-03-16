package Griffith;

public abstract class BaseCharacter {
    protected String name;
    protected int hp, stamina, agility, intellect;

    public BaseCharacter(String name, int hp, int stamina, int agility, int intellect) {
        this.name = name;
        this.hp = hp;
        this.stamina = stamina;
        this.agility = agility;
        this.intellect = intellect;
    }
    // The core damage logic that subclasses will @Override
    public void takeDamage(int incomingDamage) {
        this.hp = Math.max(0, this.hp - incomingDamage);
    }

    public boolean isAlive() { return this.hp > 0; }
    public String getName() { return name; }
    public int getHp() { return hp; }
}