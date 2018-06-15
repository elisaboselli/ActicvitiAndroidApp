package limpito.procesodenegocios;

public class ActivitiTask {

    private String id;
    private String name;
    private String desc;
    private String created;
    private String pinstance_id;

    public ActivitiTask(String id, String name, String desc, String created, String pinstance_id){
        this.id = id;
        this.name = name;
        this.pinstance_id = pinstance_id;

        if("null".equals(desc))
            this.desc = "No description provided.";
        else
            this.desc = desc;

        this.created = created.substring(8,10)+ "/" + created.substring(5,7) + "/" + created.substring(0,4);;
    }

    public String getId(){
        return this.id;
    }

    public String getName(){
        return this.name;
    }

    public String getDescription() {
        return this.desc;
    }

    public String getPrintableName(){
        return "(" + this.id + ") - " + this.name;
    }

    public String getPrintableInfo(){
        return "Created on " + this.created + " from process instance " + this.pinstance_id;
    }
}
