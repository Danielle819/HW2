package il.ac.tau.cs.software1;

import java.util.Arrays;

public class App {
    public static void main(String[] args){
        String str = " A string with \n various whitespace delimiters";
        String[] tokens = str.split(" ");
        System.out.println(Arrays.toString(tokens));
        tokens = str.split("\\s+");
        System.out.println(Arrays.toString(tokens));
        tokens = str.trim().split("\\s+");
        System.out.println(Arrays.toString(tokens));
        
        System.out.println("Working Directory " + System.getProperty("user.dir"));


    }
}
