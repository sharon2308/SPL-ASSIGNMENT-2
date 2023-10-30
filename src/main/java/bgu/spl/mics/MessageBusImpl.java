package bgu.spl.mics;

import bgu.spl.mics.application.passiveObjects.Attack;
import bgu.spl.mics.application.passiveObjects.Diary;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {

	private static class MessageBusImplHolder{
		private static MessageBusImpl instance = new MessageBusImpl();
	}

	private static MessageBusImpl instance = null;
	private ConcurrentHashMap<MicroService, LinkedBlockingQueue<Message>> queueOfMS; // holds the queue of each microService
	private ConcurrentHashMap<Class<? extends Event>, LinkedBlockingQueue<MicroService>> subToEvent; // holds for each event, which microService is subed to it
	private ConcurrentHashMap<Class<? extends Broadcast>,LinkedBlockingQueue<MicroService>> subToBroadcast; // holds for each broadcast, which microService is subed to it
	private ConcurrentHashMap<Event,Future> futureOfEvent; // holds for each event, its future
	private Object lock1;
	private Object lock2;
	private Object lock3;
	private Object lock4;


	private MessageBusImpl(){
		queueOfMS = new ConcurrentHashMap<MicroService, LinkedBlockingQueue<Message>>();
		subToEvent = new ConcurrentHashMap<Class<? extends Event>, LinkedBlockingQueue<MicroService>>();
		subToBroadcast = new ConcurrentHashMap<Class<? extends Broadcast>,LinkedBlockingQueue<MicroService>>();
		futureOfEvent = new ConcurrentHashMap<Event,Future>();
		lock1 = new Object();
		lock2 = new Object();
		lock3 = new Object();
		lock4 = new Object();
	}

	public static MessageBusImpl getInstance(){
		return MessageBusImplHolder.instance;
	}

	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		synchronized (lock3) {
			if (subToEvent.containsKey(type)) { // if some microService is already subed to this event, just add m to the event list
				subToEvent.get(type).add(m);
			} else { // no one is subed to this event, create new event list and add m
				LinkedBlockingQueue<MicroService> toAdd = new LinkedBlockingQueue<>();
				toAdd.add(m);
				subToEvent.put(type, toAdd);
			}
			m.AddToMyEvents(type);
		}
	}

	@Override
	public synchronized void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		synchronized (lock4) {
			if (subToBroadcast.containsKey(type)) { // if some microService is already subed to this broadcast, just add m to the broadcast list
				subToBroadcast.get(type).add(m);
			} else { // no one is subed to this broadcast, create new broadcast list and add m
				LinkedBlockingQueue<MicroService> toAdd = new LinkedBlockingQueue<>();
				toAdd.add(m);
				subToBroadcast.put(type, toAdd);
			}
			m.AddToMyBroadCast(type);
		}
	}

	@Override @SuppressWarnings("unchecked")
	public <T> void complete(Event<T> e, T result) {
		Future relevantFutrure = futureOfEvent.get(e);
		relevantFutrure.resolve(result); // update the matching future of e
		futureOfEvent.remove(relevantFutrure); // future is no longer needed
	}

	@Override
	public void sendBroadcast(Broadcast b) {
		synchronized (lock2) {
			LinkedBlockingQueue<MicroService> tempList = subToBroadcast.get(b.getClass());
			if (tempList == null) { // no one subed to this broadcast
				return;
			}
			Iterator<MicroService> it = tempList.iterator();
			while (it.hasNext()) {
				MicroService m = it.next();
				LinkedBlockingQueue q = queueOfMS.get(m);
				if (q == null) { // in the case which m didn't register yet
					return;
				}
				q.add(b);
			}
		}
	}

	private <T> boolean addToQueue(Event<T> e){ // implementation of round robin
		synchronized (lock1){
			LinkedBlockingQueue<MicroService> tempList =  subToEvent.get(e.getClass());
			if (tempList==null || tempList.size() == 0 ){ // no one subed to this event
				return false;
			}
			MicroService m;
			if (tempList.size() == 1) { // only 1 MS handles this type of Event
				m = tempList.peek();
			} else { // more than 1 MS handles this type of event - give the event to the "first in line" and put him at the "end of the line"
				m = tempList.poll();
				try {
					tempList.put(m);
				}catch (InterruptedException i){};
			}
			LinkedBlockingQueue q = queueOfMS.get(m);
			if (q == null){ // microService didn't register yet
				return false;
			}
			q.add(e);
		}
		return true;
	}

	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		Future future = new Future();
		futureOfEvent.put(e, future);
		if (addToQueue(e)) { // e was added to the chosen subed microService Queue in a round robin manner
			return future;
		}
		return null;
	}


	@Override
	public void register(MicroService m) {
		LinkedBlockingQueue<Message> q = new LinkedBlockingQueue<>();
		queueOfMS.put(m,q);
	}

	@Override
	public void unregister(MicroService m) {
		queueOfMS.remove(m);
		removeFromAll(m);
	}

	private void removeFromAll(MicroService m){ // deletes all m references
		for ( Class c: m.getMyEvents()) {
			subToEvent.get(c).remove(m);
		}
		m.getMyEvents().clear();
		for ( Class c: m.getMyBroadcasts()) {
			subToBroadcast.get(c).remove(m);
		}
		m.getMyBroadcasts().clear();
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		LinkedBlockingQueue<Message> q = queueOfMS.get(m);
		Message take = q.take(); // wait until q is not empty
		return take;
	}
}