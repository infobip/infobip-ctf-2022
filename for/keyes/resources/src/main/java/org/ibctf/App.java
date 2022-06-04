package org.ibctf;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Random;
import java.util.UUID;

public class App {

    private static final int MAX_COLUMNS = 50;
    private static final int MAX_DATA = 150;
    private static final String FLAG = "ibctf{y0ung_h2s-run-fr33_hung-up-l1k3-my-m4n_and_m34}";

    private static String buildCreate(int maxColumns) {
        StringBuilder call = new StringBuilder("CREATE TABLE IF NOT EXISTS moneys (id INT, ");
        for (int i = 1; i <= maxColumns; ++i) {
            call.append(String.format("data%d VARCHAR(100)", i));
            call.append(i == maxColumns ? ");" : ", ");
        }
        return call.toString();
    }

    private static String buildInsert(int maxColumns) {
        StringBuilder stat = new StringBuilder("INSERT INTO moneys VALUES ");
        Random random = new Random();
        for (int i = 1; i <= MAX_DATA; ++i) {
            stat.append("(").append(i).append(", ");
            int pos = random.nextInt(maxColumns + 1);
            for (int j = 1; j <= maxColumns; ++j) {
                stat.append(String.format("'%s'", j == pos ? (i == 69 ? FLAG : "ibctf{" + UUID.randomUUID() + "}") : "$$$$$"));
                if (j != maxColumns) {
                    stat.append(", ");
                }
            }
            stat.append(")");
            if (i != MAX_DATA) {
                stat.append(", ");
            }
        }
        stat.append(";");
        return stat.toString();
    }

    private static void status(String s) {
        System.out.printf("Executing: %s\n", s);
    }

    public static void main(String[] args) {
        try {
            Files.deleteIfExists(FileSystems.getDefault().getPath("./data.mv.db"));
            Connection c = DriverManager.getConnection("jdbc:h2:./data;CIPHER=AES;USER=sa;PASSWORD=pwd ");
            String createCall = buildCreate(MAX_COLUMNS);
            status(createCall);
            c.prepareCall(createCall).execute();
            String insertCall = buildInsert(MAX_COLUMNS);
            status(insertCall);
            c.prepareStatement(insertCall).execute();
        } catch (Exception e) {
            System.err.println(e);
        }
    }
}
