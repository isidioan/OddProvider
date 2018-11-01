package OddsApi;

import OddsApi.Application;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import static OddsApi.Application.*;

public class EmailSender {
    public static <T extends HomeXAwayOdds> void sendEmail(final List<SureHomeXAwayPrediction> data) {

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        final Session session = Session.getDefaultInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication("##########", "########");
                    }
                });

        try {

            final Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("garagemanagement1@gmail.com"));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse("garagemanagement1@gmail.com"));

            message.setSubject("Testing Subject");

            final String columnsHtml =
                    Stream.of("Number", "MatchId", "Home Team", "Away Team",  "First Bookmaker", "Second Bookmaker", "Third Bookmaker", "HomeOdd",
                            "DrawOdd", "AwayOdd", "Score", "ProfitPercentage")
                            .collect(Collectors.joining("</th> \n <th>", "<th>", "</th>"));

            final StringBuilder strBuilder = new StringBuilder();
            IntStream.range(0, data.size())
                    .forEach(row -> strBuilder.append(data.get(row).toHtml(row + 1)));

            final String table =
                    "<html>\n" +
                            "<head>\n" +
                            "<style>\n" +
                            "table {\n" +
                            "    font-family: arial, sans-serif;\n" +
                            "    border-collapse: collapse;\n" +
                            "    width: 100%;\n" +
                            "}\n" +
                            "\n" +
                            "td, th {\n" +
                            "    border: 2px solid #CED7ED;\n" +
                            "    text-align: center;\n" +
                            "    padding: 8px;\n" +
                            "}\n" +
                            "\n" +
                            "tr:nth-child(even) {\n" +
                            "    background-color: #dddddd;\n" +
                            "}\n" +
                            "</style>\n" +
                            "</head>\n" +
                            "<body>\n" +
                            "\n" +
                            "<h2>1X2 Sure Bets</h2>\n" +
                            "\n" +
                            "<table>\n" +
                            "  <tr>\n" +
                            " " + columnsHtml +
                            "  </tr>\n"
                            + strBuilder +
                            "</table>\n" +
                            "\n" +
                            "</body>\n" +
                            "</html>";

            message.setContent(table, "text/html; charset=utf-8");

            Transport.send(message);

            System.out.println("Sent");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
    }
}