package Griffith;

public abstract class BaseCharacter {
    protected String name;
    protected int hp, stamina, agility, intellect, attack;

    public BaseCharacter(String name, int hp, int stamina, int agility, int intellect, int attack) {
        this.name = name;
        this.hp = hp;
        this.stamina = stamina;
        this.agility = agility;
        this.intellect = intellect;
        this.attack = attack;
    }

    public void takeDamage(int incomingDamage) {
        this.hp = Math.max(0, this.hp - incomingDamage);
    }

    public boolean isAlive() {
        return this.hp > 0;
    }

    public String getName() {
        return name;
    }

    public int getHp() {
        return hp;
    }

    public int getStamina() {
        return stamina;
    }

    public int getAgility() {
        return agility;
    }

    public int getIntellect() {
        return intellect;
    }

    public int getAttack() {
        return attack;
    }

    public int calculateAttack() {
        return 0;
    }

    public void setHp(int hp) {
        this.hp = Math.max(0, hp);
    }
}