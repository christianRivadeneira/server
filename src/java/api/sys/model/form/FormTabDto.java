package api.sys.model.form;

import java.util.ArrayList;
import java.util.List;

public class FormTabDto {

    public String name;
    public String label;
    public List<FormFieldDto> flds = new ArrayList<>();
}
