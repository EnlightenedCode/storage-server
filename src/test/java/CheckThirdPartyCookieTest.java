import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.io.*;
import java.util.Arrays;
import javax.servlet.http.*;
import com.risevision.storage.api.CreateThirdPartyCookieServlet;
import org.junit.Test;
import com.risevision.storage.api.CheckThirdPartyCookieServlet;

public class CheckThirdPartyCookieTest {

    @Test
    public void itShouldWriteTrueIfCookieIsFound() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter strWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(strWriter);
        Cookie c = new Cookie("third_party_c_t","third_party_c_t");
        Cookie[] cookies = new Cookie[1];
        cookies[0] = c;
        CreateThirdPartyCookieServlet servlet = new CreateThirdPartyCookieServlet(c, Arrays.asList("http://storage.risevision.com", "http://localhost:8000"));
        when(response.getWriter()).thenReturn(writer);
        when(request.getCookies()).thenReturn(cookies);


        new CheckThirdPartyCookieServlet(servlet, Arrays.asList("http://storage.risevision.com", "http://localhost:8000")).doGet(request, response);

        writer.flush();
        System.out.println(strWriter.toString());
        assertTrue(strWriter.toString().contains("{\"check\":\"true\""));
    }
}