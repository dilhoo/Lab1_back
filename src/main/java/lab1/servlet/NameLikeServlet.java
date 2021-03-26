package lab1.servlet;

import com.google.gson.Gson;
import lab1.EntityManagerProvider;
import lab1.ServletHelper;
import lab1.model.Vehicle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class NameLikeServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String value = req.getParameter("value");
        if (value == null) {
            ServletHelper.setBadRequest(resp, "No name substring specified");
            return;
        }
        List<Vehicle> result = EntityManagerProvider.provide()
                .createNamedQuery("nameLike", Vehicle.class)
                .setParameter("value", value)
                .getResultList();
        ServletHelper.setResult(resp, result, gson);
    }
}
