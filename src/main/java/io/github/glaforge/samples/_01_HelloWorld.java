package io.github.glaforge.samples;

import io.github.glaforge.antigravity.Agent;
import io.github.glaforge.antigravity.AgentConfig;
import io.github.glaforge.antigravity.AgentResponse;

import java.util.concurrent.TimeUnit;

/**
 * Sample 01: A simple Hello World demonstrating how to create and chat with an Agent
 * using the unofficial Antigravity SDK for Java.
 */
public class _01_HelloWorld {

    public static void main(String[] args) {
        System.out.println("=== Antigravity Java SDK - 01 Hello World ===");

        // Configure the agent with system instructions
        AgentConfig config = AgentConfig.builder()
                .instructions("You are a helpful and concise AI assistant.")
                .build();

        // Always wrap Agent in try-with-resources to ensure proper cleanup of the Go harness process
        try (Agent agent = new Agent(config)) {
            String prompt = "Hello! Please introduce yourself in one or two sentences.";
            System.out.println("User: " + prompt);
            System.out.println("\nWaiting for agent response...\n");

            AgentResponse response = agent.chat(prompt).get(120, TimeUnit.SECONDS);

            System.out.println("Agent:\n" + response.text());
        } catch (Exception e) {
            System.err.println("Error running agent: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
