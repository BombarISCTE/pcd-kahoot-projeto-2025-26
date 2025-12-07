package Utils;

public class ModifiedCountdownLatch {
    private int count;
    private final int bonusFactor;
    private final int bonusCount;
    private final int waitPeriod;
    private int bonusUsados = 0;
    private boolean tempoExpirado = false;

    public ModifiedCountdownLatch(int bonusFactor , int bonusCount , int waitPeriod , int count ) {
        this.bonusFactor = bonusFactor;
        this.bonusCount = bonusCount;
        this.waitPeriod = waitPeriod;
        if (count < 0) throw new IllegalArgumentException("count < 0");
        this.count = count;
    }

    // As threads chamam isto para esperar, esperam ate que o contador chegue a 0 ou o tempo expire
    public synchronized void await() throws InterruptedException {
        while (count > 0 && !tempoExpirado) {
            wait();
        }
    }

    // O temporizador chama isto para reduzir o contador, se o tempo expirar lança uma exceção,
    // se o contador chegar a 0 notifica todas as threads em espera, retorna o fator de bónus se aplicável
    public synchronized int countDown() {
        if(tempoExpirado) {
            return 0;
        }

        int fator;
        if(bonusUsados < bonusCount){
           fator = bonusFactor;
           bonusUsados++;
        } else {
           fator = 1;
        }

        count--;

        if(count <= 0){
           notifyAll();
        }

        return fator;
    }

    // O temporizador chama isto quando o tempo expira para notificar todas as threads em espera
    public synchronized void tempoExpirado() {
        tempoExpirado = true;
        count = 0;
        notifyAll();
    }

    public synchronized int getCount() {
        return count;
    }

    public int getWaitPeriod() {
        return waitPeriod;
    }

}
