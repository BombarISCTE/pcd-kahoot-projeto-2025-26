package Utils;

public class ModifiedCountdownLatch {
    private int count;
    private final int bonusFactor;
    private final int bonusCount;
    private final int waitPeriod;
    private int bonusUsados = 0;

    public ModifiedCountdownLatch(int bonusFactor , int bonusCount , int waitPeriod , int count ) {
        this.bonusFactor = bonusFactor;
        this.bonusCount = bonusCount;
        this.waitPeriod = waitPeriod;
        if (count < 0) throw new IllegalArgumentException("count < 0");
        this.count = count;
    }

    // As threads chamam isto para esperar
    public synchronized void await() throws InterruptedException {
        while (count > 0) {
            wait();
        }
    }

    // O temporizador chama isto para reduzir o contador
    public synchronized int countDown() {
       int fator;
       if(bonusUsados < bonusCount){
           fator = bonusFactor;
           bonusUsados++;
       } else {
           fator = 1;
       }
       count--;
       if(count <= 0){
           notify();
       }
       return fator;
    }

    public synchronized void reset(int count) { //caso queira reutilizar o latch
        if (count < 0) throw new IllegalArgumentException("count < 0");
        this.count = count;
    }

    public synchronized int getCount() {
        return count;
    }
}
