package Utils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class Timer extends Thread {
    private final int segundos;
    private Instant start;

    public Timer(int segundos) {
        this.segundos = segundos;
    }

    @Override
    public void run() {
        Instant start = Instant.now();
        this.start = start;

        try {
            for (int i = segundos; i > 0; i--) {
                System.out.println("Faltam " + i + "s");
                Thread.sleep(1000); // espera 1 segundo
            }

            Instant end = Instant.now();
            long millis = ChronoUnit.MILLIS.between(start, end);
            System.out.println("Tempo esgotado! (tempo decorrido: " + Math.round(millis / 1000.0) + "s)");

        } catch (InterruptedException e) {
            Instant end = Instant.now();
            long millis = ChronoUnit.MILLIS.between(start, end);
            System.out.println("Timer interrompido! Tempo decorrido: " + Math.round(millis / 1000.0) + "s");
            Thread.currentThread().interrupt();
        }
    }
    public long getElapsedTime() {
        if (start == null) {
            throw new IllegalStateException("Timer ainda não foi iniciado.");
        }
        Instant now = Instant.now();
        return Math.round(ChronoUnit.MILLIS.between(start, now)/1000.0);
    }

    public static void main(String[] args) {
        Timer t = new Timer(5); // exemplo com 5 segundos
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            System.out.println("Main interrompida!");
            Thread.currentThread().interrupt();
        }
    }
}
