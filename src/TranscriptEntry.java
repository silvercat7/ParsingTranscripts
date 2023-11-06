public class TranscriptEntry {
    private double startTime;
    private double endTime;
    private String speaker;
    private String statement;

    public TranscriptEntry(double startTime, double endTime, String speaker, String statement) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.speaker = speaker;
        this.statement = statement;
    }

    public double getStartTime() {
        return startTime;
    }

    public double getEndTime() {
        return endTime;
    }

    public double getDuration() {
        return endTime - startTime;
    }

    public String getSpeaker() {
        return speaker;
    }

    public String getStatement() {
        return statement;
    }

    public String toString() {
        return "start time: " + startTime + ", end time: " + endTime + ", speaker: " + speaker + ", statement: " + statement;
    }
}
