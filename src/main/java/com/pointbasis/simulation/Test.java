package com.pointbasis.simulation;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class Test {

    public static void main(String[] args) throws Exception {
        StringBuilder command = new StringBuilder();
        command.append("sudo aws configure set default.region us-west-2");
        run(command.toString());
    }

    public static Map<String, String> run(String command) throws Exception {
        Process process = Runtime.getRuntime().exec(command);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder successfulOutput = new StringBuilder();
        String error = null;
        String line, pre = null;
        while ((line = reader.readLine()) != null) {
            if (!line.equals(pre)) {
                successfulOutput.append(line);
                System.out.println(line);
                pre = line;
            }
        }
        reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        while ((line = reader.readLine()) != null) {
            error = line;
            System.out.println(line);
        }

        Map<String, String> output = new HashMap<>();
        output.put("successfulOutput", successfulOutput.toString());
        output.put("error", error);
        return output;
    }
}
