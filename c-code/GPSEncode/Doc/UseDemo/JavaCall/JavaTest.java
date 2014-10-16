import com.fleety.base.*;
import java.io.*;

public class JavaTest
{
    public static void main(String[] args) {   
        System.out.println("Test start...");
        GPSEncode gps = new GPSEncode();
        System.out.println("start time: "+ new java.util.Date());

        for(int i=0; i<1000000; i++)
        {
           gps.call(false);   
        }

        System.out.println(" stop time: "+ new java.util.Date());
    }   
}  
