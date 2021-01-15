package kr.co.bocare.sms;

import kr.co.bocare.database.DatabaseConnector;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@WebServlet(urlPatterns = {"/deleteReservedMessage"})
public class DeleteReservedMessageServlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteReservedMessageServlet.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        process(req, resp);
    }

    private void process(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json; charset=UTF-8");
        String trEtc1 = request.getParameter("trEtc1");
        LOGGER.debug("trEtc1 : {}", trEtc1);
        try (PrintWriter out = response.getWriter();
             Connection connection = DatabaseConnector.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM sc_tran WHERE TR_ETC1 = ?");
        ) {
            preparedStatement.setString(1, trEtc1);
            JSONObject jsonObject = new JSONObject();
            int count = preparedStatement.executeUpdate();
            if (count >= 1) {
                jsonObject.put("result", "success");
                jsonObject.put("count", String.valueOf(count));
            } else {
                jsonObject.put("result", "failed");
            }
            out.print(jsonObject.toJSONString());
        } catch (SQLException | NamingException throwables) {
            LOGGER.error("ERROR : {}", throwables.getMessage());
            try (PrintWriter out = response.getWriter()) {
                out.println("오류 발생 : " + throwables.getMessage());
            }

        }
    }
}
