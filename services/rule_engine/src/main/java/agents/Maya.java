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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import lombok.*;

@Getter
@Setter
public class Maya extends Agent {

    private List<String> memory = new ArrayList<>();
    private int MAX_MEMORY = 6;
    private String name;
    private boolean askedStudyWithMe = false;
    private boolean askedForPomodoro = false;
    private boolean pomodoroOn = false;
    private boolean breakOn = false;
    private Date pomodoroTimer;
    private int pomodoroQuestionCounter = 0;
    private boolean notifiedAboutExamOption = false;
    private boolean notifiedAboutEssayOption = false;
    private Date lastInteractionTime;
    private String translationFor;
    private Date timeToMakeSchedule;
    private boolean askedForSchedule = false;
    private Date scheduleStart;
    private boolean askedForScheduleDuration = false;
    private int numberOfPomodoro = 0;
    private boolean scheduleOn = false;
    private boolean scheduleStarted = false;
    private List<Integer> pomodoroDaliyMemory = new ArrayList<>();
    private LocalDateTime lastPomodoroTime;
    private List<Float> weeklyAvgMemory = new ArrayList<>();

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
        Log.info("Sent message:\n" + text);
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
        Log.info("Translation input:\n" + text);
        String chatId = getConnections().get("telegram");
        GoogleTranslateAdapterAPI.translateMessage(chatId, text, source, target);
    }

    public void setScheduleTime() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1);
        int tomorrowYear = tomorrow.getYear();
        int tomorrowMonth = tomorrow.getMonthValue();
        int tomorrowDay = tomorrow.getDayOfMonth();

        String scheduleTime = String.format(
            "%04d-%02d-%02d 06:00", tomorrowYear, tomorrowMonth, tomorrowDay
        );
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        timeToMakeSchedule = convertLocalDateTimeToDate(
            LocalDateTime.parse(scheduleTime, formatter)
        );
    }

    public void trySetStartScheduleTime(String strTime) {
        try {
            String[] arrTime = strTime.split(":");

            if (arrTime.length != 2 || arrTime[0].length() > 2 || arrTime[1].length() > 2) {
                throw new Exception("Incorrect format of time!");
            }

            int hour = Integer.parseInt(arrTime[0]);
            int minute = Integer.parseInt(arrTime[1]);

            LocalDateTime now = LocalDateTime.now();
            int year = now.getYear();
            int month = now.getMonthValue();
            int day = now.getDayOfMonth();

            String scheduleTime = String.format(
                "%04d-%02d-%02d %02d:%02d", year, month, day, hour, minute
            );
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            LocalDateTime scheduleStartTime = LocalDateTime.parse(scheduleTime, formatter);
            LocalDateTime decrementedScheduleStartTime = scheduleStartTime.minusHours(2); 

            if (decrementedScheduleStartTime.isBefore(now)) {
                throw new Exception("Time for start schedule is before current time!");
            }

            scheduleStart = convertLocalDateTimeToDate(decrementedScheduleStartTime);
            askedForSchedule = false;
            askedForScheduleDuration = true;

            sendMessage(
                "Postavila sam vrijeme u??enja! Sada upi??i koliko pomodora ??eli?? u??iti, " +
                "jedan pomodoro je pola sata. Na primjer ako ??eli?? u??iti 2 i pol sata upi??i '5'."
            );

        } catch (Exception e) {
            Log.info("Error when tried to set start schedule time: " + e.getMessage());
            sendMessage(
                "Ne razumijem tvoj odgovor! Molim te upi??i odgovor u pravom formatu, "
                + "naprimjer ako ??eli?? po??eti u??iti u 15 sati i 45 minuta upi??i '15:45'!"
            );
        }
    }

    private LocalDateTime convertDateToLocalDateTime(Date dateToConvert) {
        return dateToConvert.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    private Date convertLocalDateTimeToDate(LocalDateTime dateToConvert) {
        return Date.from(dateToConvert.atZone(ZoneId.systemDefault()).toInstant());
    }

    public void trySetNumberOfPomodoro(String strNumPomodoro) {
         try {
            int numPomodoro = Integer.parseInt(strNumPomodoro);
            numberOfPomodoro = numPomodoro;
            askedForScheduleDuration = false;
            scheduleOn = true;
            sendMessage(
                "OK, raspored u??enja je spreman, ja ??u ti javiti kada treba?? po??eti!"
            );   
        } catch (Exception e) {
            Log.info("Error when tried to set number of pomodoro!");
            sendMessage(
                "Ne razumijem tvoj odgovor! Treba?? upisati brojkom koliko pomodora ??eli?? u??iti!"
            );
        }
    }

    public void decrementNumberOfPomodoro() {
        numberOfPomodoro -= 1;
    }

    public void incrementPomodoroDailyMemory(Date now) {
        LocalDateTime nowConverted = convertDateToLocalDateTime(now);
        LocalDateTime nowCorrect = nowConverted.plusHours(2);

        if (pomodoroDaliyMemory.isEmpty() || isNextDay(nowCorrect)) {
            pomodoroDaliyMemory.add(1);
        } else {
            int index = pomodoroDaliyMemory.size() - 1;
            Integer lastElement = pomodoroDaliyMemory.get(index);
            lastElement += 1;
            pomodoroDaliyMemory.set(index, lastElement);
        }

        lastPomodoroTime = nowCorrect;
    }

    private boolean isNextDay(LocalDateTime now) {
        int nowYear = now.getYear();
        int nowDay = now.getDayOfYear();

        int lastYear = lastPomodoroTime.getYear();
        int lastDay = lastPomodoroTime.getDayOfYear();

        if (nowYear > lastYear || nowDay > lastDay) {
            return true;
        }

        return false;
    }

    public void sendDailyLearningSummary() {
        int lastIndex = pomodoroDaliyMemory.size() - 1;
        
        if (lastIndex > 0 && pomodoroDaliyMemory.get(lastIndex) > pomodoroDaliyMemory.get(lastIndex - 1)) {
            sendMessage(
                String.format("Hej %s, gotovo je dogovoreno u??enje, danas si u??io/la vi??e nego ju??er! Bravo!", name)
            );
        } else if (lastIndex > 0 && pomodoroDaliyMemory.get(lastIndex) < pomodoroDaliyMemory.get(lastIndex - 1)) {
            sendMessage(
                String.format("Hej %s, gotovo je dogovoreno u??enje, danas si u??io/la manje nego ju??er! To je lo??e, treba vi??e u??iti!", name)
            );
        } else if (lastIndex > 0 && pomodoroDaliyMemory.get(lastIndex) == pomodoroDaliyMemory.get(lastIndex - 1)) {
            sendMessage(
                String.format("Hej %s, gotovo je dogovoreno u??enje, danas si u??io/la jednako kao i ju??er! To je OK!", name)
            );
        } else {
            sendMessage(
                String.format("Hej %s, gotovo je dogovoreno u??enje, sada je vrijeme za igru!", name)
            ); 
        }
    }

    public void sendWeeklyLearningSummary() {
        LocalDate today = LocalDate.now();
        DayOfWeek dayOfWeek = today.getDayOfWeek();

        if (dayOfWeek.getValue() == 7) {
            weeklyAvgMemory.add(calculateWeeklyAvg());
            trimPomodoroDaliyMemory();

            int lastIndex = weeklyAvgMemory.size() - 1;

            if (weeklyAvgMemory.size() >= 2 && Float.compare(weeklyAvgMemory.get(lastIndex), weeklyAvgMemory.get(lastIndex - 1)) > 0) {
                sendMessage(
                    String.format("Ovaj tjedan si u??io/la %.2f pomodora dnevno vi??e nego pro??li tjedan! Bravo!", 
                    (weeklyAvgMemory.get(lastIndex) - weeklyAvgMemory.get(lastIndex -1)))
                );
            } else if (weeklyAvgMemory.size() >= 2 && Float.compare(weeklyAvgMemory.get(lastIndex), weeklyAvgMemory.get(lastIndex - 1)) == 0) {
                sendMessage(
                    "Ovaj tjedan si u??io/la jednako kao i pro??li tjedan! Nije lo??e!"
                );
            } else {
                sendMessage(
                    String.format("Ovaj tjedan si u??io/la %.2f pomodora dnevno manje nego pro??li tjedan! To je lo??e!", 
                    (weeklyAvgMemory.get(lastIndex -1) - weeklyAvgMemory.get(lastIndex)))
                );
            }

            trimWeeklyAvgMemory();
        }
    }

    private void trimWeeklyAvgMemory() {
        if (weeklyAvgMemory.size() > 2) {
            weeklyAvgMemory = weeklyAvgMemory.subList(weeklyAvgMemory.size() - 2, weeklyAvgMemory.size());
        }
    }

    private float calculateWeeklyAvg() {
        int daysInAWeek = 7;

        if (pomodoroDaliyMemory.size() < daysInAWeek) {
            daysInAWeek = pomodoroDaliyMemory.size();
        }

        int lastIndex = pomodoroDaliyMemory.size() - 1;
        int firstIndex = lastIndex - daysInAWeek;
        
        float sum = 0;
        for (int i = firstIndex; i <= lastIndex; i++) {
            sum += pomodoroDaliyMemory.get(i);
        }

        float avg = sum / daysInAWeek;

        return avg;
    }

    private void trimPomodoroDaliyMemory() {
        if (pomodoroDaliyMemory.size() > 8) {
            pomodoroDaliyMemory = 
                pomodoroDaliyMemory.subList(pomodoroDaliyMemory.size() -1, pomodoroDaliyMemory.size());
        }
    }
}