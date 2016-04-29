package io.stallion.selenium;

import jline.console.ConsoleReader;

import java.io.Console;
import java.io.IOException;



public class LineReader {
    private ConsoleReader reader;
    public LineReader() {
        try {
            this.reader = new ConsoleReader();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String prompt(String s) {
        try {
            return reader.readLine(s);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
