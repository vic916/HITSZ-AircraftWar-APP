package edu.hitsz.prop;

import edu.hitsz.aircraft.AbstractAircraft;


public class BloodProp extends AbstractProp {
    private static final int HEAL_AMOUNT = 20;

    public BloodProp(int locationX, int locationY, double speedX, double speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void effect(AbstractAircraft aircraft) {
        System.out.println("BloodSupply active!");
        if (aircraft == null) {
            return;
        }
        int missingHp = aircraft.getMaxHp() - aircraft.getHp();
        if (missingHp > 0) {
            aircraft.decreaseHp(-Math.min(HEAL_AMOUNT, missingHp));
        }
    }
}
