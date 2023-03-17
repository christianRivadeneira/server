package api.mss.dto;

import java.util.List;

public class MssEventCreationRequestDto {

    public String eventType;
    public String notes;
    public Integer postId;
    public Integer typeId;
    public String incidentNotes;
    public Integer incidentTypeId;
    public List<MssEventFldDto> flds;
}
