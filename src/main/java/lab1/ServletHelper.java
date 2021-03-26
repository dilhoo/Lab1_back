package lab1;

import com.google.gson.Gson;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Reader;

public final class ServletHelper {

    private ServletHelper() {
    }

    public static String getBody(HttpServletRequest req) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        char[] buf = new char[128];
        Reader reader = req.getReader();
        int bytesRead;
        while ((bytesRead = reader.read(buf)) != -1) {
            stringBuilder.append(buf, 0, bytesRead);
        }
        reader.close();
        return stringBuilder.toString();
    }

    private static void setResponse(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        resp.getWriter().write(message);
    }

    public static void setBadRequest(HttpServletResponse resp, String message) throws IOException {
        setResponse(resp, HttpServletResponse.SC_BAD_REQUEST, message);
    }

    public static void setNotFound(HttpServletResponse resp, String message) throws IOException {
        setResponse(resp, HttpServletResponse.SC_NOT_FOUND, message);
    }

    public static void setResult(HttpServletResponse resp, Object object, Gson gson) throws IOException {
        resp.setContentType("text/json; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(gson.toJson(object));
    }
}
