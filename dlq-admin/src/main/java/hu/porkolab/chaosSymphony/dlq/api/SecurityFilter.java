package hu.porkolab.chaosSymphony.dlq.api;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class SecurityFilter implements Filter {
	private static final String TOKEN = System.getProperty("DLQ_ADMIN_TOKEN", "dev-token");

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest r = (HttpServletRequest) req;
		String t = r.getHeader("X-Admin-Token");
		if (t == null || !t.equals(TOKEN)) {
			((HttpServletResponse) res).sendError(401, "Unauthorized");
			return;
		}
		chain.doFilter(req, res);
	}
}