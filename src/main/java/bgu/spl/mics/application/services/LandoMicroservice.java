package bgu.spl.mics.application.services;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.Callback;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.AttackEvent;
import bgu.spl.mics.application.messages.BombDestroyerEvent;
import bgu.spl.mics.application.messages.landoFinishedBC;
import bgu.spl.mics.application.passiveObjects.Attack;
import bgu.spl.mics.application.passiveObjects.Diary;


/**
 * LandoMicroservice
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class LandoMicroservice  extends MicroService {

    private long duration;

    public LandoMicroservice(long duration) {
        super("Lando");
        this.duration=duration;

    }

    @Override
    protected void initialize() {

        //when lando finish his attack - terminate
        Callback terminateCallback = (e) ->{
            Diary.getInstance().setLandoTerminate(System.currentTimeMillis());
            terminate();
        };
        subscribeBroadcast(landoFinishedBC.class,terminateCallback);

        //bombing the star - sleep and send broadcast to terminate
        Callback callback = (Object i) -> {
            try {
                 Thread.sleep(duration);
            } catch (InterruptedException interruptedException) {}
            Broadcast broadcast = new landoFinishedBC();
            sendBroadcast(broadcast);
        };
        subscribeEvent(BombDestroyerEvent.class,callback);


    }
}
