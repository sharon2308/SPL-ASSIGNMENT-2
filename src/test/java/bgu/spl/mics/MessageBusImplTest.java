package bgu.spl.mics;

import bgu.spl.mics.application.messages.AttackEvent;
import bgu.spl.mics.application.messages.landoFinishedBC;
import bgu.spl.mics.application.passiveObjects.Attack;
import bgu.spl.mics.application.services.C3POMicroservice;
import bgu.spl.mics.application.services.HanSoloMicroservice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MessageBusImplTest {

    private MessageBusImpl messageBusTest;
    private MicroService microServiceTest1;
    private MicroService microServiceTest2;
    private AttackEvent eventTest;
    private Broadcast broadcastTest;

    @BeforeEach
    void setUp() {
        messageBusTest = MessageBusImpl.getInstance();
        microServiceTest1 = new HanSoloMicroservice();
        microServiceTest2 = new C3POMicroservice();

        // this lines were modified because we changed the constructor of AttackEvent
        List<Integer> serial = new LinkedList<>();
        serial.add(1);
        eventTest = new AttackEvent(new Attack(serial,1000));


        broadcastTest = new landoFinishedBC();
    }

    @Test
    //checking register , subscribeEvent , sendEvent , awaitMessage
    void subscribeEvent() {
        messageBusTest.register(microServiceTest2);
        messageBusTest.subscribeEvent(eventTest.getClass(), microServiceTest2);
        messageBusTest.sendEvent(eventTest);
        try {
            assertEquals((messageBusTest.awaitMessage(microServiceTest2)), eventTest);
        } catch (InterruptedException i) {
            fail();
        }
        messageBusTest.unregister(microServiceTest2); // this line was added to reset the msgBus before the next test

    }

    @Test
        //checking subscribeBroadcast , sendBroadcast
    void subscribeBroadcast() {
        messageBusTest.register(microServiceTest1);
        messageBusTest.register(microServiceTest2);
        messageBusTest.subscribeBroadcast(broadcastTest.getClass(), microServiceTest2);
        messageBusTest.subscribeBroadcast(broadcastTest.getClass(), microServiceTest1);
        messageBusTest.sendBroadcast(broadcastTest);
        try {
            assertEquals(messageBusTest.awaitMessage(microServiceTest1), broadcastTest);
        } catch (InterruptedException i) {
            fail();
        }
        try {
            assertEquals(messageBusTest.awaitMessage(microServiceTest2), broadcastTest);
        } catch (InterruptedException i) {
            fail();
        }
        messageBusTest.unregister(microServiceTest1); // this lines were added to reset the msgBus before the next test
        messageBusTest.unregister(microServiceTest2);
    }

    @Test
    void complete() {
        messageBusTest.register(microServiceTest1);
        messageBusTest.subscribeEvent(eventTest.getClass(), microServiceTest1);
        Future<Boolean> ans = messageBusTest.sendEvent(eventTest);
        assertFalse(ans.isDone());
        messageBusTest.complete(eventTest, true);
        assertTrue(ans.isDone());
        assertTrue(ans.get());
        messageBusTest.unregister(microServiceTest1); // this line was added to reset the msgBus before the next test

    }
}