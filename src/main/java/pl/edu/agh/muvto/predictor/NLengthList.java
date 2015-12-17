package pl.edu.agh.muvto.predictor;

import java.util.ArrayList;
import java.util.Iterator;

public class NLengthList<T> implements Iterable<T>{
    private ArrayList<T> list;
    private int N;
    private int lastInsertedIndex;
    
    public NLengthList(int n){
        list = new ArrayList<>();
        lastInsertedIndex = 0;
        this.N = n;
    }
    
    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }
    
    public void add(T elem){
        if(list.size() == N){
            list.set(lastInsertedIndex, elem);
            lastInsertedIndex++;
            
            resetIndexReachingN();
            
        } else {
            list.add(elem);
        }
    }
    
    public T get(int index){
        return list.get(index);
    }
    
    public int size(){
        return this.list.size();
    }
    
    private void resetIndexReachingN(){
        if(lastInsertedIndex == N)
          lastInsertedIndex = 0;
    }
}
