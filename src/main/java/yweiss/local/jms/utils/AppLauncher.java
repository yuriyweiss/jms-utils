package yweiss.local.jms.utils;

import yweiss.local.jms.utils.worker.*;

import java.util.Arrays;

public class AppLauncher {

    private static final String GET_FROM_QUEUE = "get_from_queue";
    private static final String GET_FROM_DURABLE = "get_from_durable";
    private static final String DELETE_FROM_QUEUE = "delete_from_queue";
    private static final String DELETE_FROM_DURABLE = "delete_from_durable";
    private static final String SEND_TO_QUEUE = "send_to_queue";
    private static final String SEND_TO_TOPIC = "send_to_topic";

    public static void main(String[] args) {
        try {
            System.out.println("command arguments: " + Arrays.deepToString(args));
            String command = args[0];
            if (GET_FROM_QUEUE.equals(command)) {
                new GetFromQueue(Arrays.copyOfRange(args, 1, args.length)).execute();
            } else if (DELETE_FROM_QUEUE.equals(command)) {
                new DeleteFromQueue(Arrays.copyOfRange(args, 1, args.length)).execute();
            } else if (DELETE_FROM_DURABLE.equals(command)) {
                new DeleteFromDurable(Arrays.copyOfRange(args, 1, args.length)).execute();
            } else if (SEND_TO_QUEUE.equals(command)) {
                new SendToQueue(Arrays.copyOfRange(args, 1, args.length)).execute();
            } else if (SEND_TO_TOPIC.equals(command)) {
                new SendToTopic(Arrays.copyOfRange(args, 1, args.length)).execute();
            } else if (GET_FROM_DURABLE.equals(command)) {
                new GetFromDurable(Arrays.copyOfRange(args, 1, args.length)).execute();
            } else {
                System.out.println("command not recognized: " + command);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
