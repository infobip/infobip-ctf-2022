package org.ibctf;

import org.h2.store.fs.FilePathEncrypt;
import org.h2.tools.ChangeFileEncryption;

import java.sql.SQLException;
import java.util.Scanner;

public class EncryptFile {

    public static void main(String... args) {
        //ThisIsTheTimeThePlaceTheMotionGreaseIsTheWayWeAreFeeling
        System.out.print("Input: ");
        Scanner scanner = new Scanner(System.in);
        String seed = scanner.nextLine();
        try {
            ChangeFileEncryption.execute(".", "data", "AES", "pwd".toCharArray(), seed.toCharArray(), false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Perform dump at this point
        //  jcmd <PID> GC.heap_dump ./dump
        scanner.nextLine();
    }
}
