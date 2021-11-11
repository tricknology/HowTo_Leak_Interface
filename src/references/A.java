package references;

import java.util.concurrent.atomic.AtomicBoolean;

class A {

    private int id;

    private StrongReference.SomeInterface s;
    private AtomicBoolean stopped = new AtomicBoolean(false);

    A(StrongReference.SomeInterface s) {
        this.s = s;
    }

    private Thread t;

    public void execute(int id) {
        this.id = id;
        t = new Thread(() -> {
            while (!stopped.get()) {
                try {
                    s.logSomething(id);
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.out.println("interrupted " + id);
                }
            }
            System.out.println("end while loop"  + id);
        }, String.valueOf(id));
        t.start();
    }

    public void stop() {
        System.out.println("Stop called on " + id);
        if (!stopped.get()) {
            stopped.set(true);
        }
        if (t.isAlive()) {
            t.interrupt();
        }
    }

}
