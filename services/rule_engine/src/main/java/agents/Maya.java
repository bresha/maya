package agents;

import com.mindsmiths.ruleEngine.model.Agent;
import com.mindsmiths.telegramAdapter.TelegramAdapterAPI;
import com.mindsmiths.gpt3.GPT3AdapterAPI;
import com.mindsmiths.gpt3.completion.GPT3Completion;
import com.mindsmiths.GoogleTranslateAdapter.GoogleTranslateAdapterAPI;
import com.mindsmiths.ruleEngine.util.Log;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import lombok.*;

@Getter
@Setter
public class Maya extends Agent {

    private List<String> memory = new ArrayList<>();
    private int MAX_MEMORY = 6;
    private String name;
    private boolean askedStudyWithMe;
    private boolean askedForPomodoro;
    private boolean pomodoroOn;
    private boolean breakOn;
    private Date pomodoroTimer;
    private int pomodoroQuestionCounter = 0;
    private boolean notifiedAboutExamOption = false;
    private boolean notifiedAboutEssayOption = false;
    private Date lastInteractionTime;
    private String translationFor;

    public Maya() {
    }

    public Maya(String connectionName, String connectionId) {
        super(connectionName, connectionId);
    }

    private void trimMemory() {
        if (memory.size() > MAX_MEMORY + 1)
            memory = memory.subList(memory.size() - 1 - MAX_MEMORY, memory.size());
    }

    public void addMessageToMemory(String sender, String text) {
        memory.add(String.format("%s: %s\n", sender, text));
        trimMemory();
    }

    public void clearMemory() {
        memory.clear();
    }

    public void incrementPomodoroQuestionCounter() {
        pomodoroQuestionCounter += 1;
    }

    public void sendMessage(String text) {
        String chatId = getConnections().get("telegram");
        TelegramAdapterAPI.sendMessage(chatId, text);
        lastInteractionTime = new Date();
    }

    public void askGPT3() {
        String intro = "This is a conversation between a human and an intelligent AI assistant named Maya.\n";
        simpleGPT3Request(intro + "Human: " + String.join("\n", memory) + "\nMaya:");
    }

    public void simpleGPT3Request(String prompt) {
        Log.info("Prompt for GPT-3:\n" + prompt);
        GPT3AdapterAPI.complete(
            prompt, // input prompt
            "text-davinci-001", // model
            150, // max tokens
            0.9, // temperature
            1.0, // topP
            1, // N
            null, // logprobs
            false, // echo
            List.of("Human:", "Maya:"), // STOP words
            0.6, // presence penalty
            0.0, // frequency penalty
            1, // best of
            null // logit bias
        );
    }

    public void translate(String text, String source, String target) {
        String chatId = getConnections().get("telegram");
        GoogleTranslateAdapterAPI.translateMessage(chatId, text, source, target);
    }
}