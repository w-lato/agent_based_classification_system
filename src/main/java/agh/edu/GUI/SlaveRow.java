package agh.edu.GUI;

public class SlaveRow
{
    private String ID;
    private String type;
    private Double eval;

    public SlaveRow(String ID, String type, Double eval) {
        this.ID = ID;
        this.type = type;
        this.eval = eval;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getEval() {
        return eval;
    }

    public void setEval(Double eval) {
        this.eval = eval;
    }
}
