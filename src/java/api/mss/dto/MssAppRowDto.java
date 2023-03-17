package api.mss.dto;

import api.mss.model.MssMinute;
import api.mss.model.MssMinuteEvent;
import api.mss.model.MssMinuteField;
import api.mss.model.MssMinuteIncident;
import api.mss.model.MssMinuteIncidentType;
import api.mss.model.MssMinuteType;
import api.mss.model.MssMinuteValue;
import api.mss.model.MssPost;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MssAppRowDto {

    public int id;
    public String title;
    public String subTitle;
    public boolean inEvent;
    public List<MssAppRowDto> incidents = new ArrayList<>();

    public static List<MssAppRowDto> getByPost(int postId, int minuteTypeId, Connection conn) throws Exception {
        SimpleDateFormat hdf = new SimpleDateFormat("hh:mm a");

        MssPost post = new MssPost().select(postId, conn);
        int clientId = post.clientId;

        List<MssAppRowDto> rta = new ArrayList<>();

        MssMinuteType t = new MssMinuteType().select(minuteTypeId, conn);
        MssMinute m = MssMinute.getByPostToday(postId, t.id, conn);
        if (m != null) {
            List<MssMinuteEvent> evs = MssMinuteEvent.getAll(m.id, conn);
            for (int j = 0; j < evs.size(); j++) {
                MssMinuteEvent ev = evs.get(j);
                MssAppRowDto evDto = new MssAppRowDto();
                rta.add(evDto);
                evDto.title = (ev.type.equals("in") ? "Entrada" : "Salida") + " " + hdf.format(ev.regDate);
                evDto.id = ev.id;
                evDto.inEvent = ev.type.equals("in");
                List<MssMinuteField> flds = MssMinuteField.getAll(t.id, conn);

                evDto.subTitle = "";
                for (int k = 0; k < flds.size(); k++) {
                    MssMinuteField fld = flds.get(k);
                    String v = MssMinuteValue.getValue(fld.id, ev.id, conn);
                    if (v != null) {
                        evDto.subTitle += ("<b>" + fld.name + "</b>: " + v + ". ");
                    }
                }

                List<MssMinuteIncident> incs = MssMinuteIncident.getByEvent(ev.id, conn);
                for (int k = 0; k < incs.size(); k++) {
                    MssMinuteIncident inc = incs.get(k);
                    MssMinuteIncidentType incType = new MssMinuteIncidentType().select(inc.typeId, conn);
                    MssAppRowDto incDto = new MssAppRowDto();
                    incDto.title = incType.name;
                    incDto.subTitle = inc.notes;
                    evDto.incidents.add(incDto);
                }
            }
        }
        return rta;
    }
    
}
