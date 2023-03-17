package api.smb.dto;

import java.util.Date;

public class BatchAnullRequest {

    public long from;
    public long to;
    public Date when;
    public int anullCauseId;
}
