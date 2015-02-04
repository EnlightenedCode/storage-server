import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.io.*;
import javax.servlet.http.*;
import org.junit.Test;
import com.risevision.storage.api.CreateThirdPartyCookieServlet;
import java.util.Arrays;
import java.util.List;

public class CreateThirdPartyCookieTest {

    @Test
    public void itShouldAddCookieInResponse() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter strWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(strWriter);
        when(response.getWriter()).thenReturn(writer);

        Cookie c = new Cookie("third_party_c_t","third_party_c_t");
        List<String> domains = Arrays.asList("http://storage.risevision.com","http://localhost:8000");
        new CreateThirdPartyCookieServlet(c, domains).doGet(request, response);

        writer.flush();
        System.out.println(strWriter.toString());
        assertTrue(strWriter.toString().contains("{\"completed\": \"true\"}"));
    }
}