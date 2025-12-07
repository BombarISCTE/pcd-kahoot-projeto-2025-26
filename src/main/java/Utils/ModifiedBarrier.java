package Utils;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ModifiedBarrier {

    private final Lock lock = new ReentrantLock();
    private final Condition todosChegaram = lock.newCondition();

    private final int totalJogadores;
    private final Runnable barrierAction;
    private int jogadoresQueChegaram = 0;
    private boolean tempoExpirado = false;

    public ModifiedBarrier(int totalJogadores, Runnable barrierAction) {
        this.totalJogadores = totalJogadores;
        this.barrierAction = barrierAction;
    }

    public void chegouJogador(){
        lock.lock();
        try{
            jogadoresQueChegaram++;
            if(jogadoresQueChegaram >= totalJogadores){
                barrierAction.run();
                todosChegaram.signalAll();
            }
        }finally {
            lock.unlock();
        }
    }

    public void tempoExpirado(){
        lock.lock();
        try{
            tempoExpirado = true;
            barrierAction.run();
            todosChegaram.signalAll();
        }finally {
            lock.unlock();
        }
    }

    public void await() throws InterruptedException {
        lock.lock();
        try{
            while(jogadoresQueChegaram < totalJogadores && !tempoExpirado){
                todosChegaram.await();
            }
        } finally {
            lock.unlock();
        }
    }

}
