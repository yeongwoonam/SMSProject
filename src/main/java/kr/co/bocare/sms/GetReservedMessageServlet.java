package kr.co.bocare.sms;

import kr.co.bocare.database.DatabaseConnector;
import org.json.simple.JSONArray;
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@WebServlet(urlPatterns = {"/getReservedMessage"})
public class GetReservedMessageServlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(GetReservedMessageServlet.class);


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        process(req, resp);
    }

    private void process(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json; charset=UTF-8");
        String from = request.getParameter("from");
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        String searchTarget = request.getParameter("searchTarget");
        JSONArray resultArray = new JSONArray();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        SimpleDateFormat sqlFormatting = new SimpleDateFormat("yyyy-MM-dd");
        if (searchTarget != null && !searchTarget.equals("")) {
            try (PrintWriter out = response.getWriter();
                 Connection connection = DatabaseConnector.getConnection();
                 // 2020-11-23 : 수신인 연락처 기준 검색을 수신인명 기준으로 변경
                 PreparedStatement preparedStatement = connection.prepareStatement("SELECT DISTINCT(TR_ETC1) from sc_tran WHERE TR_CALLBACK = ? AND (TR_SENDDATE BETWEEN ? AND ?) AND TR_ETC2 LIKE ? GROUP BY tr_etc1 ORDER BY tr_etc1 ");
            ) {

                preparedStatement.setString(1, from);
                preparedStatement.setDate(2, new java.sql.Date(sqlFormatting.parse(startDate).getTime()));
                preparedStatement.setDate(3, new java.sql.Date(Util.addOneDay(sqlFormatting.parse(endDate)).getTime()));
                preparedStatement.setString(4, searchTarget);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        String trEtc1 = resultSet.getString(1); // 그룹 아이디
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("trEtc", trEtc1);
                        try (PreparedStatement preparedStatement2 = connection.prepareStatement("SELECT TR_SENDDATE,  TR_ETC2,  TR_MSG FROM sc_tran WHERE TR_CALLBACK = ? AND TR_ETC1 = ?")) {
                            preparedStatement2.setString(1, from);
                            preparedStatement2.setString(2, trEtc1);
                            boolean isFirst = true;
                            try (ResultSet resultSet2 = preparedStatement2.executeQuery()) {
                                while (resultSet2.next()) {
                                    if (isFirst) {
                                        Date trSendDate = resultSet2.getTimestamp(1); // 보내기로 한 날짜, 예약한 시간
                                        String trEtc2 = resultSet2.getString(2); // 착신자 이름
                                        String trMessage = resultSet2.getString(3); // 문자 내용
                                        jsonObject.put("trSendDate", dateFormat.format(trSendDate));
                                        jsonObject.put("trPhone", trEtc2);
                                        jsonObject.put("trMessage", trMessage);
                                        isFirst = false;
                                    }
                                }
                                int count = resultSet2.getRow();
                                count -= 1;
                                jsonObject.put("count", String.valueOf(count));
                                resultArray.add(jsonObject);
                            }
                        }
                    }
                }
                JSONObject wrapperObject = new JSONObject();
                wrapperObject.put("result", resultArray);
                out.print(wrapperObject.toJSONString());
            } catch (SQLException | NamingException | ParseException throwables) {
                LOGGER.error("ERROR {}", throwables.getMessage());
            }
        } else {
            try (PrintWriter out = response.getWriter();
                 Connection connection = DatabaseConnector.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement("SELECT DISTINCT(TR_ETC1) from sc_tran WHERE TR_CALLBACK = ? AND (TR_SENDDATE BETWEEN ? AND ?) GROUP BY tr_etc1 ORDER BY tr_etc1 ");
            ) {

                preparedStatement.setString(1, from);
                preparedStatement.setDate(2, new java.sql.Date(sqlFormatting.parse(startDate).getTime()));
                preparedStatement.setDate(3, new java.sql.Date(Util.addOneDay(sqlFormatting.parse(endDate)).getTime()));
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        String trEtc1 = resultSet.getString(1); // 그룹 아이디
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("trEtc", trEtc1);
                        try (PreparedStatement preparedStatement2 = connection.prepareStatement("SELECT TR_SENDDATE,  TR_ETC2,  TR_MSG FROM sc_tran WHERE TR_CALLBACK = ? AND TR_ETC1 = ?")) {
                            preparedStatement2.setString(1, from);
                            preparedStatement2.setString(2, trEtc1);
                            boolean isFirst = true;
                            try (ResultSet resultSet2 = preparedStatement2.executeQuery()) {
                                while (resultSet2.next()) {
                                    if (isFirst) {
                                        Date trSendDate = resultSet2.getTimestamp(1); // 보내기로 한 날짜, 예약한 시간
                                        String trEtc2 = resultSet2.getString(2); // 착신자 이름
                                        String trMessage = resultSet2.getString(3); // 문자 내용
                                        jsonObject.put("trSendDate", dateFormat.format(trSendDate));
                                        jsonObject.put("trPhone", trEtc2);
                                        jsonObject.put("trMessage", trMessage);
                                        isFirst = false;
                                    }
                                }
                                int count = resultSet2.getRow();
                                count -= 1;
                                jsonObject.put("count", String.valueOf(count));
                                resultArray.add(jsonObject);
                            }
                        }
                    }
                }
                JSONObject wrapperObject = new JSONObject();
                wrapperObject.put("result", resultArray);
                out.print(wrapperObject.toJSONString());
            } catch (SQLException | NamingException | ParseException throwables) {
                LOGGER.error("ERROR {}", throwables.getMessage());
            }
        }

    }
}
