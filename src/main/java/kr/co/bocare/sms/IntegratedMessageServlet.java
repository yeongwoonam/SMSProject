package kr.co.bocare.sms;

import kr.co.bocare.database.DatabaseConnector;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
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

@WebServlet(urlPatterns = {"/sendMessages"})
public class IntegratedMessageServlet extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(IntegratedMessageServlet.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        process(req, resp);
    }

    private void process(HttpServletRequest request, HttpServletResponse response) throws IOException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm");
        response.setContentType("application/json; charset=UTF-8");
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String message = request.getParameter("message");
        JSONArray jsonArray = (JSONArray) JSONValue.parse(message);
        int failed = 0;

        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = (JSONObject) jsonArray.get(i);

            String from = (String) jsonObject.get("from");
            String to = (String) jsonObject.get("to");
            String text = (String) jsonObject.get("text");
            String time = (String) jsonObject.get("time");
            String trEtc1 = (String) jsonObject.get("groupId");
            String toName = (String) jsonObject.get("toName");
            String type = (String) jsonObject.get("type");
            if (type.equals("lms")) {
                try (
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
                    if (preparedStatement.executeUpdate() == 1) {
                    } else {
                        failed++;
                    }
                } catch (SQLException | NamingException | ParseException throwables) {
                    LOGGER.error("ERROR : {}", throwables.getMessage());
                }
            } else if (type.equals("sms")) {
                try (
                        Connection connection = DatabaseConnector.getConnection();
                        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO SC_TRAN(TR_SENDDATE, TR_MSGTYPE, TR_PHONE, TR_CALLBACK, TR_MSG, TR_ETC1, TR_ETC2) VALUES(?, 0, ?, ?, ?, ?, ?)");
                ) {
                    Date date = simpleDateFormat.parse(time);
                    preparedStatement.setTimestamp(1, new Timestamp(date.getTime()));
                    preparedStatement.setString(2, to);
                    preparedStatement.setString(3, from);
                    preparedStatement.setString(4, text);
                    preparedStatement.setString(5, trEtc1);
                    preparedStatement.setString(6, toName);
                    if (preparedStatement.executeUpdate() == 1) {

                    } else {
                        failed++;
                    }
                } catch (SQLException | ParseException | NamingException throwables) {
                    LOGGER.error("ERROR : {}", throwables.getMessage());
                    try (PrintWriter out = response.getWriter()) {
                        out.println("오류 발생 : " + throwables.getMessage());
                    }
                }
            }
        }


        JSONObject resultObject = new JSONObject();
        if (failed == 0) {
            resultObject.put("result", "success");
        } else {
            resultObject.put("result", "failed");
        }
        try (
                PrintWriter out = response.getWriter()) {
            out.print(resultObject.toJSONString());
        }
    }
}
