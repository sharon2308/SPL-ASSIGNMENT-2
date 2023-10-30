package bgu.spl.mics.application.passiveObjects;


import java.util.LinkedList;
import java.util.List;

/**
 * Passive object representing the resource manager.
 * <p>
 * This class must be implemented as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add ONLY private methods and fields to this class.
 */
public class Ewoks {

    private static class EwoksHolder{
        private static Ewoks instance = new Ewoks();
    }

    private static LinkedList<Ewok> ewoks;

    private Ewoks(){
        ewoks = new LinkedList<>();
    }

    public static Ewoks getInstance(){
        return EwoksHolder.instance;
    }

    public static void setList(int numOfEwoks){
        for (int i = 0; i < numOfEwoks; i++) {
            Ewok ewok = new Ewok(i+1);
            ewoks.addLast(ewok);
        }
    }

    public void getResources(List<Integer> requiredEwoks){
        for (int i = 0; i < requiredEwoks.size(); i++) {
            ewoks.get(requiredEwoks.get(i) - 1).acquire();
        }
    }

    public void releaseAll(List<Integer> requiredEwoks){
        for (int i = 0; i < requiredEwoks.size(); i++) {
            ewoks.get(requiredEwoks.get(i) - 1).release();
        }
    }

}
