package util;

public class MGCModel {

    private int id;
    private String name;
    private String status;

    public MGCModel() {
        super();
        // TODO Auto-generated constructor stub
    }

    public MGCModel(int id, String name, String status) {
        super();
        this.id = id;
        this.name = name;
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "MGCMedol [id=" + id + ", name=" + name + ", status=" + status
                + "]";
    }

}
