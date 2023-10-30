package bgu.spl.mics.application;

import bgu.spl.mics.Callback;
import bgu.spl.mics.Future;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.passiveObjects.Attack;
import bgu.spl.mics.application.passiveObjects.Diary;
import bgu.spl.mics.application.passiveObjects.Ewok;
import bgu.spl.mics.application.passiveObjects.Ewoks;
import bgu.spl.mics.application.services.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;


/** This is the Main class of the application. You should parse the input file,
 * create the different components of the application, and run the system.
 * In the end, you should output a JSON.
 */
public class Main {

    public static void main(String[] args) {

 

        //json input
        operationData operationData = new operationData();
        String path = args[0];

        Gson gson = new Gson();
        try{
            Reader reader = Files.newBufferedReader(Paths.get(String.valueOf(path)));
            operationData = gson.fromJson(reader,operationData.class);
        }catch (IOException e){
            e.printStackTrace();
        }

        //Initialization and run
        MessageBusImpl.getInstance();
        Ewoks.getInstance();
        Ewoks.setList(operationData.getEwoks());
        initializeAll(operationData.getR2D2(), operationData.getLando(), operationData.getAttack());


        //json output
        try(FileWriter writer= new FileWriter(args[1])){
                gson.toJson(Diary.getInstance(),writer);
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    public static void initializeAll(long durationR2D2, long durationLando, Attack[] attacks){

        MicroService leia = new LeiaMicroservice(attacks);
        MicroService HanSolo = new HanSoloMicroservice();
        MicroService C3P0 = new C3POMicroservice();
        MicroService R2D2 = new R2D2Microservice(durationR2D2);
        MicroService Lando = new LandoMicroservice(durationLando);

        Thread leiaT = new Thread(leia);
        Thread HanT = new Thread(HanSolo);
        Thread C3POT = new Thread(C3P0);
        Thread R2D2T = new Thread(R2D2);
        Thread landoT = new Thread(Lando);

        leiaT.start();
        HanT.start();
        C3POT.start();
        R2D2T.start();
        landoT.start();

        try {
            leiaT.join();
            HanT.join();
            C3POT.join();
            R2D2T.join();
            landoT.join();
        }catch (InterruptedException interruptedException){};
    }
}


