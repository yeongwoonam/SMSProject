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
import java.text.SimpleDateFormat;
import java.util.Date;
@WebServlet(urlPatterns = {"/getLMSHistoryDetailGroup"})
public class GetLMSHistoryDetailGroupServlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(GetLMSHistoryDetailGroupServlet.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        process(req, resp);
    }

    private void process(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json; charset=UTF-8");
        String trEtc1 = request.getParameter("trEtc1");
        // 2020-11-23 : 상세보기 팝업 내 검색 추가
        String trEtc2 = request.getParameter("trEtc2");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        try (PrintWriter out = response.getWriter();
             Connection connection = DatabaseConnector.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT REQDATE, STATUS,  PHONE, SENTDATE,  MSG, ETC2  FROM MMS_LOG WHERE ETC1 = ? AND ETC2 LIKE ? ORDER BY SENTDATE desc");
        ) {
            boolean isFirst = true;
            JSONObject jsonObject = new JSONObject();
            JSONArray trPhoneArray = new JSONArray();
            preparedStatement.setString(1, trEtc1);
            preparedStatement.setString(2, trEtc2);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                int i = 1;
                while (resultSet.next()) {
                    if (isFirst) {
                        Date trSendDate = resultSet.getTimestamp(1); // 보내기로 한 날짜, 예약한 시간
                        String resultCode = resultSet.getString(2); //결과 코드
                        Date realSendDate = resultSet.getTimestamp(4); //이동 통신사에서 처리한 시간
                        String trMessage = resultSet.getString(5); // 문자 내용
                        jsonObject.put("trSendDate", dateFormat.format(realSendDate));
                        jsonObject.put("trSendTime", timeFormat.format(realSendDate));
                        jsonObject.put("resultCode", resultCode);
                        jsonObject.put("trMessage", trMessage);
                        isFirst = false;
                    }
                    String trPhone = resultSet.getString(3); // 상대방(착신) 번호
                    String name = resultSet.getString(6);
                    JSONObject object = new JSONObject();
                    object.put("phoneNumber", trPhone);
                    object.put("number", String.valueOf(i++));
                    object.put("name", name);
                    trPhoneArray.add(object);
                }
                jsonObject.put("trPhones", trPhoneArray);
                out.print(jsonObject.toJSONString());
            }
        } catch (SQLException | NamingException throwables) {
            LOGGER.error("ERROR :{}", throwables.getMessage());
        }
    }
}
