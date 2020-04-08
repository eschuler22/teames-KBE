package htwb.ai.teames;

public class TestResult {

    private String methodName;
    
    private Boolean passed;
    
    private boolean executionError;
     
    private String reasonForError;

    public TestResult(String methodName, Boolean passed) {
        super();
        this.methodName = methodName;
        this.passed = passed;
        this.executionError = false;
        this.reasonForError = "";
    }

    public TestResult(String methodName, String reasonForFailedExecution) {
        super();
        this.methodName = methodName;
        this.passed = null;
        this.executionError = true;
        this.reasonForError = reasonForFailedExecution;
    }
    
    @Override
    public String toString() {
        
        StringBuilder strB = new StringBuilder ("TestResult for '" + methodName + "': ");
        
        if (passed != null) {
            if (passed.booleanValue()) {
                strB.append("passed");
            } else {
                strB.append("failed");
            }
            return strB.toString();
        }
        
        if (executionError) {
            return strB.append( "error due to '")
                    .append(reasonForError).append("'").toString();
        }
    
        return strB.append( "Hmm, I don't understand what happened!!!").toString();
    }
}
