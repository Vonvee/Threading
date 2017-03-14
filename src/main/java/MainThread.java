import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Vonvee on 08.03.2017.
 */
public class MainThread {
    private static Semaphore semaphore = new Semaphore(0);
    private static List<Integer> buffer = new ArrayList<Integer>();
    private static Object bufferSync = new Object();
    private static AtomicBoolean bufferEnd = new AtomicBoolean(false);

    public static void main(String[] args) {

        try {
            new MainThread().run();
            System.out.println("Program has been ended.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    private void run() throws InterruptedException {
        Thread producer = new Thread(new Producer());
        Thread consumer = new Thread(new Consumer());

        producer.start();
        consumer.start();

        producer.join();
        consumer.join();
    }


    class Producer implements Runnable {

        synchronized public void run() {
            Scanner in = new Scanner(System.in);
            String tempLine = "";
            do {
                System.out.println("Enter: ");
                tempLine = in.nextLine();

                synchronized (bufferSync) {
                    buffer.add(convert(tempLine));
                }
                semaphore.release();

            } while (!tempLine.equals("esc"));
            bufferEnd.set(true);
            System.out.println("Thread 1 is over.");
            semaphore.release();
        }

        private Integer convert(String word) {
            return Converter.replaceNumbers(word);
        }
    }


    private class Consumer implements Runnable {

        private static final int WAINTING_TIME = 2000;

        public void run() {
            System.out.println("Consumer started");
            try {
                int value;
                while (true) {
                    semaphore.acquire();
                    if (bufferEnd.get()) {
                        System.out.println("Thread 2 is over.");
                        return;
                    }
                    Thread.sleep(WAINTING_TIME);
                    synchronized (bufferSync) {
                        value = buffer.remove(0);
                    }
                    System.out.println("Consumer got: " + value);
                }
            } catch (InterruptedException e) {

            }
        }
    }
}

//This part has been taken and optimized from "http://stackoverflow.com/questions/4062022/how-to-convert-words-to-a-number"
class Converter {
    public static final String[] DIGITS = {"one", "two", "three", "four", "five", "six", "seven", "eight", "nine"};
    public static final String[] TENS = {null, "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety"};
    public static final String[] MAGNITUDES = {"hundred", "thousand", "million", "point"};

    public static Integer replaceNumbers(String input) {
        String result = "";
        String[] decimal = input.split(MAGNITUDES[3]);
        String[] millions = decimal[0].split(MAGNITUDES[2]);

        for (int i = 0; i < millions.length; i++) {
            String[] thousands = millions[i].split(MAGNITUDES[1]);

            for (int j = 0; j < thousands.length; j++) {
                int[] triplet = {0, 0, 0};
                StringTokenizer set = new StringTokenizer(thousands[j]);

                if (set.countTokens() == 1) { //If there is only one token given in triplet
                    String uno = set.nextToken();
                    triplet[0] = 0;
                    for (int k = 0; k < DIGITS.length; k++) {
                        if (uno.equals(DIGITS[k])) {
                            triplet[1] = 0;
                            triplet[2] = k + 1;
                        }
                        if (uno.equals(TENS[k])) {
                            triplet[1] = k + 1;
                            triplet[2] = 0;
                        }
                    }
                } else if (set.countTokens() == 2) {  //If there are two tokens given in triplet
                    String uno = set.nextToken();
                    String dos = set.nextToken();
                    if (dos.equals(MAGNITUDES[0])) {  //If one of the two tokens is "hundred"
                        for (int k = 0; k < DIGITS.length; k++) {
                            if (uno.equals(DIGITS[k])) {
                                triplet[0] = k + 1;
                                triplet[1] = 0;
                                triplet[2] = 0;
                            }
                        }
                    } else {
                        triplet[0] = 0;
                        for (int k = 0; k < DIGITS.length; k++) {
                            if (uno.equals(TENS[k])) {
                                triplet[1] = k + 1;
                            }
                            if (dos.equals(DIGITS[k])) {
                                triplet[2] = k + 1;
                            }
                        }
                    }
                } else if (set.countTokens() == 3) {  //If there are three tokens given in triplet
                    String uno = set.nextToken();
                    String tres = set.nextToken();
                    for (int k = 0; k < DIGITS.length; k++) {
                        if (uno.equals(DIGITS[k])) {
                            triplet[0] = k + 1;
                        }
                        if (tres.equals(DIGITS[k])) {
                            triplet[1] = 0;
                            triplet[2] = k + 1;
                        }
                        if (tres.equals(TENS[k])) {
                            triplet[1] = k + 1;
                            triplet[2] = 0;
                        }
                    }
                } else if (set.countTokens() == 4) {  //If there are four tokens given in triplet
                    String uno = set.nextToken();
                    String dos = set.nextToken();
                    String tres = set.nextToken();
                    String cuatro = set.nextToken();
                    for (int k = 0; k < DIGITS.length; k++) {
                        if (uno.equals(DIGITS[k])) {
                            triplet[0] = k + 1;
                        }
                        if (cuatro.equals(DIGITS[k])) {
                            triplet[2] = k + 1;
                        }
                        if (tres.equals(TENS[k])) {
                            triplet[1] = k + 1;
                        }
                    }
                } else {
                    triplet[0] = 0;
                    triplet[1] = 0;
                    triplet[2] = 0;
                }

                result = result + Integer.toString(triplet[0]) + Integer.toString(triplet[1]) + Integer.toString(triplet[2]);
            }
        }


        return Integer.parseInt(result);
    }
}