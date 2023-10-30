package bgu.spl.mics.application.services;
import bgu.spl.mics.Callback;
import bgu.spl.mics.Future;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.application.messages.AttackEvent;
import bgu.spl.mics.application.messages.BombDestroyerEvent;
import bgu.spl.mics.application.messages.DeactivationEvent;
import bgu.spl.mics.application.passiveObjects.Attack;
import bgu.spl.mics.application.messages.landoFinishedBC;

import java.util.ArrayList;
import java.util.List;


import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.passiveObjects.Diary;

/**
 * LeiaMicroservices Initialized with Attack objects, and sends them as  {@link AttackEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link AttackEvent}.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class LeiaMicroservice extends MicroService {
	private Attack[] attacks;
	
    public LeiaMicroservice(Attack[] attacks) {
        super("Leia");
		this.attacks = attacks;
    }

    @Override
    protected void initialize() {

        //making sure that leia send the attack events only after all microServices are registered and sub
        try{
            Thread.sleep(2000);
        }catch (InterruptedException interruptedException){};

        //send AttackEvents and follow all futures
        AttackEvent[] attackEvents= transferToEvent();
        Future[] futures = new Future[attacks.length];
        for (int i = 0; i < attackEvents.length; i++) {
            Future future = sendEvent(attackEvents[i]);
            futures[i] = future;
        }

        //when lando finish his attack - terminate
        Callback terminateCallback = (Object i) -> {
            Diary.getInstance().setLeiaTerminate(System.currentTimeMillis());
            terminate();
        };
        subscribeBroadcast(landoFinishedBC.class, terminateCallback);

        //follow attack progression and update R2D2
        for (int j = 0; j < futures.length; j++){
            futures[j].get();
        }
        DeactivationEvent deactivationEvent = new DeactivationEvent();
        Future deactivationFuture = sendEvent(deactivationEvent);
        deactivationFuture.get(); // wait till R2D2 finish

        //update lando to attack
        BombDestroyerEvent bombDestroyerEvent = new BombDestroyerEvent();
        sendEvent(bombDestroyerEvent);


    }

    public AttackEvent[] transferToEvent(){
        AttackEvent[] attackEvents = new AttackEvent[attacks.length];
        for (int i = 0; i < attacks.length; i++) {
            attackEvents[i]= new AttackEvent(attacks[i]);
        }
        return attackEvents;
    }
}
