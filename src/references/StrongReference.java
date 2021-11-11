package references;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Objects;

public class StrongReference {
    private static final int RETRY = 3;
    //change this to stop the leak
    private static final boolean STOP_LEAK = false;
    interface SomeInterface{

        void logSomething(int i);
    }
    //Pretend this is your Application
    public static void main(String[] args) throws InterruptedException, UnsupportedEncodingException {
        System.out.println("Running Jar path = " + getRunningJarPath());
        deleteOldDumps();

        //say you implement this interface statically in Application class that you wont null, ever
        SomeInterface s = i -> System.out.println("Something " + i);

        //create a new object that has a thread
        A a = new A(s);
        //run the thread
        a.execute(1);

        //get refs before they are eligible for GC
        references.HeapDump.dumpHeap(getRunningJarPath() + "java/heap-dumps/strongRefBeforeGCEligible.hprof", false);

        //need to stop the thread and dereference the interface we created
        if (STOP_LEAK){
            a.stop();
        }

        //replace that object with a new one
        a = new A(s);
        //run the thread
        a.execute(2);

        //get refs before we call GC
        references.HeapDump.dumpHeap(getRunningJarPath() + "java/heap-dumps/strongRefBeforeGC.hprof", false);

        //need to stop the thread and dereference the interface we created
        if (STOP_LEAK){
            a.stop();
        }

        runGC();
        //get refs after we call GC
        references.HeapDump.dumpHeap(getRunningJarPath() + "java/heap-dumps/strongRefAfterGC.hprof", false);
    }


    static String getRunningJarPath() throws UnsupportedEncodingException {
        String path = StrongReference.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        return new File(URLDecoder.decode(path, "UTF-8")).getParent() + File.separator;
    }

    static void deleteOldDumps() throws InterruptedException {
        boolean isDeleted = deleteFiles();
        int attempt = 0;
        while (!isDeleted && attempt++ < RETRY) {
            isDeleted = deleteFiles();
            Thread.sleep(1000 * attempt);
        }
    }

    private static boolean deleteFiles() {
        File dir;
        try {
            dir = new File(getRunningJarPath() + "java/heap-dumps");

            if (!dir.exists()){
                dir.mkdirs();
            }
            File[] files = dir.listFiles();
            for (File file : Objects.requireNonNull(files)) {
                boolean deleted = file.delete();
                System.out.println(file + "deleted?" + deleted);
            }
            return files.length != 0;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return true;
    }

    static void runGC() throws InterruptedException {
        System.out.println("Running GC..");
        System.gc(); // Hint to run gc
        Thread.sleep(2000L); // sleep hoping to let GC thread run
        System.out.println("Finished running GC..");
    }
}