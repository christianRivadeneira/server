package api.ord.dto;

import api.ord.model.OrdPqrOther;
import api.ord.orfeo.OrfeoCreatePqrCommand;

/**
 *
 * @author Danny
 */
public class CreatePqrOtherRequest {

    public OrdPqrOther pqr;
    public OrfeoCreatePqrCommand command;
}
