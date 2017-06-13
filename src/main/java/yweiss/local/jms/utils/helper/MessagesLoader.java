package yweiss.local.jms.utils.helper;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MessagesLoader {

    public static List<MessageWrapper> loadMessagesFromBinFile(String sendMode, String fileName) throws Exception {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName));
        try {
            List<MessageWrapper> wrappers = (ArrayList<MessageWrapper>) ois.readObject();
            return filterMessagesBySendMode(sendMode, wrappers);
        } finally {
            ois.close();
        }
    }

    private static List<MessageWrapper> filterMessagesBySendMode(String sendMode, List<MessageWrapper> wrappers) throws Exception {
        if (SendConstants.MODE_BY_IDS.equals(sendMode)) {
            return filterWrappersByIds(wrappers);
        } else {
            return wrappers;
        }
    }

    private static List<MessageWrapper> filterWrappersByIds(List<MessageWrapper> wrappers) throws Exception {
        Set<String> messageIds = getMessageIdsFromStdin();
        List<MessageWrapper> result = new ArrayList<>();
        for (MessageWrapper wrapper : wrappers) {
            if (messageIds.contains(wrapper.getJmsMessageId())) {
                result.add(wrapper);
            }
        }
        return result;
    }

    private static Set<String> getMessageIdsFromStdin() throws Exception {
        Set<String> result = new HashSet<>();
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String s;
        while ((s = in.readLine()) != null && s.length() != 0) {
            result.add(s);
        }
        return result;
    }

    public static List<MessageWrapper> loadMessagesFromTextFile(String fileName) throws Exception {
        List<MessageWrapper> result = new ArrayList<>();
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
        String s;
        StringBuilder sb = new StringBuilder();
        while ((s = in.readLine()) != null) {
            if ("<!-- MESSAGE SEPARATOR -->".equals(s.trim())) {
                appendMessageWrapper(sb, result);
                sb = new StringBuilder();
            } else {
                sb.append("\n").append(s);
            }
        }
        appendMessageWrapper(sb, result);
        return result;
    }

    private static void appendMessageWrapper(StringBuilder sb, List<MessageWrapper> result) {
        String payload = sb.toString().trim();
        if (payload.length() > 0) {
            MessageWrapper wrapper = new MessageWrapper();
            wrapper.setMessageType(MessageWrapper.TEXT_MESSAGE);
            wrapper.setMessagePayload(sb.toString().trim());
            result.add(wrapper);
        }
    }
}
