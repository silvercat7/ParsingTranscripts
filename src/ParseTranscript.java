import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class ParseTranscript {
    private ArrayList<TranscriptEntry> entries;
    private ArrayList<String> speakers;

    public ParseTranscript(String fileName) throws IOException {
        entries = new ArrayList<>();
        String[] lines = parseFile(fileName);
        for (int current = 2; current < lines.length; current += 4) {
            double startTime = parseStartTime(lines[current + 1]);
            double endTime = parseEndTime(lines[current + 1]);
            String speaker = parseName(lines[current + 2]);
            if (speaker.equals("this line is not formatted correctly :(")) {
                speaker = entries.get(entries.size() - 1).getSpeaker();
            }
            String statement = parseStatement(lines[current + 2]);
            TranscriptEntry entry = new TranscriptEntry(startTime, endTime, speaker, statement);
            entries.add(entry);
        }
        outputSummaryStatistics();
        outputCondensedTranscript();
    }

    public String[] parseFile(String fileName) throws IOException {
        String data = readFile(fileName);
        return data.split("\n");
    }

    public String readFile(String fileName) throws IOException {
        return new String(Files.readAllBytes(Paths.get(fileName)));
    }

    public double parseStartTime(String timeLine) {
        double seconds = 0;
        int hoursIndex = timeLine.indexOf(":");
        String hours = timeLine.substring(0, hoursIndex);
        seconds += ((Double.parseDouble(hours)) * 3600);
        int minutesIndex = timeLine.indexOf(":", hoursIndex + 1);
        String minutes = timeLine.substring(hoursIndex + 1, minutesIndex);
        seconds += ((Double.parseDouble(minutes)) * 60);
        String secs = timeLine.substring(minutesIndex + 1, minutesIndex + 7);
        seconds += (Double.parseDouble(secs));
        return seconds;
    }

    public double parseEndTime(String timeLine) {
        double seconds = 0;
        int startIndex = timeLine.indexOf(">") + 2;
        int hoursIndex = timeLine.indexOf(":", startIndex);
        String hours = timeLine.substring(startIndex, hoursIndex);
        seconds += ((Double.parseDouble(hours)) * 3600);
        int minutesIndex = timeLine.indexOf(":", hoursIndex + 1);
        String minutes = timeLine.substring(hoursIndex + 1, minutesIndex);
        seconds += ((Double.parseDouble(minutes)) * 60);
        String secs = timeLine.substring(minutesIndex + 1);
        seconds += (Double.parseDouble(secs));
        return seconds;
    }

    public String parseName(String nameLine) {
        int index = nameLine.indexOf(":");
        if (index == -1) {
            return "this line is not formatted correctly :(";
        }
        return nameLine.substring(0, index);
    }

    public String parseStatement(String nameLine) {
        int index = nameLine.indexOf(":");
        if (index == -1) {
            return nameLine;
        }
        return nameLine.substring(index + 2);
    }

    public static void writeDataToFile(String filePath, String data) {
        try (FileWriter f = new FileWriter(filePath);
             BufferedWriter b = new BufferedWriter(f);
             PrintWriter writer = new PrintWriter(b)) {
            writer.println(data);
        } catch (IOException error) {
            System.err.println("there was a problem writing to the file " + filePath);
            error.printStackTrace();
        }
    }

    public void outputSummaryStatistics() {
        try {
            PrintWriter summaryStatistics = new PrintWriter(new FileWriter("summaryStatistics.txt"));
            summaryStatistics.println("total number of speakers: " + getNumSpeakers());
            summaryStatistics.println("total length of session: " + getSessionLength() + " minutes");
            summaryStatistics.println("total speaking time: " + getSpeakingTime() + " minutes");
            summaryStatistics.println("total number of speaker switches: " + getSpeakerSwitches());
            summaryStatistics.println();
            summaryStatistics.println("total talk time: ");
            for (String speaker : speakers) {
                summaryStatistics.println(speaker + ": " + getSpeakerTime(speaker) + " minutes - " + getSpeakingTimePercentage(speaker) + "%");
            }
            summaryStatistics.println();
            summaryStatistics.println("average length of a speech event:");
            for (String speaker : speakers) {
                summaryStatistics.println(speaker + ": " + averageLengthOfSpeechEvent(speaker) + " minutes");
            }
            summaryStatistics.close();
        } catch (IOException error){
            error.printStackTrace();
        }
    }

    public void outputCondensedTranscript() {
        try {
            PrintWriter condensedTranscript = new PrintWriter(new FileWriter("condensedTranscript.txt"));
            String speaker = entries.get(0).getSpeaker();
            String statement = entries.get(0).getStatement();
            for (int i = 1; i < entries.size(); i++) {
                TranscriptEntry entry = entries.get(i);
                if (entry.getSpeaker().equals(speaker)) {
                    statement += " ";
                    statement += entry.getStatement();
                } else {
                    condensedTranscript.println(speaker + ": " + statement);
                    speaker = entry.getSpeaker();
                    statement = entry.getStatement();
                }
            }
            condensedTranscript.close();
        } catch (IOException error) {
            error.printStackTrace();
        }
    }

    public int getNumSpeakers() {
        speakers = new ArrayList<>();
        for (TranscriptEntry entry : entries) {
            if (speakers.isEmpty()) {
                speakers.add(entry.getSpeaker());
            } else {
                for (int i = 0; i < speakers.size(); i++) {
                    if (speakers.get(i).equals(entry.getSpeaker())) {
                        break;
                    } else if (i == speakers.size() - 1) {
                        speakers.add(entry.getSpeaker());
                    }
                }
            }
        }
        return speakers.size();
    }

    public double getSessionLength() {
        return (entries.get(entries.size() - 1).getEndTime())/60;
    }

    public double getSpeakingTime() {
        double speakingTime = 0;
        for (TranscriptEntry entry : entries) {
            speakingTime += entry.getDuration();
        }
        return speakingTime/60;
    }

    public int getSpeakerSwitches() {
        int numSpeakerSwitches = 0;
        String speaker = entries.get(0).getSpeaker();
        for (TranscriptEntry entry : entries) {
            if (!entry.getSpeaker().equals(speaker)) {
                numSpeakerSwitches++;
                speaker = entry.getSpeaker();
            }
        }
        return numSpeakerSwitches;
    }

    public double getSpeakerTime(String speaker) {
        int speakerTime = 0;
        for (TranscriptEntry entry : entries) {
            if (entry.getSpeaker().equals(speaker)) {
                speakerTime += entry.getDuration();
            }
        }
        return speakerTime/60.0;
    }

    public double getSpeakingTimePercentage(String speaker) {
        return (getSpeakerTime(speaker)/getSpeakingTime()) * 100;
    }

    public double averageLengthOfSpeechEvent(String speaker) {
        double allLengths = 0;
        double allEntries = 0;
        for (TranscriptEntry entry : entries) {
            if (entry.getSpeaker().equals(speaker)) {
                allLengths += entry.getDuration();
                allEntries++;
            }
        }
        return (allLengths/allEntries)/60;
    }
}
