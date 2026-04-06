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
    
        //Mage Tests
        @Test
        @DisplayName("Mage Test: Damage below threshold should be applied")
        void testMageTakesLowDamage() {
            int hpBefore = mage.getHp();
            
            mage.takeDamage(20); // below threshold (25)

            int hpAfter = mage.getHp();

            assertTrue(hpAfter < hpBefore, "Mage should take damage when below threshold.");
        }

        @Test
        @DisplayName("Mage Test: Damage above threshold should be ignored")
        void testMageIgnoresHighDamage() {
            int hpBefore = mage.getHp();
            
            mage.takeDamage(40); // above threshold

            int hpAfter = mage.getHp();

            assertEquals(hpBefore, hpAfter, "Mage should ignore damage above threshold.");
        }

        @Test
        @DisplayName("Mage Test: Casting spell reduces mana")
        void testMageManaReduction() {
            int manaBefore = mage.getMana();

            mage.castSpell("Fireball");

            int manaAfter = mage.getMana();

            assertEquals(manaBefore - 20, manaAfter, "Casting a spell should reduce mana by 20.");
        }

        @Test
        @DisplayName("Mage Test: Cannot cast spell without enough mana")
        void testMageNotEnoughMana() {
            // Drain mana
            for (int i = 0; i < 6; i++) {
                mage.castSpell("Spam Spell");
            }

            int manaBefore = mage.getMana();

            mage.castSpell("Final Spell"); // should fail

            int manaAfter = mage.getMana();

            assertEquals(manaBefore, manaAfter, "Mana should not change if spell cannot be cast.");
        }
    }
