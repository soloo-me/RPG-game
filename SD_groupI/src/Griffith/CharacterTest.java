package Griffith;


import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test; 
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

public class CharacterTest {
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

            //verifies that HP has decreased by the full damage amount when damage is above 28
            assertEquals(hpBefore - 28, archer.getHp(),
                    "Archer should take damage above 27.");
        }
        
     // Tank tests
        @Test
        @DisplayName("Tank Test: Heavy attack reduces stamina by 25")
        void testTankAttackConsumesStamina() {
            int staminaBefore = tank.getStamina();
            
            tank.performAttackAction();
            
            int staminaAfter = tank.getStamina();
            assertEquals(staminaBefore - 25, staminaAfter, "Tank should lose 25 stamina per attack.");
        }

        @Test
        @DisplayName("Tank Test: Cannot attack with insufficient stamina")
        void testTankCannotAttackWithoutStamina() {
            // Tank starts with 120. 4 attacks = 100 stamina used. 20 remaining.
            // 5th attack requires 25, so it should fail.
            for (int i = 0; i < 4; i++) {
                tank.performAttackAction();
            }
            
            int damage = tank.performAttackAction();
            
            assertEquals(0, damage, "Tank should deal 0 damage when stamina is below 25.");
        }

        @Test
        @DisplayName("Tank Test: Absorbs (ignores) damage greater than 25")
        void testTankAbsorbsHighDamage() {
            int hpBefore = tank.getHp();
            
            // Tank logic: if incomingDamage > 25, take 0 damage
            tank.takeDamage(30); 
            
            assertEquals(hpBefore, tank.getHp(), "Tank should ignore hits greater than 25.");
        }

        @Test
        @DisplayName("Tank Test: Takes normal damage if 25 or less")
        void testTankTakesLowDamage() {
            int hpBefore = tank.getHp();
            int damageAmount = 20;
            
            tank.takeDamage(damageAmount);
            
            assertEquals(hpBefore - damageAmount, tank.getHp(), "Tank should take full damage if hit is 25 or less.");
        }

        @Test
        @DisplayName("Tank Test: Stamina regeneration increases current stamina")
        void testTankRegenerateStamina() {
            // Reduce stamina first
            tank.performAttackAction(); // 120 -> 95
            int staminaAfterAttack = tank.getStamina();
            
            tank.regenerateStamina(); // 95 + 15 = 110
            
            assertEquals(staminaAfterAttack + 15, tank.getStamina(), "Tank should regenerate 15 stamina.");
        }

        @Test
        @DisplayName("Tank Test: Stamina regeneration caps at 120")
        void testTankStaminaCap() {
            // Tank is already at 120 (Max)
            tank.regenerateStamina();
            
            assertEquals(120, tank.getStamina(), "Tank stamina should not exceed MAX_STAMINA (120).");
        }
      
    }
