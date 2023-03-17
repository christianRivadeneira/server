package api.sys.model.form;

import java.util.List;
import metadata.model.Validation;

public class FormFieldDto {

    public String name;
    public String label;
    public String type;
    public String placeHolder;
    public FormFieldComboDto cmb;
    public FormFieldEnumDto enumOpts;
    public List<Validation> validations;
    public String slot;
    public Integer slotNum;
    public boolean editable;
    public String fixedEnum;
    public Boolean fixedBoolean;
    public Boolean titleCase;
}
