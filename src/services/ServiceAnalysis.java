package services;

import users.Technician;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.Serializable;

public class ServiceAnalysis implements Serializable {
    private static final long serialVersionUID = 1L;
    private int code;
    private LabAnalysis analysis;
    private Technician technician;
    private Technician supervisor;
    private ArrayList<Test> tests;
    private String finishDate;
    private String finalResult;
    private String status;

    public ServiceAnalysis(int code, LabAnalysis analysis) {
        this.code = code;
        this.analysis = analysis;
        this.tests = new ArrayList<>();
        this.finishDate = "";
        this.finalResult = "";
        this.status = "pending";
    }

    public boolean setTechnician(Technician aTechnician) {
        this.technician = aTechnician;
        return true;
    }

    public boolean setSupervisor(Technician aSupervisor) {
        this.supervisor = aSupervisor;
        return true;
    }

    public boolean addTest(Test aTest) {
        if (aTest != null) {
            tests.add(aTest);
            return true;
        }
        return false;
    }

    public boolean removeTest(String aTestName) {
        Iterator<Test> iterator = tests.iterator();
        while (iterator.hasNext()) {
            Test test = iterator.next();
            if (test.getDesignation().equals(aTestName)) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    public boolean setFinalResult(String aResult) {
        this.finalResult = aResult;
        return true;
    }

    public boolean setFinishDate(String aDate) {
        this.finishDate = aDate;
        return true;
    }

    public int getCode() {
        return code;
    }

    public LabAnalysis getAnalysis() {
        return analysis;
    }

    public Technician getTechnician() {
        return technician;
    }

    public Technician getSupervisor() {
        return supervisor;
    }

    public ArrayList<Test> getTests() {
        return new ArrayList<>(tests);
    }

    public String getFinishDate() {
        return finishDate;
    }

    public String getFinalResult() {
        return finalResult;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
