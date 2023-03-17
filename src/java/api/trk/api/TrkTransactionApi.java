//package api.trk.api;
//
//import api.BaseAPI;
//import javax.ws.rs.Path;
//
//@Path("/trkTransaction")
//public class TrkTransactionApi extends BaseAPI {
//
//    /*    @POST
//    public Response insert(TrkTransaction obj) {
//        try (Connection conn = getConnection()) {
//            SessionLogin sl = getSession(conn);
//            obj.insert(conn);
//            return Response.ok(obj).build();
//        } catch (Exception ex) {
//            return createResponse(ex);
//        }
//    }
//
//    @PUT
//    public Response update(TrkTransaction obj) {
//        try (Connection conn = getConnection()) {
//            SessionLogin sl = getSession(conn);
//            obj.update(conn);
//            return Response.ok(obj).build();
//        } catch (Exception ex) {
//            return createResponse(ex);
//        }
//    }
//
//    @GET
//    public Response get(@QueryParam("id") int id) {
//        try (Connection conn = getConnection()) {
//            SessionLogin sl = getSession(conn);
//            TrkTransaction obj = new TrkTransaction().select(id, conn);
//            return Response.ok(obj).build();
//        } catch (Exception ex) {
//            return createResponse(ex);
//        }
//    }
//
//    @DELETE
//    public Response delete(@QueryParam("id") int id) {
//        try (Connection conn = getConnection()) {
//            SessionLogin sl = getSession(conn);
//            TrkTransaction.delete(id, conn);
//            return createResponse();
//        } catch (Exception ex) {
//            return createResponse(ex);
//        }
//    }
//
//    @GET
//    @Path("/getAll")
//    public Response getAll() {
//        try (Connection conn = getConnection()) {
//            SessionLogin sl = getSession(conn);
//            return createResponse(TrkTransaction.getAll(conn));
//        } catch (Exception ex) {
//            return createResponse(ex);
//        }
//    }*/
//}
