package observer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class Observable<T> {
	
	//Constructors---------------------------------------------------
	
	public Observable(){
		this.observers = new HashSet<Observer<T>>();
	}
	
	//Properties-----------------------------------------------------
	
	private Set<Observer<T>> observers;
	
	public Set<Observer<T>> getObservers(){
		Set<Observer<T>> result;
		
		result = Collections.unmodifiableSet(observers);
		
		return result;
	}
	
	public void addObserver(Observer<T> observer){
		assert observer != null;
		
		observers.add(observer);
	}
	
	public void removeObserver(Observer<T> observer){
		assert observer != null;
		assert contains(observer);
		
		observers.remove(observer);
	}
	
	//Interface methods----------------------------------------------
	
	public boolean contains(Observer<T> observer){
		assert observer != null;
		
		boolean result;
		
		result = observers.contains(observer);
		
		return result;
	}
	
	public abstract void updateObservers(T info);
}
