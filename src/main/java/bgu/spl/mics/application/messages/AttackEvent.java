package bgu.spl.mics.application.messages;
import bgu.spl.mics.Event;
import bgu.spl.mics.application.passiveObjects.Attack;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AttackEvent implements Event<Boolean> {

    private List<Integer> serialEwoks;
    private int duration;


    public AttackEvent(Attack attack){
        serialEwoks = attack.getSerials();
        duration = attack.getDuration();
    }

    public List<Integer> getSerialEwoks() {
        return serialEwoks;
    }

    public int getDuration() {
        return duration;
    }
}
