package metadata.model;

public class FormField {

    public String fldId;
    public boolean required;

    public Boolean readFromParent; //solo si es una FK
    public Integer slotNum; //solo si el tipo de datos 

    //si es FK y no es parentId ni SlotNum
    public String cmbParentFldId;
    public String cmbGrid;
    //si no es parentId ni SlotNum
    public boolean editable;
    public String fixedEnum;//si es tipo enum
    public Boolean fixedBoolean;//si es tipo boolean

    public Boolean titleCase;//si es tipo boolean
}
