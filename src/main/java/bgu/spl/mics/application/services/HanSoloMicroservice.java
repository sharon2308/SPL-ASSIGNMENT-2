package bgu.spl.mics.application.services;


import bgu.spl.mics.*;
import bgu.spl.mics.application.messages.AttackEvent;
import bgu.spl.mics.application.messages.landoFinishedBC;
import bgu.spl.mics.application.passiveObjects.Attack;
import bgu.spl.mics.application.passiveObjects.Diary;
import bgu.spl.mics.application.passiveObjects.Ewoks;

import java.util.Collections;
import java.util.List;


/**
 * HanSoloMicroservices is in charge of the handling {@link AttackEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link AttackEvent}.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class HanSoloMicroservice extends MicroService {

    public HanSoloMicroservice() {
        super("Han");
    }


    @Override
    protected void initialize() {

        //when lando finish his attack - terminate
        Callback<landoFinishedBC> terminateCallback = (landoFinishedBC l) -> {
            terminate();
            Diary.getInstance().setHanSoloTerminate(System.currentTimeMillis());
        };
        subscribeBroadcast(landoFinishedBC.class,terminateCallback);

        // callback for attackEvent - get resources,sleep, update diary and complete
        Callback<AttackEvent> callbackAttack = (AttackEvent event) ->{
            List<Integer> orderedEwoks = event.getSerialEwoks();
            Collections.sort(orderedEwoks);
            Ewoks.getInstance().getResources(orderedEwoks);
            try {
                Thread.sleep(event.getDuration());
            }catch (InterruptedException i){};
            Ewoks.getInstance().releaseAll(orderedEwoks);
            Diary.getInstance().increaseTotalAttacks();
            Diary.getInstance().setHanSoloFinish(System.currentTimeMillis());
            complete(event,true);
        };
        subscribeEvent(AttackEvent.class,callbackAttack);
    }






}
