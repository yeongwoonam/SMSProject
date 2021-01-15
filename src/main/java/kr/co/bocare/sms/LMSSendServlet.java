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
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
@WebServlet(urlPatterns = {"/sendLMS"})
public class LMSSendServlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(LMSSendServlet.class);
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        process(req, resp);
    }
    private void process(HttpServletRequest request, HttpServletResponse response) throws IOException{
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json; charset=UTF-8");
        String from = request.getParameter("from");
        String to = request.getParameter("to");
        String text = request.getParameter("text");
        String time = request.getParameter("time");
        String trEtc1 = request.getParameter("groupId");
        String toName = request.getParameter("toName");

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm");
        try (PrintWriter out = response.getWriter();
             Connection connection = DatabaseConnector.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO MMS_MSG(REQDATE, TYPE, PHONE, CALLBACK, MSG, ETC1, ETC2, SUBJECT) VALUES(?, 0, ?, ?, ?, ?, ?, '')");
        ) {
            Date date = simpleDateFormat.parse(time);
            preparedStatement.setTimestamp(1, new Timestamp(date.getTime()));
            preparedStatement.setString(2, to);
            preparedStatement.setString(3, from);
            preparedStatement.setString(4, text);
            preparedStatement.setString(5, trEtc1);
            preparedStatement.setString(6, toName);
            JSONObject jsonObject = new JSONObject();
            if (preparedStatement.executeUpdate() == 1) {
                jsonObject.put("result", "success");
            } else {
                jsonObject.put("result", "failed");
            }
            out.print(jsonObject.toJSONString());
        } catch (SQLException | NamingException | ParseException throwables) {
            LOGGER.error("ERROR : {}",throwables.getMessage());
        }
    }
}
