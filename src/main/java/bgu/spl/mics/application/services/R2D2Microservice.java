package bgu.spl.mics.application.services;

import bgu.spl.mics.Callback;
import bgu.spl.mics.Event;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.AttackEvent;
import bgu.spl.mics.application.messages.BombDestroyerEvent;
import bgu.spl.mics.application.messages.DeactivationEvent;
import bgu.spl.mics.application.messages.landoFinishedBC;
import bgu.spl.mics.application.passiveObjects.Diary;


/**
 * R2D2Microservices is in charge of the handling {@link DeactivationEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link DeactivationEvent}.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class R2D2Microservice extends MicroService {

    private long duration;

    public R2D2Microservice(long duration) {
        super("R2D2");
        this.duration = duration;
    }

    @Override
    protected void initialize() {

        //when lando finish his attack - terminate
        Callback terminateCallback = (Object i) -> {
            Diary.getInstance().setR2D2Terminate(System.currentTimeMillis());
            terminate();
        };
        subscribeBroadcast(landoFinishedBC.class,terminateCallback);

        //deactivating the shield
        Callback<DeactivationEvent> callback = (deactivationEvent) -> {
            try {
                Thread.sleep(duration);
            } catch (InterruptedException interruptedException) {}

            Diary.getInstance().setR2D2Deactivate(System.currentTimeMillis());
            complete(deactivationEvent,true);
        };
        subscribeEvent(DeactivationEvent.class,callback);
    }
}
