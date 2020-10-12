package rpc;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

public class RpcHelper {
    public static void writeJson(HttpServletResponse response, String jsonString) {
        try {
            response.setContentType("application/json");
            response.addHeader("Access-Control-Allow-Origin", "*");
            PrintWriter out = response.getWriter();
            out.println(jsonString);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
