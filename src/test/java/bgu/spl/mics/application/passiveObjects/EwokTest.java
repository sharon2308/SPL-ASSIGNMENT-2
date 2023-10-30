package bgu.spl.mics.application.passiveObjects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EwokTest {

    private Ewok ewok;

    @BeforeEach
    void setUp() {
        ewok = new Ewok(1);
    }

    @Test
    public void testAcquire(){
        assertTrue(ewok.available);
        ewok.acquire();
        assertFalse(ewok.available);
    }
    @Test
    public void testRelease(){
        assertTrue(ewok.available);
        ewok.acquire();
        assertFalse(ewok.available);
        ewok.release();
        assertTrue(ewok.available);
    }
}