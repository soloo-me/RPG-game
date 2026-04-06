package Griffith;


import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test; 
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

public class BossGimmickTest {
    private boss gorgon;
    private Archer archer;
    private Brawler brawler;
    private Mage mage;
    private Tank tank;

    @BeforeEach
    void setUp() {
        gorgon = new boss("Gorgon");
        archer = new Archer("Legolas");
        brawler = new Brawler("Rocky");
        mage = new Mage("Gandalf");
        tank = new Tank("Rein");
    }

    @Test
    @DisplayName("Death Test: Boss HP should cap at 0")
    void testBossDeath() {
        // Boss has 400 HP. 1000 damage reduced by 15% is 850.
        // The logic should ensure HP doesn't drop to -450.
        gorgon.takeDamage(1000);
        
        assertEquals(0, gorgon.getHp(), "Boss HP must be exactly 0 after a fatal blow.");
    }

    @Test
    @DisplayName("Gimmick Test: All 4 Classes vs Boss")
    void testPartyAttacks() {
        BaseCharacter[] party = {archer, brawler, mage, tank};

        for (BaseCharacter hero : party) {
            int hpBefore = gorgon.getHp();
            int attackValue = hero.calculateAttack();
            
            gorgon.takeDamage(attackValue);
            
            // Expected: round(attack * 0.85)
            int expectedDamage = (int) Math.round(attackValue * 0.85);
            int actualDamageTaken = hpBefore - gorgon.getHp();
            
            assertEquals(expectedDamage, actualDamageTaken, 
                "Reduction failed for " + hero.getClass().getSimpleName());
        }
    }
    //Archer Tests
    @Test
    @DisplayName("Archer attack uses 20 stamina")
    void testArcherAttackConsumesStamina() {
        int staminaBefore = archer.getStamina();

        archer.performAttackAction();

        //verifies that stamina decreases by exactly 20
        assertEquals(staminaBefore - 20, archer.getStamina(),
                "Archer should lose 20 stamina when attacking.");
    }

    @Test
    @DisplayName("Archer cannot attack without enough stamina")
    void testArcherCannotAttackWithoutStamina() {
    	//keeps attacking until the archer runs out of stamina
    	//loop checks if there is enough stamina for another attack
        while (archer.hasEnoughStamina(20)) {
            archer.performAttackAction();
        }

        //after stamina is depleted, attacking returns 0 damage
        assertEquals(0, archer.performAttackAction(),
                "Archer should deal 0 damage when out of stamina.");
    }

    @Test
    @DisplayName("Archer evades damage of 27 or less")
    void testArcherEvadesLowDamage() {
        int hpBefore = archer.getHp();

        archer.takeDamage(27);
        
        //verify that hp hasn't changed(hence the attack has been evaded)
        assertEquals(hpBefore, archer.getHp(),
                "Archer should evade attacks of 27 or less.");
    }

    @Test
    @DisplayName("Archer takes damage above 27")
    void testArcherTakesHighDamage() {
        int hpBefore = archer.getHp();

        archer.takeDamage(28);

        //verifies that Hp has decreased by the full damage amount when damage is above 28
        assertEquals(hpBefore - 28, archer.getHp(),
                "Archer should take damage above 27.");
    }
}
