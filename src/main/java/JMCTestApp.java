import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class JMCTestApp {
    // For memory leak simulation
    private static final List<byte[]> memoryLeakHolder = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        System.out.println("Starting JMCTestApp...");

        // Start worker threads
        for (int i = 0; i < 4; i++) {
            new Thread(new CpuWorker(), "CPU-Worker-" + i).start();
        }

        // Periodically trigger memory, file, and network activity
        for (int i = 0; i < 10; i++) {
            allocateMemory();       // Heap
            leakMemory();           // Simulate leak
            doFileIO();             // File I/O
            doNetworkIO();          // HTTP call
            Thread.sleep(5000);     // Give time for JMC to record events
        }

        System.out.println("JMCTestApp finished.");
    }

    static void allocateMemory() {
        byte[] block = new byte[10_000_000]; // 10MB
        for (int i = 0; i < block.length; i += 4096) {
            block[i] = 1;
        }
        System.out.println("Allocated 10MB block: " + block.hashCode());
    }

    static void leakMemory() {
        byte[] leak = new byte[5_000_000]; // 5MB
        memoryLeakHolder.add(leak);        // Keeps reference => simulates memory leak
        System.out.println("Simulated memory leak: " + leak.hashCode());
    }

    static void doFileIO() throws IOException {
        String filePath = "testfile.txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.write("Log entry at " + System.currentTimeMillis() + "\n");
        }
        System.out.println("Wrote to file: " + filePath);
    }

    static void doNetworkIO() {
        try {
            URL url = new URL("https://postman-echo.com/get");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            int code = conn.getResponseCode();
            System.out.println("Made HTTP request, response code: " + code);
        } catch (IOException e) {
            System.err.println("Network error: " + e.getMessage());
        }
    }

    static class CpuWorker implements Runnable {
        @Override
        public void run() {
            while (true) {
                double dummy = 0;
                for (int i = 1; i < 500_000; i++) {
                    dummy += Math.log(i) * Math.sqrt(i);
                }
                System.out.println(Thread.currentThread().getName() + ": CPU load = " + dummy);
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
