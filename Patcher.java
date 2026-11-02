import java.io.*;
import java.nio.file.*;

public class Patcher {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Usage: java Patcher <classfile>");
            return;
        }

        byte[] data = Files.readAllBytes(Path.of(args[0]));

        // Pattern: 03 B3 01 2C (iconst_0, putstatic autoFlight #300=0x012C)
        // Followed by: 14 xx xx B3 xx xx (ldc2_w -1.0, putstatic groundheight)
        // We NOP the first 4 bytes (03 B3 01 2C) to remove autoFlight=false

        byte[] pattern = new byte[] {
            0x03,                       // iconst_0
            (byte)0xB3, 0x01, 0x2C,   // putstatic #300 (autoFlight)
            0x14                        // ldc2_w (start of next instruction)
        };

        int patched = 0;
        for (int i = 0; i < data.length - pattern.length; i++) {
            boolean match = true;
            for (int j = 0; j < pattern.length; j++) {
                if (data[i+j] != pattern[j]) {
                    match = false;
                    break;
                }
            }
            if (match) {
                System.out.println("Found autoFlight=false at byte offset " + i);
                // NOP the 4 bytes (iconst_0 + putstatic)
                data[i]   = 0x00; // nop
                data[i+1] = 0x00; // nop
                data[i+2] = 0x00; // nop
                data[i+3] = 0x00; // nop
                patched++;
            }
        }

        if (patched == 0) {
            System.out.println("Pattern not found!");
            // Debug: search for all putstatic #300
            byte[] psAutoFlight = new byte[] { (byte)0xB3, 0x01, 0x2C };
            for (int i = 0; i < data.length - 3; i++) {
                if (data[i] == psAutoFlight[0] && data[i+1] == psAutoFlight[1] && data[i+2] == psAutoFlight[2]) {
                    System.out.printf("  putstatic autoFlight at byte %d, preceded by 0x%02X%n", i, data[i-1] & 0xFF);
                }
            }
        } else {
            Files.write(Path.of(args[0]), data);
            System.out.println("Patched " + patched + " occurrence(s). Saved.");
        }
    }
}
