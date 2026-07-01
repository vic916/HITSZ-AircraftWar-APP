package edu.hitsz.prop;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.basic.Observer;

import java.util.ArrayList;
import java.util.List;

public class BombProp extends AbstractProp {
    private final List<Observer> observerLists = new ArrayList<>();

    public BombProp(int locationX, int locationY, double speedX, double speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    public void addObserver(Observer observer) {
        if (observer != null) {
            observerLists.add(observer);
        }
    }

    public void notifyAllFlyings() {
        List<Observer> observers = new ArrayList<>(observerLists);
        observerLists.clear();
        for (Observer observer : observers) {
            observer.update();
        }
    }

    @Override
    public void effect(AbstractAircraft aircraft) {
        System.out.println("BombSupply active!");
        notifyAllFlyings();
    }
}
